/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.RuleSetMap;
import uk.co.revsys.oddball.RuleTypeMap;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.oddball.util.OddUtil;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class RuleSetImpl implements RuleSet{    

    public RuleSetImpl(){
    }
    
    public RuleSetImpl(String name, boolean inMemory){
        String dbName = name.replace("/","-")+"-persist";
        setPersist(new MongoDBHelper(dbName, inMemory));
        this.name = name;
    }

    List<Rule> rules = new ArrayList<Rule>();
    List<Rule> extraRules = new ArrayList<Rule>();
    Map<String, String> prefixes = new HashMap<String, String>();

    private String name;
    private String ruleType;
    private String forEachIn;

    @Override
    public String getForEachIn() {
        return forEachIn;
    }

    @Override
    public void setForEachIn(String forEachIn) {
        this.forEachIn = forEachIn;
    }
    private MongoDBHelper persist;
    private Class ruleClass;

    @Override
    public void addRule(Rule rule) {
        rules.add(rule);
    }

    @Override
    public Rule findExtraRule(String prefix, String ruleString){
        Rule foundRule = null;
        for (Rule rule : extraRules){
            if ((prefix.equals("") || (rule.getLabel().indexOf(prefix)==0)) && rule.getRuleString().equals(ruleString)){
                foundRule = rule;
                break;
            }
        }
        return foundRule;
    }
    
    @Override
    public void addExtraRule(Rule rule) {
        extraRules.add(rule);
    }

    @Override
    public void removeExtraRule(Rule rule) {
        extraRules.remove(rule);
    }

    @Override
    public void addPrefix(String prefix, String defaultValue) {
        prefixes.put(prefix, defaultValue);
    }

    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr, int persistOption, String duplicateQuery, String avoidQuery, String ensureIndexes) throws InvalidCaseException, IOException{
        return assessCase(aCase, key, ruleSetStr, persistOption, duplicateQuery, avoidQuery, ensureIndexes, this.forEachIn);
    }

    
    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr, int persistOption, String duplicateQuery, String avoidQuery, String ensureIndexes, String forEachIn) throws InvalidCaseException, IOException{

        Opinion op = new OpinionImpl();
        MapCase aMapCase;
        
        if (forEachIn!=null){ // multiple Case
            try {
                aMapCase = (MapCase) aCase;
            } catch (ClassCastException e){
                aMapCase = new MapCase(aCase.getContent());
            }
            List subCases = (List)((Map<String, Object>)aMapCase.getContentObject()).get(this.forEachIn);
            if (subCases != null){
                for (Object subCaseString: subCases){
                    ((Map<String, Object>)aMapCase.getContentObject()).put(this.forEachIn, subCaseString);
                    aMapCase.setContent(JSONUtil.map2json((Map<String, Object>)aMapCase.getContentObject()));
                    Case subCase = new MapCase(aMapCase.getContent());
                    String caseDuplicateQuery= null;
                    String caseAvoidQuery= null;
                    if (duplicateQuery!=null){
                        caseDuplicateQuery=new OddUtil().replacePlaceholders(duplicateQuery, (Map<String, Object>)subCase.getContentObject());
                    }
                    if (avoidQuery!=null){
                        caseAvoidQuery=new OddUtil().replacePlaceholders(avoidQuery, (Map<String, Object>)subCase.getContentObject());
                    }
                    Opinion subOp = assessCase(subCase, null, ruleSetStr, persistOption, caseDuplicateQuery, caseAvoidQuery, ensureIndexes, null);
                    op.getTags().addAll(subOp.getTags());
                }
            }
            return op;
            
        } else { // single case
            for (Rule rule: rules){
                Assessment as = rule.apply(aCase, this, key);
//                try{
//                    LOGGER.debug("assessing:"+rule.asJSON().toString());
//                    LOGGER.debug(aCase.getJSONisedContent().toString());
//                } catch (Exception e){}
//                LOGGER.debug(as.getLabelStr());
                op.incorporate(as);
            }
            if (op.getTags().isEmpty()){
                boolean found = false;
                for (Rule rule:extraRules){
                    Assessment as = rule.apply(aCase, this, key);
                    if (as.getLabelStr()!=null){
                        op.incorporate(as);
                        found=true;
                        break;
                    }
                }
                if (!found){
                    op.getTags().add("*odDball*");
                }
            }
            for (String prefix:prefixes.keySet()){
                boolean found = false;
                for (String tag: op.getTags()){
                    if (tag.indexOf(prefix)==0){
                        found=true;
                        break;
                    }
                }
                if (!found){
                    for (Rule rule:extraRules){
                        if (rule.getLabel().indexOf(prefix)==0){
                            Assessment as = rule.apply(aCase, this, key);
                            if (as.getLabelStr()!=null){
                                op.incorporate(as);
                                found=true;
                                break;
                            }
                        }
                    }
                }
                if (!found){
                    op.getTags().add(prefix+prefixes.get(prefix));
                }
            }
            if (op.getLabel().contains("*ignore*")){
                op.getTags().clear();
            } else {
                if (persistOption != NEVERPERSIST){
                    String persistCase = op.getEnrichedCase(ruleSetStr, aCase, true);
                    String caseAvoidQuery= null;
                    if (avoidQuery!=null){
                        caseAvoidQuery=new OddUtil().replacePlaceholders(avoidQuery, (Map<String, Object>)aCase.getContentObject());
                    }
                    String id = getPersist().checkAlreadyExists(caseAvoidQuery);
                    if (caseAvoidQuery==null || getPersist().checkAlreadyExists(caseAvoidQuery)==null){
                        if (persistOption == UPDATEPERSIST){
                            String caseDuplicateQuery= null;
                            if (duplicateQuery!=null){
                                caseDuplicateQuery=new OddUtil().replacePlaceholders(duplicateQuery, (Map<String, Object>)aCase.getContentObject());
                            }
                            id = getPersist().checkAlreadyExists(caseDuplicateQuery);
                            while (id != null){
                                getPersist().removeCase(id);
                                id = getPersist().checkAlreadyExists(caseDuplicateQuery);
                            }
                        }
                        if (ensureIndexes!=null){
                            getPersist().ensureIndexes(ensureIndexes);
                        }
                        getPersist().insertCase(persistCase);
                    }
                    //LOGGER.debug("inserting:"+duplicateQuery);
                }
            }
            return op;
            }
    
    }

   
    @Override
    public List<Rule> getRules() {
        List<Rule> result = rules;
        return result;
    }

    @Override
    public List<Rule> getAllRules() {
        List<Rule> result = new ArrayList<Rule>();
        result.addAll(rules);
        result.addAll(extraRules);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the ruleType
     */
    @Override
    public String getRuleType() {
        return ruleType;
    }

    /**
     * @param ruleType the ruleType to set
     */
    @Override
    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
    
    @Override
    public MongoDBHelper getPersist() {
        return persist;
    }

    @Override
    public final void setPersist(MongoDBHelper persist) {
        this.persist = persist;
    }

    @Override
    public Rule createRule(String prefix, String label, String ruleString, String description, String source, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        try{
            Rule ruleInstance = (Rule) ruleClass.newInstance();
            ruleInstance.setLabel(prefix+label);
            ruleInstance.setRuleString(ruleString, resourceRepository) ;
            ruleInstance.setSource(source) ;
            ruleInstance.setDescription(description);
            return ruleInstance;
        } catch (InstantiationException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        } catch (IllegalAccessException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        } catch (RuleNotLoadedException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        }
        
    }
    
    @Override
    public void loadRules(List<String> rules, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        this.getRules().clear();
        this.extraRules.clear();
        this.prefixes.clear();
        String prefix = "";
        for (String rule : rules){
            String trimRule = rule.trim();
            if ((trimRule.indexOf("#")!=0)&&(trimRule.indexOf("$")!=0)&&(!trimRule.equals(""))){
                if (Pattern.matches("\\[.*\\]", trimRule)){
                    prefix = trimRule.substring(1, trimRule.length()-1);
                    String defaultValue = "odDball";
                    if (prefix.contains(":")){
                        String[]parsed = prefix.split(":");
                        prefix = parsed[0]+".";
                        defaultValue = parsed[1];
                    } else {
                        prefix = prefix+".";
                    }
                    if (prefix.equals("other.")){  // prefix heading "[other]" counts as no prefix at all
                        prefix="";
                    } else {
                        this.addPrefix(prefix, defaultValue);
                    }
                }else if (Pattern.matches(".*:.*", trimRule)){
                    String[] parsed = trimRule.split(":",2);
                    String source = "config";
                    String label = parsed[0];
                    if (Pattern.matches(".*;.*", label)){
                        String[] parsedLabel = label.split(";",2);
                        source = parsedLabel[1];
                        label = parsedLabel[0];
                    }
                    String description = "";
                    String ruleStr = parsed[1];
                    if (Pattern.matches(".*##.*", ruleStr)){
                        String[] split = ruleStr.split("##");
                        description = split[1].trim();
                        ruleStr = split[0].trim();
                    }
                    Rule ruleInstance = createRule(prefix, label, ruleStr, description, source, resourceRepository);
                    if (source.equals("config")){
                        this.addRule(ruleInstance);
                    } else {
                        this.addExtraRule(ruleInstance);
                    }
                }
            }
        }
    }
    
    public static List<String> getRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        String path = ".";
        if (ruleSetName.contains("/")){
            path = path+"/"+ruleSetName.substring(0, ruleSetName.lastIndexOf("/"));
            ruleSetName = ruleSetName.substring(ruleSetName.lastIndexOf("/")+1);
        }
        try{
            List<Resource> resources = resourceRepository.listResources(path);
            List<String> rules = new ArrayList<String>();
            boolean rulesetFound = false;
            for (Resource resource : resources){
                if ((resource.getName().equals(ruleSetName)) || (resource.getName().indexOf(ruleSetName+".")==0) &&(!resource.getName().contains(".json"))){
//                if ((resource.getName().indexOf(ruleSetName)==0) &&(resource.getName().indexOf(".json")==-1)){
                    rulesetFound = true;
//                    Resource resource = new Resource("", ruleSetName);
                    InputStream inputStream = resourceRepository.read(resource);
                    rules.addAll(IOUtils.readLines(inputStream));
                }
            }
            if(!rulesetFound){
                throw new RuleSetNotLoadedException(ruleSetName);
            }
            return rules;
        }
        catch (IOException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        }    
    }
    
    public static RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        try{
            List<String> rules = getRuleSet(ruleSetName, resourceRepository);
            String forEachIn= null;
            String ruleType= "default";
            String ruleHost= "inMemory";
            
            boolean inMemory = true;
            for (String rule:rules){
                if (rule.contains("$ruleType")){
                    String[] parsed = rule.trim().split(":",2);
                    ruleType=parsed[1];
                    String[] parsedRuleType = ruleType.trim().split(",",3);
                    ruleType= parsedRuleType[0];
                    try {
                        ruleHost= parsedRuleType[1];
                    }
                    catch (Exception e){
                    }
                    //rules.remove(rule);
                    inMemory = ruleHost.equals("inMemory");
                }
                if (rule.contains("$forEachIn")){
                    String[] parsed = rule.trim().split(":",2);
                    forEachIn=parsed[1];
                }
            }
//            LOGGER.debug("loading rules "+ruleSetName);
//            LOGGER.debug(Boolean.toString(inMemory));
//            LOGGER.debug(ruleHost);
//            LOGGER.debug(Integer.toString(rulePort));
            Class<? extends RuleSetImpl> ruleSetClass = new RuleSetMap().get(ruleType);
            RuleSet ruleSet = (RuleSet) ruleSetClass.newInstance();
            ruleSet.setPersist(new MongoDBHelper(ruleSetName.replace("/","-")+"-persist", inMemory));
            
            ruleSet.setRuleType(ruleType);
            Class ruleClass = new RuleTypeMap().get(ruleType);
            ruleSet.setName(ruleSetName);
            ruleSet.setRuleClass(ruleClass);
            ruleSet.setForEachIn(forEachIn);
            ruleSet.loadRules(rules, resourceRepository);
            
            return ruleSet;
        } catch (InstantiationException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        } catch (IllegalAccessException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        }
    }

    @Override
    public void reloadRules(ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        List<String> ruleList = getRuleSet(getName(), resourceRepository);
        if (ruleList.get(0).contains("$ruleType")){
            String rule = ruleList.get(0);
            ruleList.remove(rule);
        }
        loadRules(ruleList, resourceRepository);
    }
    
    /**
     * @return the ruleClass
     */
    @Override
    public Class getRuleClass() {
        return ruleClass;
    }

    /**
     * @param ruleClass the ruleClass to set
     */
    @Override
    public void setRuleClass(Class ruleClass) {
        this.ruleClass = ruleClass;
    }

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");
    
}
