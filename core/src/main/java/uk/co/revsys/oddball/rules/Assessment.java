/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

/**
 *
 * @author Andrew
 */
public class Assessment{

    private String caseStr;
    private String ruleStr;
    private String labelStr;

    public Assessment(String caseStr, String ruleStr, String labelStr) {
        this.caseStr = caseStr;
        this.ruleStr = ruleStr;
        this.labelStr = labelStr;
    }

    /**
     * @return the caseStr
     */
    public String getCaseStr() {
        return caseStr;
    }

    /**
     * @param caseStr the caseStr to set
     */
    public void setCaseStr(String caseStr) {
        this.caseStr = caseStr;
    }

    /**
     * @return the ruleStr
     */
    public String getRuleStr() {
        return ruleStr;
    }

    /**
     * @param ruleStr the ruleStr to set
     */
    public void setRuleStr(String ruleStr) {
        this.ruleStr = ruleStr;
    }

    /**
     * @return the labelStr
     */
    public String getLabelStr() {
        return labelStr;
    }

    /**
     * @param labelStr the labelStr to set
     */
    public void setLabelStr(String labelStr) {
        this.labelStr = labelStr;
    }
    
}
