/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.regex.Pattern;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.StringCase;

/**
 *
 * @author Andrew
 */
public class RegExRule extends RuleImpl {

    public RegExRule() {
    }
    
    public RegExRule(String ruleString, String label) {
        super(ruleString, label);
    }

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

}
