/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.rules;

import java.io.IOException;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.MapCase;

/**
 *
 * @author Andrew
 */
public class MongoRuleSet extends RuleSetImpl {

    public MongoRuleSet() {
        super();
        setAssess(new MongoDBHelper("oddball-assess", true));
    }

    private MongoDBHelper assess;

    public MongoDBHelper getAssess() {
        return assess;
    }

    public final void setAssess(MongoDBHelper assess) {
        this.assess = assess;
    }

    public MongoRuleSet(String name, boolean inMemory) {
        super(name, inMemory);
        setAssess(new MongoDBHelper("oddball-assess", true));
    }

    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr, int persistOption, String duplicateQuery, String avoidQuery, String forEachIn) throws InvalidCaseException, IOException {
        String caseStr = aCase.getContent();
        Case theCase = new MapCase(caseStr);
        if (forEachIn == null) {  //lowest level of nesting
            String caseId = assess.insertCase(caseStr);
            Opinion op = super.assessCase(theCase, caseId, ruleSetStr, persistOption, duplicateQuery, avoidQuery, forEachIn);
            assess.removeCase(caseId);
            return op;
        } else {
            Opinion op = super.assessCase(theCase, "", ruleSetStr, persistOption, duplicateQuery, avoidQuery, forEachIn);
            return op;
        }
    }

}
