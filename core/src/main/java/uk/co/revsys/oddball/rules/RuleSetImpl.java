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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.RuleSetMap;
import uk.co.revsys.oddball.RuleTypeMap;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.util.OddballException;
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

    private String name;
    private String ruleType;
   private MongoDBHelper persist;


    @Override
    public void addRule(Rule rule) {
        rules.add(rule);
    }

    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr) throws IOException{
        
        Opinion op = new OpinionImpl();

        
        for (Rule rule: rules){
            Assessment as = rule.apply(aCase, this, key);
            op.incorporate(as);
        }
        if (op.getTags().isEmpty()){
            op.getTags().add("*odDball*");
        }
        return op;
    }

   
    public Set<Rule> getRules() {
        return rules;
    }

    public String getName() {
        return name;
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

    public static RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository)throws OddballException{
        try{
            Resource resource = new Resource("", ruleSetName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> rules = IOUtils.readLines(inputStream);
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
            for (String rule : rules){
                String[] parsed = rule.trim().split(":",2);
                Rule ruleInstance = (Rule) ruleClass.newInstance();
                ruleInstance.setLabel(parsed[0]);
                ruleInstance.setRuleString(parsed[1], resourceRepository);
                ruleSet.addRule(ruleInstance);
            }
            return ruleSet;
        }
        catch (java.io.FileNotFoundException e){
            throw new OddballException("No Rule Set named "+ruleSetName+" in repository", e);
        } catch (IOException ex) {
            throw new OddballException("Unable to load Rule Set " + ruleSetName + ": " + ex.getMessage());
        } catch (InstantiationException ex) {
            throw new OddballException("Unable to load Rule Set " + ruleSetName + ": " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new OddballException("Unable to load Rule Set " + ruleSetName + ": " + ex.getMessage());
        }
    }
    
    
}
