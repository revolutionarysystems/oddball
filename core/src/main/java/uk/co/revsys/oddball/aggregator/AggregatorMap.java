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
    
public class AggregatorMap extends HashMap<String, Class>{

	public AggregatorMap() {
		put("episode", EpisodeAggregator.class);
		put("summary", SummaryAggregator.class);
	}

}
    
