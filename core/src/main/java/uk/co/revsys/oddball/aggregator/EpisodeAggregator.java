/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import uk.co.revsys.oddball.cases.Case;

/**
 *
 * @author Andrew
 */
public class EpisodeAggregator implements Aggregator{
    
    
    
    @Override
    public ArrayList<Object> aggregateCases(Iterable<String> caseStrings, Map options) throws AggregationException{
        ArrayList<Object> response = new ArrayList<Object>();
        long timeOutPeriod = 600000L;
        long timeOutReference = new Date().getTime(); // default is now
        if (options.containsKey("timeOutPeriod")){
            timeOutPeriod = Long.parseLong((String)options.get("timeOutPeriod"));
        }
        if (options.containsKey("timeOutReference")){
            try {
                timeOutReference = Long.parseLong((String)options.get("timeOutReference"));
            } catch (NumberFormatException e){}
        }
        try {
            for (Episode ep : aggregateEvents(caseStrings, timeOutPeriod, timeOutReference)){
                response.add(ep.asMap());
            }
        }
        catch (EventNotCreatedException e){
            throw new AggregationException("Event could not be created", e);
        }
        return response;
    }

    
    public ArrayList<Episode> aggregateEvents(Iterable<String> eventStrings, long timeOutPeriod, long timeOutReference) throws EventNotCreatedException{
        ArrayList<Event> eventList = new ArrayList<Event>();
        for (String eventString : eventStrings){
            try {
                Event event = new Event(eventString);
                eventList.add(event);
            }
            catch (IOException ex){
                throw new EventNotCreatedException(eventString, ex);
            }
            catch (ParseException ex){
                throw new EventNotCreatedException(eventString+": bad time format", ex);
            }
        }
        Collections.sort(eventList);
        //boolean openEpisode = false;
        long previousEventTime = 0;
        long previousTagTime = 0;
        ArrayList<Episode> episodes = new ArrayList<Episode>();
        Episode currentEpisode = null;
        for (Event event : eventList){
            if (currentEpisode==null){
                currentEpisode = new Episode(event.getOwner(), event.getAgent(), event.getSeries(), event.getEventTime(), event.getTagTime());
            } else {
                if (event.getEventTime()-previousEventTime > timeOutPeriod){   //timedout
                    currentEpisode.close(event.getEventTime()-previousEventTime, event.getTagTime());
                    episodes.add(currentEpisode);
                    currentEpisode = new Episode(event.getOwner(), event.getAgent(), event.getSeries(), event.getEventTime(), event.getTagTime());
                } else {  //not Timed out
                    currentEpisode.recordInterval(previousEventTime, event.getEventTime(), event.getTagTime());
                }
            }
            currentEpisode.recordState(event.getState(),event.getCode(), event.getEventTime(), event.getTagTime());
            previousEventTime = event.getEventTime();
            previousTagTime = event.getTagTime();
        }
        if (currentEpisode!=null){
            if (currentEpisode.isOpen()){
                if (timeOutReference-currentEpisode.getLastTagTime() > timeOutPeriod){
                    currentEpisode.close(timeOutReference-currentEpisode.getLastTagTime(), previousTagTime);
                }
            }
            episodes.add(currentEpisode);
        }
        return episodes;
    }
    
}
