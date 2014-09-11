/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public abstract class RuleImpl implements Rule {

    public RuleImpl() {
    }
    
    public RuleImpl(String ruleString, String label) {
        this.ruleString = ruleString;
        this.label = label;
    }
    
    protected String ruleString;

    protected String source;
    
    protected String label;

    @Override
    public abstract Assessment apply(Case aCase, RuleSet ruleSet, String key);
    
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
    

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }
    

    @Override
    public String toString(){
        return "Rule-"+label+":"+ruleString;
    }

    
    @Override
    public String asJSON()throws IOException{
        HashMap mapRule = new HashMap();
        mapRule.put("label", this.label);
        mapRule.put("ruleString", this.ruleString.replace("\"","\\\""));
        mapRule.put("source", this.source);
        return JSONUtil.map2json(mapRule);
    }

    @Override
    public String asRuleConfig()throws IOException{
        StringBuilder rule = new StringBuilder("");
        System.out.println(getLabel());
        String prefix = this.getPrefix();
        System.out.println(prefix);
        if (prefix !=null){
            rule.append("["+prefix+"]\n");
        } else {
            rule.append("[other]\n");
        }
        rule.append(getLabelOnly());
        rule.append(";");
        rule.append(this.getSource());
        rule.append(":");
        rule.append(getRuleString());
        rule.append("\n");
        return rule.toString();
    }

    private String getPrefix(){
        if (getLabel().indexOf(".")>0){
            String[] labelParts=getLabel().split("\\.", 2);
            return labelParts[0];
        } else {
            return null;
        }
    }
    
    private String getLabelOnly(){
        if (getLabel().indexOf(".")>0){
            String[] labelParts=getLabel().split("\\.", 2);
            return labelParts[1];
        } else {
            return label;
        }
    }
    
    
}