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
public class BasicStatsAccumulator implements PropertyAccumulator{

    private float min = Float.MAX_VALUE;
    private float max = -Float.MAX_VALUE;
    private float total = 0;
    private int nonNulls = 0;

    public BasicStatsAccumulator() {
        
    }
                
    @Override
    public void accumulateProperty(Object property){
        if (property!=null){
            
            float value = Float.parseFloat(property.toString());
            //float value = (Float)Float.property;
            total+= value;
            nonNulls+=1;
            if (value < min){
                min= value;
            }
            if (value > max){
                max= value;
            }
        }
    }

    @Override
    public Map assessProperty(Object property){
        Map<Object, Object> results = new HashMap<Object, Object>();
        results.put("value", property);
        if (nonNulls > 0){
            results.put("ratioToMin", (Float.parseFloat(property.toString()))/min);
            results.put("ratioToMax", (Float.parseFloat(property.toString()))/max);
            results.put("ratioToAve", (Float.parseFloat(property.toString()))/(total/nonNulls));
        }
        return results;
    }

    
    @Override
    public Map readOffResults(){
        Map<String, String> results = new HashMap<String, String>();
        results.put("nonNulls", Integer.toString(nonNulls));
        results.put("total", Float.toString(total));
        results.put("min", Float.toString(min));
        results.put("max", Float.toString(max));
        if (nonNulls > 0){
            results.put("ave", Float.toString(total/nonNulls));
        }
        return results;
    }

    
}
