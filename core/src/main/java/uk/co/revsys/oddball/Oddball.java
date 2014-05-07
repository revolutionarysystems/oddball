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
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetImpl;
import uk.co.revsys.oddball.util.OddballException;

/**
 *
 * @author Andrew
 */

public class Oddball{

    String resourceLocation;
    HashMap<String, RuleSet> ruleSets = new HashMap<String, RuleSet> ();
    
    public Oddball(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public Opinion assessCase(String ruleSetName, Case aCase)throws OddballException{
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet == null){
            ruleSet = loadRuleSet(ruleSetName, resourceLocation);
            ruleSets.put(ruleSetName, ruleSet);
        }
        return ruleSet.assessCase(aCase);
        
    }
    
    private RuleSet loadRuleSet(String ruleSetName, String resourceLocation)throws OddballException{
        try{
            return new RuleSetImpl("dummy");
        // load using resource-repository
        }
        catch (Exception e){
            throw new OddballException("No Rule Set named "+ruleSetName+" in "+resourceLocation);
        }
    }
    
}
