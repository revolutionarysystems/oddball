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
    
public class PropertyAccumulatorMap extends HashMap<String, Class>{

	public PropertyAccumulatorMap() {
		put("collate", CollationAccumulator.class);
		put("stats1", BasicStatsAccumulator.class);
		put("stats2", VarianceStatsAccumulator.class);
		put("lognormal", LogNormalStatsAccumulator.class);
	}

}
    
