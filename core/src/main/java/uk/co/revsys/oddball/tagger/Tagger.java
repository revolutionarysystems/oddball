/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.OwnerMissingException;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RuleSet;

/**
 *
 * @author Andrew
 */
public class Tagger {

    public Tagger(String ruleSetName, RuleSet ruleSet) {
        this.ruleSetName = ruleSetName;
        this.ruleSet = ruleSet;
    }
    
    public String tagCase(Case aCase, Map<String, String> options, int persistOption, String duplicateQuery, String avoidQuery, RuleSet overridePersistRuleSet) throws OwnerMissingException, InvalidCaseException, IOException{
        if (options.containsKey("owner")) {
            aCase.ensureOwner(options.get("owner"));
        }
        if (aCase.getOwner() == null) {
            throw new OwnerMissingException(getRuleSetName());
        }
        Opinion opinion = tagCaseOpinion(aCase, options, persistOption, duplicateQuery, avoidQuery, overridePersistRuleSet);
        String enrichedCase = "";
        boolean retag = false;
        if (options.containsKey("retag")&&options.get("retag").equals("true")){
            retag = true;
        }
        enrichedCase = opinion.getEnrichedCase(getRuleSetName(), aCase, false, null, retag);
        return enrichedCase;
    }

        
    public Opinion tagCaseOpinion(Case aCase, Map<String, String> options, int persistOption, String duplicateQuery, String avoidQuery, RuleSet overridePersistRuleSet) throws OwnerMissingException, InvalidCaseException, IOException{
        Collection<String> results = new ArrayList<String>();
        boolean retag = false;
        if (options.containsKey("retag")&&options.get("retag").equals("true")){
            retag = true;
        }
        return getRuleSet().assessCase(aCase, null, getRuleSetName(), persistOption, duplicateQuery, avoidQuery, overridePersistRuleSet, retag);
    }
    
    private RuleSet ruleSet;
    private String ruleSetName;
    
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

    /**
     * @return the ruleSet
     */
    public RuleSet getRuleSet() {
        return ruleSet;
    }

    /**
     * @param ruleSet the ruleSet to set
     */
    public void setRuleSet(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    /**
     * @return the ruleSetName
     */
    public String getRuleSetName() {
        return ruleSetName;
    }

    /**
     * @param ruleSetName the ruleSetName to set
     */
    public void setRuleSetName(String ruleSetName) {
        this.ruleSetName = ruleSetName;
    }


}
