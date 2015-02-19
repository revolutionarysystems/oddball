/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.identifier;

import uk.co.revsys.oddball.aggregator.*;
import uk.co.revsys.oddball.*;
import java.util.HashMap;
import uk.co.revsys.oddball.aggregator.EpisodeAggregator;
import uk.co.revsys.oddball.aggregator.SummaryAggregator;

/**
 *
 * @author Andrew
 */
    
public class IdentifierMap extends HashMap<String, Class>{

	public IdentifierMap() {
		put("summary", SummaryIdentifier.class);
	}

}
    
