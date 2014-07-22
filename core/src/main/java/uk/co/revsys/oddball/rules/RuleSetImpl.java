/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.io.InputStream;
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
    

    public RuleSetImpl() {
        setPersist(new MongoDBHelper("oddball-persist"));
    }
    
    public RuleSetImpl(String name) {
        setPersist(new MongoDBHelper(name+"-persist"));
        this.name = name;
    }

    Set<Rule> rules = new HashSet<Rule>();
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
    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }

    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr) throws InvalidCaseException{
        
        Opinion op = new OpinionImpl();

        
        for (Rule rule: rules){
            Assessment as = rule.apply(aCase, this, key);
            op.incorporate(as);
        }
        if (op.getTags().isEmpty()){
            op.getTags().add("*odDball*");
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
                op.getTags().add(prefix+"odDball");
            }
        }
        return op;
    }

   
    public Set<Rule> getRules() {
        return rules;
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

    public void loadRules(List<String> rules, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        this.getRules().clear();
        this.prefixes.clear();
        try{
            String prefix = "";
            for (String rule : rules){
                String trimRule = rule.trim();
                System.out.println(trimRule);
                if ((trimRule.indexOf("#")!=0)&&(!trimRule.equals(""))){
                    if (Pattern.matches("\\[.*\\]", trimRule)){
                        System.out.println("=prefix");
                        prefix = trimRule.substring(1, trimRule.length()-1)+".";
                        System.out.println(prefix);
                        if (prefix.equals("other.")){  // prefix heading "[other]" counts as no prefix at all
                            prefix="";
                        } else {
                            this.addPrefix(prefix);
                        }
                    }else if (Pattern.matches(".*:.*", trimRule)){
                        String[] parsed = trimRule.split(":",2);
                        Rule ruleInstance = (Rule) ruleClass.newInstance();
                        ruleInstance.setLabel(prefix+parsed[0]);
                        ruleInstance.setRuleString(parsed[1], resourceRepository) ;
                        System.out.println(ruleInstance.getLabel());
                        this.addRule(ruleInstance);
                    }
                }
            }
        } catch (InstantiationException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        } catch (IllegalAccessException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        } catch (RuleNotLoadedException ex) {
            throw new RuleSetNotLoadedException(name, ex);
        }
    }
    
    public static List<String> getRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        try{
            Resource resource = new Resource("", ruleSetName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> rules = IOUtils.readLines(inputStream);
            return rules;
        }
        catch (IOException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        }    
    }
    
    public static RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        try{
            List<String> rules = getRuleSet(ruleSetName, resourceRepository);
            String ruleType= "default";
            if (rules.get(0).contains("$ruleType")){
                String rule = rules.get(0);
                String[] parsed = rule.trim().split(":",2);
                ruleType=parsed[1];
                rules.remove(rule);
            }
            Class<? extends RuleSetImpl> ruleSetClass = new RuleSetMap().get(ruleType);
            RuleSet ruleSet = (RuleSet) ruleSetClass.newInstance();
            ruleSet.setRuleType(ruleType);
            Class ruleClass = new RuleTypeMap().get(ruleType);
            ruleSet.setName(ruleSetName);
            ruleSet.setRuleClass(ruleClass);
            System.out.println("Loading rules:"+ruleSetName);
            ruleSet.loadRules(rules, resourceRepository);
            
            return ruleSet;
        } catch (InstantiationException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        } catch (IllegalAccessException ex) {
            throw new RuleSetNotLoadedException(ruleSetName, ex);
        }
    }

    public void reloadRules(ResourceRepository resourceRepository) throws RuleSetNotLoadedException{
        LOGGER.debug("Reloading rules for "+getName());
        for (Rule rule : this.getRules()){
            LOGGER.debug(rule.getRuleString());
        }
        List<String> rules = getRuleSet(getName(), resourceRepository);
        if (rules.get(0).contains("$ruleType")){
            String rule = rules.get(0);
            rules.remove(rule);
        }
        LOGGER.debug("Rules found for "+getName());
        for (String line : rules){
            LOGGER.debug(line);
        }
        loadRules(rules, resourceRepository);
        LOGGER.debug("Rules loaded for "+getName());
        for (Rule rule : this.getRules()){
            LOGGER.debug(rule.getRuleString());
        }
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
