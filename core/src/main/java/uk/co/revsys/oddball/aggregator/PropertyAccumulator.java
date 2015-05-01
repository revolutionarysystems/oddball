/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.Map;

/**
 *
 * @author Andrew
 */
public interface PropertyAccumulator {

    public void accumulateProperty(Object property);

    public Map assessProperty(Object property);
    
    public Map readOffResults();
    
    public void setOptions(Map<String, String> options);
    
}
