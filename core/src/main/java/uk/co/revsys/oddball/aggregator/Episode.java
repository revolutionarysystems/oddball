/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import uk.co.revsys.oddball.util.OddUtil;

/**
 *
 * @author Andrew
 */
public class Episode {

    public Episode(String owner, String agent, String series, long startTime, long firstTagTime, String watchList) {
        this.owner = owner;
        this.agent = agent;
        this.series = series;
        this.startTime = startTime;
        this.firstTagTime = firstTagTime;
        this.states = new ArrayList<String>();
        this.signals = new ArrayList<Map<String, Object>>();
        this.status = Episode.OPEN;
        this.stateCodes = new StringBuilder("");
        watches = new HashMap<String, ArrayList<String>>();
        watchValues = new HashMap<String, String>();
        alerts = new HashMap<String, Object>();
        if (watchList!=null && !watchList.equals("")){
            String[]watchProperties = watchList.split(",");
            for (String watchProperty : watchProperties){
                watches.put(watchProperty.replace(".","~"), new ArrayList<String>());
            } 
        }
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
                ArrayList<String> watchValues = watches.get(watchProperty);
                if (!watchValues.isEmpty()){
                    String prevValue = watchValues.get(watchValues.size()-1);
                    String step= Integer.toString(watchValues.size());
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
    private long startTime;
    private long endTime;
    private long firstTagTime;
    private long lastTagTime;
    private long duration;
    private ArrayList<String> states;
    private ArrayList<Map<String, Object>> signals;
    private int status;
    private StringBuilder stateCodes;
    private Map<String, ArrayList<String>> watches;
    private Map<String, String> watchValues;
    private Map<String, Object> alerts;

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
        for (int i=1;i<input.size();i++){
            if (!input.get(i).equals(input.get(0))){
                allAlike = false;
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
        episodeMap.put("watches", watches);
        episodeMap.put("watchValues", watchValues);
        episodeMap.put("alerts", alerts);
        return episodeMap;
    }

    static int OPEN = 1;
    static int CLOSED = 0;

}
