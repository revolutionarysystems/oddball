/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

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

    public void addExtraRule(Rule rule);
    
    public Rule createRule(String prefix, String label, String ruleString, String comment, String source, ResourceRepository resourceRepository) throws RuleSetNotLoadedException;

    public Rule findExtraRule(String prefix, String ruleString);
    
    public void removeExtraRule(Rule rule);

    public void addPrefix(String prefix, String defaultValue);
    
    public List<Rule> getRules();
        
    public List<Rule> getAllRules();
        
    public Opinion assessCase(Case aCase, String key, String ruleSetStr, int persistOption, String duplicateQuery, String forEachIn) throws InvalidCaseException;

    public Opinion assessCase(Case aCase, String key, String ruleSetStr, int persistOption, String duplicateQuery) throws InvalidCaseException;
    
    public String getRuleType();

    public void setRuleType(String ruleType);
    
    public MongoDBHelper getPersist();
    
    public Class getRuleClass();
    
    public void setRuleClass(Class ruleClass);

    public String getForEachIn();
    
    public void setForEachIn(String forEachIn);

    public String getName();

    public void setName(String name);
    
    public void loadRules(List<String> rules, ResourceRepository resourceRepository) throws RuleSetNotLoadedException;

    public void reloadRules(ResourceRepository resourceRepository) throws RuleSetNotLoadedException;

    public void setPersist(MongoDBHelper persist);
    
    public static final int NEVERPERSIST=0;
    public static final int ALWAYSPERSIST=1;
    public static final int UPDATEPERSIST=2;
    

}
