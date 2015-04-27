/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import uk.co.revsys.oddball.util.JSONUtil;

/**
 *
 * @author Andrew
 */
public class Event implements Comparable<Event> {

    public Event(String jsonCase) throws IOException, ParseException{
        Map<String, Object> eventMap = JSONUtil.json2map(jsonCase);
        caseMap = eventMap;
        Map<String, Object> eventSubcaseMap = new HashMap<String, Object>();
        if (eventMap.get("case")!=null){
            eventSubcaseMap = (Map<String, Object>) eventMap.get("case");
        }
        Map<String, Object> derivedMap = (Map<String, Object>) eventMap.get("derived");
        if (eventMap.get("tagTime")!=null){
            tagTime=Long.parseLong((String)eventMap.get("tagTime"));
        } else {
            tagTime=Long.parseLong((String)eventMap.get("timestamp"));
        }
        if (eventMap.get("time")!=null){
            try {
                eventTime=(Long)eventMap.get("time");
            } catch (ClassCastException e) {
                eventTime=parseTime((String)eventMap.get("time"));
            }
        } else {
            if (eventSubcaseMap.get("time")!=null){
                try {
                    eventTime=(Long)eventSubcaseMap.get("time");
                } catch (ClassCastException e) {
                    eventTime=parseTime((String)eventSubcaseMap.get("time"));
                }
            } else {
                if (eventMap.get("clientTime")!=null){
                    eventTime=(Long)eventMap.get("clientTime");
                } else {
                    if (eventSubcaseMap.get("clientTime")!=null){
                        eventTime=(Long)eventSubcaseMap.get("clientTime");
                    }
                }
            }
        }
        if (eventMap.get("state")!=null){    
            state=(String)eventMap.get("state");
        } else {
            state=(String)derivedMap.get("state");
        }
        
        if (eventMap.get("code")!=null){    
            code=(String)eventMap.get("code");
        } else {
            code=(String)derivedMap.get("code");
        }
        
        if (code==null || code.equals("odDball")){
            code="?";
        } else {
            code=code.substring(0,1);
        }

        if (eventMap.get("accountId")!=null){    
            owner=(String)eventMap.get("accountId");
        } else {
            owner=(String)eventSubcaseMap.get("accountId");
        }
        
        if (owner==null){
            if (eventMap.get("owner")!=null){    
                owner=(String)eventMap.get("owner");
            } else {
                owner=(String)eventSubcaseMap.get("owner");
            }
        }
        
        if (eventMap.get("userId")!=null){    
            agent=(String)eventMap.get("userId");
        } else {
            agent=(String)eventSubcaseMap.get("userId");
        }
        
        if (agent==null){
            if (eventMap.get("agent")!=null){    
                agent=(String)eventMap.get("agent");
            } else {
                agent=(String)eventSubcaseMap.get("agent");
            }
        }

        if (eventMap.get("sessionId")!=null){    
            series=(String)eventMap.get("sessionId");
        } else {
            series=(String)eventSubcaseMap.get("sessionId");
        }
        
        if (series==null){
            if (eventMap.get("series")!=null){    
                series=(String)eventMap.get("series");
            } else {
                series=(String)eventSubcaseMap.get("series");
            }
        }

    }
    
    private long parseTime(String timeString) throws ParseException{
        try {
            long time=Long.parseLong(timeString);
            return time;
        } catch (NumberFormatException ex){
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
            //SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss");
            try {
                long time = sdf.parse(timeString).getTime();
                return time;
            } catch (ParseException ex2){
                sdf = new SimpleDateFormat("dd MMM HH:mm:ss");
                long time = sdf.parse(timeString).getTime();
                return time;
            }
            
        } 
    }
    
    
    @Override
    public int compareTo(Event compareEvent) {

		long compareEventTime = ((Event) compareEvent).getEventTime(); 
 
		//ascending order
		return (int) (this.eventTime - compareEventTime);
 
    }
    
    private long tagTime;
    private long eventTime;
    private String state;
    private String code;
    private String owner;
    private String agent;
    private String series;
    private Map<String, Object> caseMap;

    /**
     * @return the tagTime
     */
    public long getTagTime() {
        return tagTime;
    }

    /**
     * @param tagTime the tagTime to set
     */
    public void setTagTime(long tagTime) {
        this.tagTime = tagTime;
    }

    /**
     * @return the eventTime
     */
    public long getEventTime() {
        return eventTime;
    }

    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
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
     * @return the caseMap
     */
    public Map<String, Object> getCaseMap() {
        return caseMap;
    }


}
