/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
        this.stateMap = new HashMap<Long, String>();
        this.signals = new ArrayList<Map<String, Object>>();
        this.status = Episode.OPEN;
//        this.stateCodes = new StringBuilder("");
        this.stateCodeMap = new HashMap<Long, String>();
//        this.watches = new HashMap<String, ArrayList<String>>();
        this.watchMaps = new HashMap<String, Map<Long, String>>();
//        this.customDataWatches = new HashMap<String, ArrayList<String>>();
        this.customDataWatchMaps = new HashMap<String, Map<Long, String>>();
        this.watchValues = new HashMap<String, String>();
        this.parametersMap = new HashMap<String, String>();
        this.customDataWatchValues = new HashMap<String, String>();
        //alerts = new HashMap<String, Object>();
        if (watchList != null && !watchList.equals("")) {
            String [] watchNames = watchList.split(",");
            for (String watchName : watchNames) {
                watchProperties.add(watchName);
                watchMaps.put(watchName, new HashMap<Long, String>());
            }
        }
    }

    public Episode(Map episodeDetails, String watchList, String customDataTag) {
        if (watchList != null && !watchList.equals("")) {
            String [] watchNames = watchList.split(",");
            for (String watchName : watchNames) {
                watchProperties.add(watchName);
            }
        }
        for (Entry entry : (Set<Entry>) episodeDetails.entrySet()){
            if (entry.getKey().equals("owner")){
                this.owner = (String) entry.getValue();
            } else if (entry.getKey().equals("agent")){
                this.agent = (String) entry.getValue();
            } else if (entry.getKey().equals("series")){
                this.series = (String) entry.getValue();
            } else if (entry.getKey().equals("startTime")){
                this.startTime = (Long) entry.getValue();
            } else if (entry.getKey().equals("endTime")){
                this.endTime = (Long) entry.getValue();
            } else if (entry.getKey().equals("firstTagTime")){
                this.firstTagTime = (Long) entry.getValue();
            } else if (entry.getKey().equals("lastTagTime")){
                this.lastTagTime = (Long) entry.getValue();
            } else if (entry.getKey().equals("duration")){
            } else if (entry.getKey().equals("signals")){
            } else if (entry.getKey().equals("states")){
            } else if (entry.getKey().equals("status")){
            } else if (entry.getKey().equals("stateCodes")){
            } else if (entry.getKey().equals("parameters")){
            } else if (entry.getKey().equals("watches")){
            } else if (entry.getKey().equals("customDataWatches")){
            } else if (entry.getKey().equals("watchValues")){
            } else if (entry.getKey().equals("customDataWatchValues")){
            } else if (entry.getKey().equals("timeoutLimit")){
            } else if (entry.getKey().equals("length")){
            } else if (entry.getKey().equals("alerts")){
            } else if (entry.getKey().equals("time")){
            } else if (entry.getKey().equals("avgWait")){
            } else {
//                LOGGER.debug("baggage"+entry.getKey().toString());
                baggageMap.put((String)entry.getKey(), entry.getValue());
            }
        }
        this.customDataTag = customDataTag;
//        this.owner = (String) episodeDetails.get("owner");
//        this.agent = (String) episodeDetails.get("agent");
//        this.series = (String) episodeDetails.get("series");
//        this.startTime = (Long) episodeDetails.get("startTime");
//        this.endTime = (Long) episodeDetails.get("endTime");
//        this.firstTagTime = (Long) episodeDetails.get("firstTagTime");
//        this.lastTagTime = (Long) episodeDetails.get("lastTagTime");
        try {
            this.duration = new Long((Integer) episodeDetails.get("duration"));
        } catch (ClassCastException e) {
            this.duration = (Long) episodeDetails.get("duration");
        }
        this.signals = (ArrayList<Map<String, Object>>) episodeDetails.get("signals");
        this.stateMap = buildStateMap((ArrayList<String>) episodeDetails.get("states"), signals);
        this.status = Episode.OPEN;
        if (((String) episodeDetails.get("status")).equals("closed")) {
            this.status = Episode.CLOSED;
        }
        this.stateCodeMap = buildStateCodeMap((String) episodeDetails.get("stateCodes"), signals);
//        LOGGER.debug("init");
//        LOGGER.debug(watchProperties.toString());
        this.watchMaps = buildWatchMaps((HashMap<String, ArrayList<String>>) episodeDetails.get("watches"), signals, watchProperties);
        this.customDataWatchMaps = buildWatchMaps((HashMap<String, ArrayList<String>>) episodeDetails.get("customDataWatches"), signals, null);
        this.watchValues = (HashMap<String, String>) episodeDetails.get("watchValues");
        this.parametersMap = (HashMap<String, String>) episodeDetails.get("parameters");
        this.customDataWatchValues = (HashMap<String, String>) episodeDetails.get("customDataWatchValues");
        //this.alerts = (HashMap<String, Object>) episodeDetails.get("alerts");
    }

    public void markTime(long thisTime, long thisTagTime) {
        this.endTime = thisTime;
        this.lastTagTime = thisTagTime;
        this.duration = this.endTime - this.startTime;
    }

    public void recordState(String state, String code, long thisTime, long thisTagTime, long timeoutLimit, Map<String, Object> caseMap, String descriptionProperty) {
//    public void recordState(String state, String code, long thisTime, long thisTagTime) {
        //this.states.add(state);
//        LOGGER.debug("Recording State");
        this.stateMap.put(thisTime, state);
        Map<String, Object> signal = new HashMap<String, Object>();
        if (descriptionProperty != null) {
            signal.put("description", new OddUtil().getDeepProperty(caseMap, descriptionProperty));
        }
        signal.put("state", state);
        signal.put("id", caseMap.get("_id"));
        signal.put("time", thisTime);
        signals.add(signal);
//        this.stateCodes.append(code);
            this.stateCodeMap.put(thisTime, code);
        this.endTime = thisTime;
        this.lastTagTime = thisTagTime;
        this.timeoutLimit = timeoutLimit;
        this.duration = this.endTime - this.startTime;
        for (String watchProperty : watchMaps.keySet()) {
//            LOGGER.debug(watchProperty);
//            Object propertyValue = new OddUtil().getDeepProperty(caseMap, watchProperty.replace("~", "."));
            Object propertyValue = new OddUtil().getDeepProperty(caseMap, watchProperty);
            if (propertyValue != null) {
                Map<Long, String> watchValuesMap = watchMaps.get(watchProperty);
//                LOGGER.debug(watchValuesMap.toString());
                watchMaps.get(watchProperty).put(thisTime, propertyValue.toString());
//                LOGGER.debug(watchValuesMap.toString());
            }
        }
        if (caseMap.containsKey(customDataTag)) {
            Map<String, Object> customData = (Map<String, Object>) new OddUtil().getDeepProperty(caseMap, customDataTag);
            for (String key : customData.keySet()) {
                if (!customDataWatchMaps.containsKey(key)) {
                    customDataWatchMaps.put(key, new HashMap<Long, String>());
                }
//                if ((String) customData.get(key) != null) {
                if (customData.get(key).toString() != null) {
                    (customDataWatchMaps.get(key)).put(thisTime, customData.get(key).toString());
                } else {
                    (customDataWatchMaps.get(key)).put(thisTime, "null");
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
//        this.states.add(intervalString + "s");
//        String intervalString = Integer.toString(intervalSeconds);
//        this.stateCodes.append(intervalCode);
//        Map<String, Object> signal = signals.get(signals.size()-1);
//        signal.put("wait", thisTime - prevTime);
    }

    private String lengthEncode(long intervalSeconds) {
        String intervalString = Long.toString(intervalSeconds);
        String intervalCode = "0";
        if (intervalSeconds > 0) {
            intervalCode = Integer.toString(intervalString.length());
        }
        return intervalCode;
    }

    public void close(long timedOutTime, long lastTagTime) {
        int intervalSeconds = (int) ((timedOutTime) / 1000);
        String intervalString = Integer.toString(intervalSeconds);
//        this.states.add(intervalString + "s > timeout");
        this.stateMap.put(timedOutTime+lastTagTime, intervalString + "s > timeout");
//        this.stateCodes.append("X");
        this.stateCodeMap.put(timedOutTime+lastTagTime, "X");
        this.duration = this.endTime - this.startTime;
        this.status = Episode.CLOSED;
        this.lastTagTime = lastTagTime;
    }

    public boolean isOpen() {
        return status == Episode.OPEN;
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
        ArrayList timings = new ArrayList<Long>();
        timings.addAll(stateMap.keySet());
        Collections.sort(timings);
        return (Long)timings.get(0);
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
        ArrayList timings = new ArrayList<Long>();
        timings.addAll(stateMap.keySet());
        Collections.sort(timings);
        int offset = 1;
        if (this.status==CLOSED){
            offset = 2;
        }
        return (Long)timings.get(timings.size()-offset);
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
        return getEndTime() - getStartTime();
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
        ArrayList timings = new ArrayList<Long>();
        timings.addAll(stateMap.keySet());
        Collections.sort(timings);
        ArrayList<String> states = new ArrayList<String>();
        long prevTime = 0;
        for (Long time : (ArrayList<Long>) timings) {
            String state = stateMap.get(time);
            if (prevTime != 0) {
                long interval = (time - prevTime)/1000;
                String intervalString = Long.toString(interval);
                states.add(intervalString + "s");
            }
            prevTime = time;
            states.add(state);
        }
        return states;
    }

//    /**
//     * @param states the states to set
//     */
//    public void setStates(ArrayList<String> states) {
//        this.states = states;
//    }


    public HashMap<Long, String> buildStateMap(ArrayList<String> states, ArrayList<Map<String, Object>> signals) {
        ArrayList timings = new ArrayList<Integer>();
        HashMap<Long, String> stateMap = new HashMap<Long, String>();
        int i = 0;
        for (Object signal : signals) {
            Long time = (Long) ((Map<String, Object>) signal).get("time");
            String state = "?";
            try {
                state = states.get(i * 2);
            }
            catch(ArrayIndexOutOfBoundsException e){
                // use "?"
            }
            catch(IndexOutOfBoundsException e){
                // use "?"
            }
            stateMap.put(time, state);
            i++;
        }
        return stateMap;
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
        ArrayList timings = new ArrayList<Long>();
        timings.addAll(stateCodeMap.keySet());
        Collections.sort(timings);
        StringBuilder sb = new StringBuilder();
        long prevTime = 0;
        for (Long time : (ArrayList<Long>) timings) {
            String code = stateCodeMap.get(time);
            if (prevTime != 0 && !code.equals("X")) {
                long interval = time - prevTime;
                sb.append(lengthEncode(interval / 1000));
            }
            prevTime = time;
            sb.append(code);
        }
        return sb.toString();
//        return stateCodes.toString();
    }

    public HashMap<Long, String> buildStateCodeMap(String stateCodes, ArrayList<Map<String, Object>> signals) {
        ArrayList timings = new ArrayList<Integer>();
        HashMap<Long, String> stateCodeMap = new HashMap<Long, String>();
        int i = 0;
        for (Object signal : signals) {
            Long time = (Long) ((Map<String, Object>) signal).get("time");
            String stateCode = "?";
            try{
                stateCode = stateCodes.substring(i * 2, i * 2 + 1);
            }
            catch(StringIndexOutOfBoundsException e){
                //just use "?"
            }
            stateCodeMap.put(time, stateCode);
            i++;
        }
        return stateCodeMap;
    }

    public Map<String, Map<Long, String>> buildWatchMaps(HashMap<String, ArrayList<String>> watches, ArrayList<Map<String, Object>> signals, Set<String> watchProperties) {
        ArrayList timings = new ArrayList<Integer>();
        HashMap<String, Map<Long, String>> watchMaps = new HashMap<String, Map<Long, String>>();
        int i = 0;
        int n = signals.size();
        for (Object signal : signals) {
            Long time = (Long) ((Map<String, Object>) signal).get("time");
            if (watchProperties==null){
                watchProperties = watches.keySet();
            }
            for (String property : watchProperties){
                if (!watchMaps.containsKey(property)){
                    watchMaps.put(property, new HashMap<Long, String>());
                }
//                LOGGER.debug(watches.toString());
//                LOGGER.debug(property);
                if (watches.get(property.replace(".","~")).size()==0){
                    watchMaps.get(property).put(time, null);
                } else if (watches.get(property.replace(".","~")).size()==1){
                    watchMaps.get(property).put(time, watches.get(property.replace(".","~")).get(0));
                } else {
                    watchMaps.get(property).put(time, watches.get(property.replace(".","~")).get(Math.min(i, (watches.get(property.replace(".","~")).size())-1)));
                }
            }
            i++;
        }
        return watchMaps;
    }

    public boolean containsEvent(String id, Long time){
        boolean found = false;
        for (Map<String, Object> signal : getSignals()){
            if (time==signal.get("time") || signal.get("id").equals(id)){
                found = true;
                break;
            }
        }
        return found;
    }

//    /**
//     * @param stateCodes the stateCodes to set
//     */
//    public void setStateCodes(String stateCodes) {
//        int l = stateCodes.length();
//        stateCodeMap = new HashMap<Long, String>();
//        for (int i=0; i<l/2; i++){
//            
//        }
//        this.stateCodes = new StringBuilder(stateCodes);
//    }
    private ArrayList<String> condense(Map<Long, String> input) {
        boolean allAlike = true;
        ArrayList<String> inputValues = new ArrayList<String>();
        inputValues.addAll(input.values());
        if (input.size() > 1) {
            for (int i = 1; i < input.size(); i++) {
                if (inputValues.get(i)!=null && !(inputValues.get(i).equals(inputValues.get(0)))) {
                    allAlike = false;
                }
            }
        }
        ArrayList<String> returnCase = new ArrayList<String>();
        if (allAlike && input.size() > 0) {
            returnCase.add(inputValues.get(0));
            return returnCase;
        } else {
            ArrayList<Long> timings = new ArrayList<Long>();
            timings.addAll(input.keySet());
            Collections.sort(timings);
            for (Long time : timings){
                returnCase.add(input.get(time));
            }
            return returnCase;
        }
    }

    private String initial(Map<Long, String> input) {
        if (input.size() > 0) {
            ArrayList<Long> timings = new ArrayList<Long>();
            timings.addAll(input.keySet());
            Collections.sort(timings);
            return input.get(timings.get(0));
//            return input.get(0);
        } else {
            return null;
        }
    }

    private String finalValue(Map<Long, String> input) {
        if (input.size() > 0) {
            ArrayList<Long> timings = new ArrayList<Long>();
            timings.addAll(input.keySet());
            Collections.sort(timings);
            return input.get(timings.get(timings.size()-1));
//            return input.get(input.size() - 1);
        } else {
            return null;
        }
    }

    private Map<String, Object> getAlerts(Map<String, ArrayList<String>> watches){
        Map<String, Object> alerts = new HashMap<String, Object> ();
        for (String watchProperty : watches.keySet()){
            ArrayList<String> watchValuesList = watches.get(watchProperty);
            for (int i = 1; i < watchValuesList.size(); i++){
                String prevValue = watchValuesList.get(i-1).toString();
                String currentValue=watchValuesList.get(i).toString();
                if (!currentValue.equals(prevValue)) {
                    Map<String, Object> change = new HashMap<String, Object>();
                    if (alerts.containsKey("valueChanged-" + watchProperty)) {
                        change = (HashMap<String, Object>) alerts.get("valueChanged-" + watchProperty);
                    } else {
                        alerts.put("valueChanged-" + watchProperty, change);
                    }
                    change.put(Integer.toString(i), prevValue + "," + currentValue);
                }
            }
        }
        return alerts;
    }
//                //make alerts dynamic                
//                if (!watchValuesMap.isEmpty()) {
//                    String prevValue = watchValuesList.get(watchValuesList.size() - 1);
//                    String step = Integer.toString(watchValuesList.size());
//                    if (!propertyValue.toString().equals(prevValue)) {
//                        Map<String, Object> change = new HashMap<String, Object>();
//                        if (alerts.containsKey("valueChanged-" + watchProperty)) {
//                            change = (HashMap<String, Object>) alerts.get("valueChanged-" + watchProperty);
//                        } else {
//                            alerts.put("valueChanged-" + watchProperty, change);
//                        }
//                        change.put(step, prevValue + "," + propertyValue.toString());
//                    }
//                }

    
    
    public Map<String, Object> asMap() {
        Map<String, Object> episodeMap = new HashMap<String, Object>();
        episodeMap.put("owner", owner);
        episodeMap.put("agent", agent);
        episodeMap.put("series", series);
        episodeMap.put("time", startTime);
        episodeMap.put("startTime", getStartTime());
        episodeMap.put("endTime", getEndTime());
        episodeMap.put("firstTagTime", firstTagTime);
        episodeMap.put("lastTagTime", lastTagTime);
        episodeMap.put("timeoutLimit", getTimeoutLimit());
        episodeMap.put("duration", getDuration());
        episodeMap.put("states", getStates());
        episodeMap.put("signals", signals);
        episodeMap.put("length", signals.size());
        if (signals.size() > 1) {
            episodeMap.put("avgWait", getDuration() / (signals.size() - 1));
        }
        episodeMap.put("length", signals.size());
        if (isOpen()) {
            episodeMap.put("status", "open");
        } else {
            episodeMap.put("status", "closed");
        }
        episodeMap.put("stateCodes", getStateCodes());
        Map<String, ArrayList<String>> watches = new HashMap<String, ArrayList<String>> ();
        for (String watchProperty : watchMaps.keySet()) {
            ArrayList condensed = condense(watchMaps.get(watchProperty));
            watches.put(watchProperty.replace(".", "~"), condensed);
        }
        for (String watchProperty : watchMaps.keySet()) {
            watchValues.put(watchProperty.replace(".", "~"), finalValue(watchMaps.get(watchProperty)));
        }
        Map<String, ArrayList<String>> customDataWatches = new HashMap<String, ArrayList<String>> ();
        for (String watchProperty : customDataWatchMaps.keySet()) {
            customDataWatches.put(watchProperty.replace(".", "~"), condense(customDataWatchMaps.get(watchProperty)));
        }
        for (String watchProperty : customDataWatches.keySet()) {
            customDataWatchValues.put(watchProperty.replace(".", "~"), finalValue(customDataWatchMaps.get(watchProperty)));
        }
        episodeMap.put("watches", watches);
        episodeMap.put("watchValues", watchValues);
        episodeMap.put("parameters", parametersMap);
        episodeMap.put("alerts", getAlerts(watches));
        episodeMap.put("customDataWatches", customDataWatches);
        episodeMap.put("customDataWatchValues", customDataWatchValues);
        for (Entry entry :  baggageMap.entrySet()){
            episodeMap.put((String)entry.getKey(), entry.getValue());
        }
        return episodeMap;

    }

    static int OPEN = 1;
    static int CLOSED = 0;
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

    private Map<String, Object> baggageMap = new HashMap<String, Object>();
    private String owner;
    private String agent;
    private String series;
    private String customDataTag = "case.customData";
    private long startTime;
    private long endTime;
    private long timeoutLimit;
    private long firstTagTime;
    private long lastTagTime;
    private long duration;
//    private ArrayList<String> states;
    private Map<Long, String> stateMap;
    final private ArrayList<Map<String, Object>> signals;
    private int status;
//    private StringBuilder stateCodes;
    private HashMap<Long, String> stateCodeMap;
//    final private Map<String, ArrayList<String>> watches;
    private Set<String> watchProperties = new HashSet<String>();
    final private Map<String, Map<Long, String>> watchMaps;
//    final private Map<String, ArrayList<String>> customDataWatches;
    final private Map<String, Map<Long, String>> customDataWatchMaps;
    final private Map<String, String> watchValues;
    final private Map<String, String> parametersMap;
    final private Map<String, String> customDataWatchValues;
//    final private Map<String, Object> alerts;

    /**
     * @return the timeoutLimit
     */
    public long getTimeoutLimit() {
        return timeoutLimit;
    }

}
