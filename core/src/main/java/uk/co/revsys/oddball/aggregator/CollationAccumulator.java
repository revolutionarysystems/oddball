/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class CollationAccumulator implements PropertyAccumulator{
    
    Map<String, Object> collation;

    public CollationAccumulator() {
        this.collation = new HashMap<String, Object>();
    }
                
    @Override
    public void accumulateProperty(String property){
        if (collation.containsKey(property)){
            collation.put(property, ((Integer)collation.get(property))+1);
        } else {
            collation.put(property, 1);
        }
            
    }

    @Override
    public Map readOffResults(){
        Map<String, String> results = new HashMap<String, String>();
        for (String item:collation.keySet()){
            results.put(item, collation.get(item).toString());
        }
        return results;
    }

    
}
