/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.RuleSetMap;
import uk.co.revsys.oddball.RuleTypeMap;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class RuleSetImpl implements RuleSet{    

    public RuleSetImpl(){
    }
    
    public RuleSetImpl(String name, boolean inMemory, String host, int port) throws UnknownHostException {
        setPersist(new MongoDBHelper(name+"-persist", inMemory, host, port));
        this.name = name;
    }

    Set<Rule> rules = new HashSet<Rule>();
    Set<Rule> extraRules = new HashSet<Rule>();
    Set<String> prefixes = new HashSet<String>();

    private String name;
    private String ruleType;
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
    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }

    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr, int persistOption, String duplicateQuery) throws InvalidCaseException{
        
        Opinion op = new OpinionImpl();

        
        for (Rule rule: rules){
            Assessment as = rule.apply(aCase, this, key);
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
        for (String prefix:prefixes){
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
                op.getTags().add(prefix+"odDball");
            }
        }
        if (op.getLabel().indexOf("*ignore*")>=0){
            op.getTags().clear();
        } else {
            if (persistOption != NEVERPERSIST){
                String persistCase = op.getEnrichedCase(ruleSetStr, aCase);
                if (persistOption == UPDATEPERSIST){
                    String id = getPersist().checkAlreadyExists(duplicateQuery);
                    while (id != null){
                        LOGGER.debug("removing "+id);
                        getPersist().removeCase(id);
                        id = getPersist().checkAlreadyExists(duplicateQuery);
                    }
                }
                getPersist().insertCase(persistCase);
                LOGGER.debug("inserting:"+duplicateQuery);
            }
        }
        return op;
    }

   
    public Set<Rule> getRules() {
        Set<Rule> result = rules;
        return result;
    }

    public Set<Rule> getAllRules() {
        Set<Rule> result = new HashSet<Rule>();
        result.addAll(rules);
        result.addAll(extraRules);
        return result;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the ruleType
     */
    public String getRuleType() {
        return ruleType;
    }

    /**
     * @param ruleType the ruleType to set
     */
    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
    
    public MongoDBHelper getPersist() {
        return persist;
    }

    public void setPersist(MongoDBHelper persist) {
        this.persist = persist;
    }

    public Rule createRule(String prefix, String label, String ruleString, String source, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        try{
            Rule ruleInstance = (Rule) ruleClass.newInstance();
            ruleInstance.setLabel(prefix+label);
            ruleInstance.setRuleString(ruleString, resourceRepository) ;
            ruleInstance.setSource(source) ;
            return ruleInstance;
        } catch (InstantiationException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        } catch (IllegalAccessException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        } catch (RuleNotLoadedException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        }
        
    }
    
    public void loadRules(List<String> rules, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        this.getRules().clear();
        this.extraRules.clear();
        this.prefixes.clear();
        String prefix = "";
        for (String rule : rules){
            String trimRule = rule.trim();
            if ((trimRule.indexOf("#")!=0)&&(trimRule.indexOf("$")!=0)&&(!trimRule.equals(""))){
                if (Pattern.matches("\\[.*\\]", trimRule)){
                    prefix = trimRule.substring(1, trimRule.length()-1)+".";
                    if (prefix.equals("other.")){  // prefix heading "[other]" counts as no prefix at all
                        prefix="";
                    } else {
                        this.addPrefix(prefix);
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
                    Rule ruleInstance = createRule(prefix, label, parsed[1], source, resourceRepository);
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
        try{
            List<Resource> resources = resourceRepository.listResources(".");
            List<String> rules = new ArrayList<String>();
            boolean rulesetFound = false;
            for (Resource resource : resources){
                if (resource.getName().equals(ruleSetName)){
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
    
    public static RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository, String defaultDataStoreHost, String defaultDataStorePort) throws RuleSetNotLoadedException{
        try{
            List<String> rules = getRuleSet(ruleSetName, resourceRepository);
            String ruleType= "default";
            String ruleHost= "inMemory";
            int rulePort= 0;
            
            boolean inMemory = true;
            if (rules.get(0).contains("$ruleType")){
                String rule = rules.get(0);
                String[] parsed = rule.trim().split(":",2);
                ruleType=parsed[1];
                String[] parsedRuleType = ruleType.trim().split(",",3);
                ruleType= parsedRuleType[0];
                try {
                    ruleHost= parsedRuleType[1];
//                    String rulePortStr= parsedRuleType[2];
//                    rulePort = Integer.parseInt(rulePortStr);
                }
                catch (Exception e){
                }
                rules.remove(rule);
                if (ruleHost.equals("inMemory")){
                    inMemory = true;
                } else {
                    inMemory = false;
                    ruleHost = defaultDataStoreHost;
                    rulePort = Integer.parseInt(defaultDataStorePort);
                }
                
            }
//            LOGGER.debug("loading rules "+ruleSetName);
//            LOGGER.debug(Boolean.toString(inMemory));
//            LOGGER.debug(ruleHost);
//            LOGGER.debug(Integer.toString(rulePort));
            Class<? extends RuleSetImpl> ruleSetClass = new RuleSetMap().get(ruleType);
            RuleSet ruleSet = (RuleSet) ruleSetClass.newInstance();
            ruleSet.setPersist(new MongoDBHelper(ruleSetName+"-persist", inMemory, ruleHost, rulePort));
            
            ruleSet.setRuleType(ruleType);
            Class ruleClass = new RuleTypeMap().get(ruleType);
            ruleSet.setName(ruleSetName);
            ruleSet.setRuleClass(ruleClass);
            ruleSet.loadRules(rules, resourceRepository);
            
            return ruleSet;
        } catch (InstantiationException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        } catch (IllegalAccessException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        } catch (UnknownHostException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        }
    }

    public void reloadRules(ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        List<String> rules = getRuleSet(getName(), resourceRepository);
        if (rules.get(0).contains("$ruleType")){
            String rule = rules.get(0);
            rules.remove(rule);
        }
        loadRules(rules, resourceRepository);
    }
    
    /**
     * @return the ruleClass
     */
    public Class getRuleClass() {
        return ruleClass;
    }

    /**
     * @param ruleClass the ruleClass to set
     */
    public void setRuleClass(Class ruleClass) {
        this.ruleClass = ruleClass;
    }

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");
    
}
