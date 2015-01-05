/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class CollationAccumulator implements PropertyAccumulator{
    
    Map<Object, Object> collation;

    public CollationAccumulator() {
        this.collation = new HashMap<Object, Object>();
    }
                
    @Override
    public void accumulateProperty(Object property){
        if (property!=null){
            property=((String)property).replace("\"","");
        }
        if (collation.containsKey(property)){
            collation.put(property, ((Integer)collation.get(property))+1);
        } else {
            collation.put(property, 1);
        }
            
    }

    @Override
    public Map assessProperty(Object property){
        Map<Object, Object> results = new HashMap<Object, Object>();
        int totalCount = 0;
        for (Object item:collation.keySet()){
            totalCount+=(Integer)collation.get(item);
        }
        results.put("value", property);
        if (collation.containsKey(property)){
            results.put("count", collation.get(property));
            results.put("proportion", ((Integer)collation.get(property))/(float)totalCount);
        } else {
            results.put("count", 0);
            results.put("proportion", (float)0.0);
        }
        return results;
    }

//    @Override
//    public Map readOffResults(){
//        Map<Object, Object> results = new HashMap<Object, Object>();
//        for (Object item:collation.keySet()){
//            results.put(item, collation.get(item));
//        }
//        return results;
//    }
//
    @Override
    public Map readOffResults(){
        Map<Object, Object> results = new HashMap<Object, Object>();
        ArrayList<Map<String, Object>> innerResults = new ArrayList<Map<String, Object>> ();
        for (Object item:collation.keySet()){
            Map<String, Object> cell = new HashMap<String, Object>();
            cell.put("value", item);
            cell.put("count", collation.get(item));
            innerResults.add(cell);
        }
        results.put("distribution", innerResults);
        return results;
    }

    
}
