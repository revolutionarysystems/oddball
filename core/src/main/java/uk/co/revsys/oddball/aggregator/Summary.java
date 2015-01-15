package uk.co.revsys.oddball.aggregator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import uk.co.revsys.oddball.util.OddUtil;

/**
 *
 * @author Andrew
 */
public class Summary {

    public Summary(String owner, long startTime, long duration, SummaryDefinition summaryDefinition) throws AccumulationException{
        this.owner = owner;
        this.startTime = startTime;
        this.duration = duration;
        this.count = 0;
        this.endTime = startTime+duration;
        this.firstCaseTime = endTime;
        this.lastCaseTime = startTime;
        this.summaryDefinition = summaryDefinition;
        this.accumulators = new HashMap<String, PropertyAccumulator>();
        this.properties= new HashMap<String, String>();
        Collection<Object> definition = (Collection<Object>) summaryDefinition.getDefinition().get("summary");
        //for each analysis property in summary definition
        for (Object accumulator : definition){
            Map<String, String> accMap = (Map)accumulator;
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
    private final SummaryDefinition summaryDefinition;
    private long startTime;
    private long endTime;
    private long firstCaseTime;
    private long lastCaseTime;
    private long duration;
    private long count=0;
    private final Map<String, PropertyAccumulator> accumulators;
    private final Map<String, String> properties;
    

    public void incorporate(Map<String, Object> caseMap, long caseTime){
        for (String accName : accumulators.keySet()){
            String propertyPath = properties.get(accName);
            Object propertyValue = new OddUtil().getDeepProperty(caseMap, propertyPath); 
            accumulators.get(accName).accumulateProperty(propertyValue);
        }
        if (caseTime<this.firstCaseTime){
            firstCaseTime=caseTime;
        }
        if (caseTime>this.lastCaseTime){
            lastCaseTime=caseTime;
        }
        count++;
    }
    

    
    public Map<String, Object> assess(Map<String, Object> caseMap){
        Map<String, Object> comparisonMap = new HashMap<String, Object>();
        for (String accName : accumulators.keySet()){
            Object propertyValue = null;
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
                propertyValue = subMap.get(propertyPath);
            }
            comparisonMap.put(accName, accumulators.get(accName).assessProperty(propertyValue));
        }
        return comparisonMap;
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

    /**
     * @return the firstCaseTime
     */
    public long getFirstCaseTime() {
        return firstCaseTime;
    }

    /**
     * @param firstCaseTime the firstCaseTime to set
     */
    public void setFirstCaseTime(long firstCaseTime) {
        this.firstCaseTime = firstCaseTime;
    }

    /**
     * @return the lastCaseTime
     */
    public long getLastCaseTime() {
        return lastCaseTime;
    }

    /**
     * @param lastCaseTime the lastCaseTime to set
     */
    public void setLastCaseTime(long lastCaseTime) {
        this.lastCaseTime = lastCaseTime;
    }

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
//        summaryMap.put("startTime", Long.toString(startTime));
//        summaryMap.put("endTime", Long.toString(endTime));
//        summaryMap.put("duration", Long.toString(duration));
//        summaryMap.put("count", Long.toString(count));
        summaryMap.put("startTime", startTime);
        summaryMap.put("endTime", endTime);
        if (firstCaseTime<endTime){
            summaryMap.put("firstCaseTime", firstCaseTime);
        }
        if (lastCaseTime>startTime){
            summaryMap.put("lastCaseTime", lastCaseTime);
        }
        summaryMap.put("duration", duration);
        summaryMap.put("count", count);
        for (String accName : accumulators.keySet()){
            summaryMap.put(accName, accumulators.get(accName).readOffResults());
            
        }
        return summaryMap;
    }
    
    
}

