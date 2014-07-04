/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.util.Map;
import org.jongo.FindOne;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.oddball.cases.StringCase;

/**
 *
 * @author Andrew
 */
public class MongoRuleSet extends RuleSetImpl{

    public MongoRuleSet() {
        super();
        setAssess(new MongoDBHelper("oddball-assess"));
    }

    private MongoDBHelper assess;

    public MongoDBHelper getAssess() {
        return assess;
    }

    public void setAssess(MongoDBHelper assess) {
        this.assess = assess;
    }

    public MongoRuleSet(String name) {
        super(name);
        setAssess(new MongoDBHelper(name+"-assess"));
    }
    
    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr) throws InvalidCaseException{
        String caseStr = aCase.getContent();
        Case theCase = new MapCase(caseStr);
        String caseId = assess.insertCase(caseStr);
        Opinion op = super.assessCase(theCase, caseId, ruleSetStr);
        String persistCase = op.getEnrichedCase(ruleSetStr, theCase);
        getPersist().insertCase(persistCase);
        assess.removeCase(caseId);
        return op;
    }

    
}
