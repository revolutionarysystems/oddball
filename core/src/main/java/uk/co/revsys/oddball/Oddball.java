/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.Rule;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetImpl;
import uk.co.revsys.oddball.util.OddballException;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.RepositoryItem;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */

public class Oddball{

    ResourceRepository resourceRepository;
    HashMap<String, RuleSet> ruleSets = new HashMap<String, RuleSet> ();
    
    public Oddball(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Opinion assessCase(String ruleSetName, Case aCase)throws OddballException{
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet == null){
            ruleSet = loadRuleSet(ruleSetName, resourceRepository);
            ruleSets.put(ruleSetName, ruleSet);
        }
        try {
            return ruleSet.assessCase(aCase, null, ruleSetName);
        } catch (IOException ex){
            ex.printStackTrace();
            throw new OddballException();
        }
        
    }

    public void clearRuleSet(String ruleSetName)throws OddballException{
        try{
            RuleSet ruleSet = ruleSets.get(ruleSetName);
            if (ruleSet != null) {
                ruleSets.remove(ruleSetName);
            }
        }
        catch (Exception e){
            throw new OddballException("No Rule Set named "+ruleSetName+" in repository");
        }
    }
    
    private RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository)throws OddballException{
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
            throw new OddballException("No Rule Set named "+ruleSetName+" in repository");
        }
        catch (Exception e){
            e.printStackTrace();
            throw new OddballException("Rules could not be loaded");
        }
    }

    public Iterable<String> findCases(String ruleSetName)throws OddballException{
        try{
            RuleSet ruleSet = ruleSets.get(ruleSetName);
            return ruleSet.getPersist().findCases();
        } catch (IOException ex){
            ex.printStackTrace();
            throw new OddballException();
        }
    }
    
    public Iterable<String> findCases(String ruleSetName, String query)throws OddballException{
        try{
            RuleSet ruleSet = ruleSets.get(ruleSetName);
            return ruleSet.getPersist().findCases(query);
        } catch (IOException ex){
            ex.printStackTrace();
            throw new OddballException();
        }
    }
    
    public Iterable<String> findDistinct(String ruleSetName, String field)throws OddballException{
        try{
            RuleSet ruleSet = ruleSets.get(ruleSetName);
            return ruleSet.getPersist().findDistinct(field);
        } catch (IOException ex){
            ex.printStackTrace();
            throw new OddballException();
        }
    }
    
    
}
