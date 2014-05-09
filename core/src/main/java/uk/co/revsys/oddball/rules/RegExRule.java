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
public class RegExRule implements Rule {

    public RegExRule(String regex, String label) {
        this.regex = regex;
        this.label = label;
    }
    
    private String regex;
    
    private String label;

    @Override
    public Assessment apply(Case aCase) {
        String content = ((StringCase)aCase).getContent();
        Pattern p = Pattern.compile(regex);
        boolean success = p.matcher(content).matches();
        if (success){
            return new Assessment(content, regex, label);
        } else {
            return new Assessment(content, regex, null);
        }
    }

    /**
     * @return the regEx
     */
    public String getRegEx() {
        return regex;
    }

    /**
     * @param regEx the regEx to set
     */
    public void setRegEx(String regEx) {
        this.regex = regEx;
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
        return "Rule-"+label+":"+regex;
    }
}
