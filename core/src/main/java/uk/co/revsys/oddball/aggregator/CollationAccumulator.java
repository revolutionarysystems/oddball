/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class CollationAccumulator implements PropertyAccumulator, Comparator{
    
    Map<Object, Object> collation;

    public CollationAccumulator() {
        this.collation = new HashMap<Object, Object>();
    }
                
    public int compare(Object o1, Object o2){
        return ((Integer)((HashMap)o2).get("count"))-((Integer)((HashMap)o1).get("count"));
//        return ((String)((HashMap)o2).get("value")).compareTo((String)((HashMap)o1).get("value"));
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
        int totalCount= 0;
        double totalInfo= 0;
        for (Object item:collation.keySet()){
            Map<String, Object> cell = new HashMap<String, Object>();
            cell.put("value", item);
            cell.put("count", collation.get(item));
            totalCount+=(Integer)collation.get(item);
            innerResults.add(cell);
        }
        Collections.sort(innerResults, this);
        if (innerResults.size()>0){
            int maxCount = (Integer) innerResults.get(0).get("count");
            for (Map<String, Object> entry: innerResults){
                entry.put("relFreq", ((Integer)entry.get("count")*1.0)/maxCount);
                double absFreq = ((Integer)entry.get("count")*1.0)/totalCount;
                entry.put("absFreq", absFreq);
                double info = -Math.log(absFreq)/Math.log(2) * absFreq;
                totalInfo+=info;
                entry.put("info", -Math.log(absFreq)/Math.log(2));
            }
        }
        results.put("distribution", innerResults);
        results.put("information", totalInfo);
        return results;
    }

    
}
