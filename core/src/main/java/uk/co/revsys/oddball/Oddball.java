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
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RegExRule;
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
        return ruleSet.assessCase(aCase);
        
    }
    
    private RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository)throws OddballException{
        try{
            RuleSet ruleSet = new RuleSetImpl("ruleSetName");
            Resource resource = new Resource("", ruleSetName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> rules = IOUtils.readLines(inputStream);
            for (String rule : rules){
                String[] parsed = rule.split(":",2);
                ruleSet.addRule(new RegExRule(parsed[1],parsed[0]));
            }
            return ruleSet;
        }
        catch (Exception e){
            throw new OddballException("No Rule Set named "+ruleSetName+" in repository");
        }
    }
    
}
