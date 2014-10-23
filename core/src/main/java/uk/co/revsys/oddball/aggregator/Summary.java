/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class Summary {

    public Summary(String owner, long startTime, long duration, SummaryDefinition summaryDefinition) throws AccumulationException{
        this.owner = owner;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = startTime+duration;
        this.firstTagTime = firstTagTime;
        this.summaryDefinition = summaryDefinition;
        this.accumulators = new HashMap<String, PropertyAccumulator>();
        this.properties= new HashMap<String, String>();
        Collection<Object> definition = (Collection<Object>) summaryDefinition.getDefinition().get("summary");
        //for each analysis property in summary definition
        for (Object accumulator : definition){
            Map<String, Object> accMap = (Map)accumulator;
            //set up an accumulator object, initialised
            Class accumulatorClass = new PropertyAccumulatorMap().get(accMap.get("treatment"));
            try {
                PropertyAccumulator ac = (PropertyAccumulator) accumulatorClass.newInstance();
                accumulators.put((String)accMap.get("name"), ac);
                properties.put((String)accMap.get("name"), (String) accMap.get("source"));
            } catch (InstantiationException e) {
                throw new AccumulationException("Could not instantiate accumulator: " + accMap.get("treatment"), e);
            } catch (IllegalAccessException e) {
                throw new AccumulationException("Could not instantiate accumulator: " + accMap.get("treatment"), e);
            }
            
        }
        
    }
    
    private String owner;
    private SummaryDefinition summaryDefinition;
    private long startTime;
    private long endTime;
    private long firstTagTime;
    private long lastTagTime;
    private long duration;
    private Map<String, PropertyAccumulator> accumulators;
    private Map<String, String> properties;
    

    public void incorporate(Map<String, Object> caseMap){
        for (String accName : accumulators.keySet()){
            String propertyValue = null;
            String propertyPath = properties.get(accName);
            Map subMap = caseMap;
            while (propertyPath.contains(".")&& subMap!=null){
//                System.out.println(subMap);
//                System.out.println(propertyPath);
                String key = propertyPath.substring(0, propertyPath.indexOf("."));
//                System.out.println("key="+key);
                subMap = (Map) subMap.get(key);
                propertyPath = propertyPath.substring(propertyPath.indexOf(".")+1);
//                System.out.println(subMap);
//                System.out.println(propertyPath);
            }
            if (subMap!=null){
                propertyValue = (String) subMap.get(propertyPath);
            }
            accumulators.get(accName).accumulateProperty(propertyValue);
        }
    }
    
    
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

//    /**
//     * @return the firstTagTime
//     */
//    public long getFirstTagTime() {
//        return firstTagTime;
//    }
//
//    /**
//     * @param firstTagTime the firstTagTime to set
//     */
//    public void setFirstTagTime(long firstTagTime) {
//        this.firstTagTime = firstTagTime;
//    }
//
//    /**
//     * @return the lastTagTime
//     */
//    public long getLastTagTime() {
//        return lastTagTime;
//    }
//
//    /**
//     * @param lastTagTime the lastTagTime to set
//     */
//    public void setLastTagTime(long lastTagTime) {
//        this.lastTagTime = lastTagTime;
//    }
//
    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Map<String, Object> asMap(){
        Map<String, Object> summaryMap = new HashMap<String, Object>();
        summaryMap.put("owner", owner);
        summaryMap.put("startTime", Long.toString(startTime));
        summaryMap.put("endTime", Long.toString(endTime));
//        summaryMap.put("firstTagTime", Long.toString(firstTagTime));
//        summaryMap.put("lastTagTime", Long.toString(lastTagTime));
        summaryMap.put("duration", Long.toString(duration));
        for (String accName : accumulators.keySet()){
            summaryMap.put(accName, accumulators.get(accName).readOffResults());
            
        }
        return summaryMap;
    }
    
    
}

