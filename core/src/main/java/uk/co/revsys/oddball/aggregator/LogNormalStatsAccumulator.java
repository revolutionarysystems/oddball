/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                
    @Override
    public void accumulateProperty(Object property){
        if (property!=null){
            // protect against pseudo nulls
            float rawValue = Float.parseFloat(property.toString());
            if (rawValue > 0){ // zero's not ok for a log distribution
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
    }

    @Override
    public Map assessProperty(Object property){
        Map<Object, Object> results = new HashMap<Object, Object>();
        if (property!=null){
            results.put("value", property);
            if (Float.parseFloat(property.toString()) > 0){
                if (nonNulls > 0){
                    results.put("ratioToMin", (Float.parseFloat(property.toString()))/min);
                    results.put("ratioToMax", (Float.parseFloat(property.toString()))/max);
                    results.put("avelog", Float.toString(total/nonNulls));
                    results.put("ratioLogToAveLog", (Math.log10(Float.parseFloat(property.toString()))/(total/nonNulls)));
                }
                if (nonNulls > 1){
                    float var = (sumsquares - (total*total)/nonNulls)/(nonNulls-1);
                    results.put("stdlog", Double.toString(Math.sqrt(var)));
                    results.put("standardisedDeviationLog", ((Math.log10(Float.parseFloat(property.toString())))-(total/nonNulls))/Math.sqrt(var));
                    double dev = (Math.log10(Float.parseFloat(property.toString()))-(total/nonNulls))/Math.sqrt(var);
                    results.put("deviationBand", (int)(Math.signum(dev)*Math.round(Math.abs(dev)-0.49999)));
                }
            }
        }
        return results;
    }

    
    @Override
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

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");
    
}
