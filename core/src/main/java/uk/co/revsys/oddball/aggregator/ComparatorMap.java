/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.HashMap;

/**
 *
 * @author Andrew
 */
    
public class ComparatorMap extends HashMap<String, Class>{

	public ComparatorMap() {
		put("summary", SummaryComparator.class);
	}

}
    
