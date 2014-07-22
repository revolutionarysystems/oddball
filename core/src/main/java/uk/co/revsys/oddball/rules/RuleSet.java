/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.resource.repository.ResourceRepository;

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
    
    public void setRuleClass(Class ruleClass);

    public void setName(String name);
    
    public void loadRules(List<String> rules, ResourceRepository resourceRepository) throws RuleSetNotLoadedException;

    public void reloadRules(ResourceRepository resourceRepository) throws RuleSetNotLoadedException;

    
}
