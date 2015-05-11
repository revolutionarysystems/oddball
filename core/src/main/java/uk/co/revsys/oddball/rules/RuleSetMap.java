/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.HashMap;
import uk.co.revsys.oddball.rules.MongoRuleSet;
import uk.co.revsys.oddball.rules.RuleSetImpl;

/**
 *
 * @author Andrew
 */
    
public class RuleSetMap extends HashMap<String, Class<? extends RuleSetImpl>>{

	public RuleSetMap() {
		put("default", RuleSetImpl.class);
		put("regex", RuleSetImpl.class);
		put("mongo", MongoRuleSet.class);
	}

}
    
