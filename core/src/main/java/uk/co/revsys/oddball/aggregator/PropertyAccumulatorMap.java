/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import uk.co.revsys.oddball.*;
import java.util.HashMap;
import uk.co.revsys.oddball.aggregator.EpisodeAggregator;

/**
 *
 * @author Andrew
 */
    
public class PropertyAccumulatorMap extends HashMap<String, Class>{

	public PropertyAccumulatorMap() {
		put("collate", CollationAccumulator.class);
		put("stats1", BasicStatsAccumulator.class);
	}

}
    
