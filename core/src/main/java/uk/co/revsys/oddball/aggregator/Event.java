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
        Map<String, Object> mapCase = JSONUtil.json2map(jsonCase);
        tagTime=Long.parseLong((String)mapCase.get("tagTime"));
        eventTime=parseTime((String)mapCase.get("time"));
        state=(String)mapCase.get("state");
        code=(String)mapCase.get("code");
        if (code.equals("odDball")){
            code="?";
        } else {
            code=code.substring(0,1);
        }
        owner=(String)mapCase.get("accountId");
        agent=(String)mapCase.get("userId");
        if (agent==null){
            agent=(String)mapCase.get("agent");
        }
        series=(String)mapCase.get("sessionId");
        if (series==null){
            series=(String)mapCase.get("series");
        }
    }
    
    private long parseTime(String timeString) throws ParseException{
        try {
            long time=Long.parseLong(timeString);
            return time;
        } catch (NumberFormatException ex){
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
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
    
}
