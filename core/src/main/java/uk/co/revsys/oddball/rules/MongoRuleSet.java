/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.StringCase;

/**
 *
 * @author Andrew
 */
public class MongoRuleSet extends RuleSetImpl{

    public MongoRuleSet() {
        setHelper(new MongoDBHelper());
    }

    private MongoDBHelper helper;

    /**
     * @return the context
     */
    public MongoDBHelper getHelper() {
        return helper;
    }

    /**
     * @param context the context to set
     */
    public void setHelper(MongoDBHelper helper) {
        this.helper = helper;
    }

    public MongoRuleSet(String name) {
        super(name);
        setHelper(new MongoDBHelper());
    }
    
    @Override
    public Opinion assessCase(Case aCase, String key) {
        String caseId = helper.insertCase(((StringCase)aCase).getContent());
        Opinion op = super.assessCase(aCase, caseId);
        helper.removeCase(caseId);
        return op;
    }

    
}
