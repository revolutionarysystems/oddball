/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.util.OddUtil;

/**
 *
 * @author Andrew
 */
public class Episode {

    public Episode(String owner, String agent, String series, long startTime, long firstTagTime, String watchList, String customDataTag) {
        this.owner = owner;
        this.agent = agent;
        this.series = series;
        this.startTime = startTime;
        this.firstTagTime = firstTagTime;
        this.customDataTag = customDataTag;
        this.states = new ArrayList<String>();
        this.signals = new ArrayList<Map<String, Object>>();
        this.status = Episode.OPEN;
        this.stateCodes = new StringBuilder("");
        this.watches = new HashMap<String, ArrayList<String>>();
        this.customDataWatches = new HashMap<String, ArrayList<String>>();
        this.watchValues = new HashMap<String, String>();
        this.customDataWatchValues = new HashMap<String, String>();
        alerts = new HashMap<String, Object>();
        if (watchList!=null && !watchList.equals("")){
            String[]watchProperties = watchList.split(",");
            for (String watchProperty : watchProperties){
                watches.put(watchProperty.replace(".","~"), new ArrayList<String>());
            } 
        }
    }

    public Episode(Map episodeDetails, String customDataTag) {
        this.owner = (String) episodeDetails.get("owner");
        this.agent = (String) episodeDetails.get("agent");
        this.series = (String) episodeDetails.get("series");
        this.startTime = (Long) episodeDetails.get("startTime");
        this.endTime = (Long) episodeDetails.get("endTime");
        this.firstTagTime = (Long) episodeDetails.get("firstTagTime");
        this.lastTagTime = (Long) episodeDetails.get("lastTagTime");
        try {
            this.duration =  new Long((Integer)episodeDetails.get("duration"));
        } 
        catch (ClassCastException e){
            this.duration =  (Long)episodeDetails.get("duration");
        }
        this.customDataTag = customDataTag;
        this.states = (ArrayList<String>) episodeDetails.get("states");
        this.signals = (ArrayList<Map<String, Object>>) episodeDetails.get("signals");
        this.status = Episode.OPEN;
        if (((String)episodeDetails.get("status")).equals("closed")){
            this.status = Episode.CLOSED;
        }
        this.stateCodes = new StringBuilder((String) episodeDetails.get("stateCodes"));
        this.watches = (HashMap<String, ArrayList<String>>) episodeDetails.get("watches");
        this.customDataWatches = (HashMap<String, ArrayList<String>>) episodeDetails.get("customDataWatches");
        this.watchValues = (HashMap<String, String>) episodeDetails.get("watchValues");
        this.customDataWatchValues = (HashMap<String, String>) episodeDetails.get("customDataWatchValues");
        this.alerts = (HashMap<String, Object>) episodeDetails.get("alerts");
    }
    
    
    public void markTime(long thisTime, long thisTagTime) {
        this.endTime = thisTime;
        this.lastTagTime = thisTagTime;
        this.duration = this.endTime - this.startTime;
    }

    public void recordState(String state, String code, long thisTime, long thisTagTime, Map<String, Object> caseMap, String descriptionProperty) {
//    public void recordState(String state, String code, long thisTime, long thisTagTime) {
        this.states.add(state);
        Map<String, Object> signal = new HashMap<String, Object>();
        if (descriptionProperty!=null){
            signal.put("description", new OddUtil().getDeepProperty(caseMap, descriptionProperty));
        }
        signal.put("state", state);
        signal.put("id", caseMap.get("_id"));
        signal.put("time", thisTime);
        signals.add(signal);
        this.stateCodes.append(code);
        this.endTime = thisTime;
        this.lastTagTime = thisTagTime;
        this.duration = this.endTime - this.startTime;
        for (String watchProperty : watches.keySet()){
            Object propertyValue = new OddUtil().getDeepProperty(caseMap, watchProperty.replace("~",".")); 
            if (propertyValue!=null){
                ArrayList<String> watchValuesList = watches.get(watchProperty);
                if (!watchValuesList.isEmpty()){
                    String prevValue = watchValuesList.get(watchValuesList.size()-1);
                    String step= Integer.toString(watchValuesList.size());
                    if (!propertyValue.toString().equals(prevValue)){
                        Map<String, Object> change = new HashMap<String, Object>();
                        if (alerts.containsKey("valueChanged-"+watchProperty)){
                            change = (HashMap<String, Object>) alerts.get("valueChanged-"+watchProperty);
                        }else {
                            alerts.put("valueChanged-"+watchProperty, change);
                        }
                        change.put(step, prevValue+","+propertyValue.toString());
                    }
                }
                watches.get(watchProperty).add(propertyValue.toString());
            }
        }
        if (caseMap.containsKey(customDataTag)){
            Map<String, Object> customData = (Map<String, Object>)new OddUtil().getDeepProperty(caseMap, customDataTag); 
            for (String key : customData.keySet()){
                if (!customDataWatches.containsKey(key)){
                    customDataWatches.put(key, new ArrayList());
                }
                if ((String)customData.get(key)!=null){
                    (customDataWatches.get(key)).add((String)customData.get(key));
                } else {
                    (customDataWatches.get(key)).add("null");
                }
            }
        }
    }

    public void recordInterval(long prevTime, long thisTime, long thisTagTime) {
        int intervalSeconds = (int) ((thisTime - prevTime) / 1000);
        String intervalString = Integer.toString(intervalSeconds);
        String intervalCode = "0";
        if (intervalSeconds > 0) {
            intervalCode = Integer.toString(intervalString.length());
        }
        lastTagTime = thisTagTime;
        this.states.add(intervalString + "s");
        this.stateCodes.append(intervalCode);
//        Map<String, Object> signal = signals.get(signals.size()-1);
//        signal.put("wait", thisTime - prevTime);
    }

    public void close(long timedOutTime, long lastTagTime) {
        int intervalSeconds = (int) ((timedOutTime) / 1000);
        String intervalString = Integer.toString(intervalSeconds);
        this.states.add(intervalString + "s > timeout");
        this.stateCodes.append("X");
        this.duration = this.endTime - this.startTime;
        this.status = Episode.CLOSED;
        this.lastTagTime = lastTagTime;
    }

    public boolean isOpen() {
        return status == Episode.OPEN;
    }

    private String owner;
    private String agent;
    private String series;
    private String customDataTag="case.customData";
    private long startTime;
    private long endTime;
    private long firstTagTime;
    private long lastTagTime;
    private long duration;
    private ArrayList<String> states;
    final private ArrayList<Map<String, Object>> signals;
    private int status;
    private StringBuilder stateCodes;
    final private Map<String, ArrayList<String>> watches;
    final private Map<String, ArrayList<String>> customDataWatches;
    final private Map<String, String> watchValues;
    final private Map<String, String> customDataWatchValues;
    final private Map<String, Object> alerts;

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
     * @return the agent
     */
    public String getAgent() {
        return agent;
    }

    /**
     * @param agent the agent to set
     */
    public void setAgent(String agent) {
        this.agent = agent;
    }

    /**
     * @return the series
     */
    public String getSeries() {
        return series;
    }

    /**
     * @param series the series to set
     */
    public void setSeries(String series) {
        this.series = series;
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
     * @return the firstTagTime
     */
    public long getFirstTagTime() {
        return firstTagTime;
    }

    /**
     * @param firstTagTime the firstTagTime to set
     */
    public void setFirstTagTime(long firstTagTime) {
        this.firstTagTime = firstTagTime;
    }

    /**
     * @return the lastTagTime
     */
    public long getLastTagTime() {
        return lastTagTime;
    }

    /**
     * @param lastTagTime the lastTagTime to set
     */
    public void setLastTagTime(long lastTagTime) {
        this.lastTagTime = lastTagTime;
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

    /**
     * @return the states
     */
    public ArrayList<String> getStates() {
        return states;
    }

    /**
     * @param states the states to set
     */
    public void setStates(ArrayList<String> states) {
        this.states = states;
    }

    /**
     * @return the signals
     */
    public ArrayList<Map<String, Object>> getSignals() {
        return signals;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the stateCodes
     */
    public String getStateCodes() {
        return stateCodes.toString();
    }

    /**
     * @param stateCodes the stateCodes to set
     */
    public void setStateCodes(String stateCodes) {
        this.stateCodes = new StringBuilder(stateCodes);
    }

    private ArrayList<String> condense(ArrayList<String> input){
        boolean allAlike = true;
        if (input.size()>1){
            for (int i=1;i<input.size();i++){
                if (!input.get(i).equals(input.get(0))){
                    allAlike = false;
                }
            }
        }
        if (allAlike && input.size()>0){
            ArrayList<String> returnCase = new ArrayList<String>();
            returnCase.add(input.get(0));
            return returnCase;
        } else {
            return input;
        }
    }
    
    private String initial(ArrayList<String> input){
        if (input.size()>0){
            return input.get(0);
        } else {
            return null;
        }
    }
    
    private String finalValue(ArrayList<String> input){
        if (input.size()>0){
            return input.get(input.size()-1);
        } else {
            return null;
        }
    }
    
    public Map<String, Object> asMap() {
        Map<String, Object> episodeMap = new HashMap<String, Object>();
        episodeMap.put("owner", owner);
        episodeMap.put("agent", agent);
        episodeMap.put("series", series);
        episodeMap.put("time", startTime);
        episodeMap.put("startTime", startTime);
        episodeMap.put("endTime", endTime);
        episodeMap.put("firstTagTime", firstTagTime);
        episodeMap.put("lastTagTime", lastTagTime);
        episodeMap.put("duration", duration);
        episodeMap.put("states", states);
        episodeMap.put("signals", signals);
        episodeMap.put("length", signals.size());
        if (signals.size()>1){
            episodeMap.put("avgWait", duration/(signals.size()-1));
        }
        episodeMap.put("length", signals.size());
        if (isOpen()) {
            episodeMap.put("status", "open");
        } else {
            episodeMap.put("status", "closed");
        }
        episodeMap.put("stateCodes", stateCodes.toString());
        for (String watchProperty: watches.keySet()){
            watches.put(watchProperty, condense(watches.get(watchProperty)));
        }
        for (String watchProperty: watches.keySet()){
            watchValues.put(watchProperty, finalValue(watches.get(watchProperty)));
        }
        for (String watchProperty: customDataWatches.keySet()){
            customDataWatches.put(watchProperty, condense(customDataWatches.get(watchProperty)));
        }
        for (String watchProperty: customDataWatches.keySet()){
            customDataWatchValues.put(watchProperty, finalValue(customDataWatches.get(watchProperty)));
        }
        episodeMap.put("watches", watches);
        episodeMap.put("watchValues", watchValues);
        episodeMap.put("alerts", alerts);
        episodeMap.put("customDataWatches", customDataWatches);
        episodeMap.put("customDataWatchValues", customDataWatchValues);
        return episodeMap;
        
    }

    static int OPEN = 1;
    static int CLOSED = 0;
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
