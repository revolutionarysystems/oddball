/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.regex.Pattern;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class RegExRule implements Rule {

    public RegExRule() {
    }
    
    public RegExRule(String ruleString, String label) {
        this.ruleString = ruleString;
        this.label = label;
    }
    
    private String ruleString;
    
    private String label;

    @Override
    public Assessment apply(Case aCase, RuleSet ruleSet, String key) {
        String content = ((StringCase)aCase).getContent();
        Pattern p = Pattern.compile(ruleString);
        boolean success = p.matcher(content).matches();
        if (success){
            return new Assessment(content, ruleString, label);
        } else {
            return new Assessment(content, ruleString, null);
        }
    }

    /**
     * @return the ruleString
     */
    public String getRuleString() {
        return ruleString;
    }

    /**
     * @param ruleString the ruleString to set
     */
    public void setRuleString(String ruleString, ResourceRepository resourceRepository) throws RuleNotLoadedException{
        this.ruleString = ruleString;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
    

    @Override
    public String toString(){
        return "Rule-"+label+":"+ruleString;
    }
}
