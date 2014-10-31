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
public class LogNormalStatsAccumulator implements PropertyAccumulator{

    private float min = Float.MAX_VALUE;
    private float max = Float.MIN_VALUE;
    private float total = 0;
    private float sumsquares = 0;
    private int nonNulls = 0;
    public LogNormalStatsAccumulator() {
        
    }
                
    public void accumulateProperty(String property){
        if (property!=null){
            float rawValue = Float.parseFloat(property);
            float value = (float) Math.log10(rawValue);
            total+= value;
            sumsquares+=value * value;
            nonNulls+=1;
            if (rawValue < min){
                min= rawValue;
            }
            if (rawValue > max){
                max= rawValue;
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
            results.put("avelog", Float.toString(total/nonNulls));
            results.put("centre", Double.toString(Math.pow(10.0, total/nonNulls)));
        }
        if (nonNulls > 1){
            double var = (sumsquares - (total*total)/nonNulls)/(nonNulls-1);
            double std = Math.sqrt(var);
            results.put("varlog", Double.toString(var));
            results.put("stdlog", Double.toString(std));
            results.put("lowMargin", Double.toString(Math.pow(10.0, total/nonNulls-std)));
            results.put("highMargin", Double.toString(Math.pow(10.0, total/nonNulls+std)));
        }
        return results;

    }

    
}
