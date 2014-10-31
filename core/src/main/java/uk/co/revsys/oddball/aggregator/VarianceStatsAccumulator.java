/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.HashMap;
import java.util.Map;
import uk.co.revsys.oddball.util.JSONUtil;

/**
 *
 * @author Andrew
 */
public class VarianceStatsAccumulator implements PropertyAccumulator{

    private float min = Float.MAX_VALUE;
    private float max = Float.MIN_VALUE;
    private float total = 0;
    private float sumsquares = 0;
    private int nonNulls = 0;
    public VarianceStatsAccumulator() {
        
    }
                
    public void accumulateProperty(String property){
        if (property!=null){
            float value = Float.parseFloat(property);
            total+= value;
            sumsquares+=value * value;
            nonNulls+=1;
            if (value < min){
                min= value;
            }
            if (value > max){
                max= value;
            }
        }
    }

    public Map readOffResults(){
        Map<String, String> results = new HashMap<String, String>();
        results.put("nonNulls", Integer.toString(nonNulls));
        results.put("total", Float.toString(total));
        results.put("sumsquares", Float.toString(sumsquares));
        results.put("min", Float.toString(min));
        results.put("max", Float.toString(max));
        if (nonNulls > 0){
            results.put("ave", Float.toString(total/nonNulls));
        }
        if (nonNulls > 1){
            float var = (sumsquares - (total*total)/nonNulls)/(nonNulls-1);
            results.put("var", Float.toString(var));
            results.put("std", Double.toString(Math.sqrt(var)));
        }
        return results;
    }

    
}
