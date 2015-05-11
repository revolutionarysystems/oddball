/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.HashMap;
import uk.co.revsys.oddball.rules.MongoRule;
import uk.co.revsys.oddball.rules.RegExRule;

/**
 *
 * @author Andrew
 */
    
public class RuleTypeMap extends HashMap<String, Class<?>>{

	public RuleTypeMap() {
		put("default", RegExRule.class);
		put("regex", RegExRule.class);
		put("mongo", MongoRule.class);
	}

}
    
