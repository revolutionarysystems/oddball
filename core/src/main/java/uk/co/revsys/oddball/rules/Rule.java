/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.util.OddballException;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public interface Rule {
    
    public Assessment apply(Case aCase, RuleSet ruleSet, String key);
    
    /**
     * @return the ruleString
     */
    public String getRuleString();
    
    /**
     * @param ruleString the ruleString to set
     */
    public void setRuleString(String ruleString, ResourceRepository resourceRepository)throws OddballException;

    /**
     * @return the label
     */
    public String getLabel();
    
    /**
     * @param label the label to set
     */
    public void setLabel(String label);

}
