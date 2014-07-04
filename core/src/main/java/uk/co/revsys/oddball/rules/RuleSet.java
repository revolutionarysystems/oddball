/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.util.Set;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;

/**
 *
 * @author Andrew
 */
public interface RuleSet {
    
    public void addRule(Rule rule);
    public void addPrefix(String prefix);
    
    public Set<Rule> getRules();
    
    public Opinion assessCase(Case aCase, String key, String ruleSetStr) throws InvalidCaseException;
    
    public String getRuleType();

    public void setRuleType(String ruleType);
    
    public MongoDBHelper getPersist();
    
}
