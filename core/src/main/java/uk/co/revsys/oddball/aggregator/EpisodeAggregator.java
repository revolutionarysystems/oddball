/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.oddball.util.OddUtil;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class EpisodeAggregator implements Aggregator {

    @Override
    public ArrayList<Map> aggregateCases(Iterable<String> caseStrings, Map<String, String> options, ResourceRepository resourceRepository) throws AggregationException {
        ArrayList<Map> response = new ArrayList<Map>();
        long timeOutPeriod = 600000L;
        long timeOutReference = new Date().getTime(); // default is now
        String descriptionProperty = null;
        String watchList = "";
        if (options.containsKey("watchList")) {
            watchList = options.get("watchList");
        }
        String excludeCodes = "";
        if (options.containsKey("excludeCodes")) {
            excludeCodes = options.get("excludeCodes");
        }
        String customDataTag = "case.customData";
        if (options.containsKey("customDataTag")) {
            customDataTag = options.get("customDataTag");
        }
        if (options.containsKey("timeOutPeriod")) {
            try {
                timeOutPeriod = new OddUtil().parseTimePeriod((String) options.get("timeOutPeriod"), "~");
            } catch (InvalidTimePeriodException e) {
                timeOutPeriod = Long.parseLong((String) options.get("timeOutPeriod"));
            }
        }
        if (options.containsKey("timeOutReference")) {
            try {
                timeOutReference = new OddUtil().parseTimePeriod((String) options.get("timeOutReference"), "~");
            } catch (InvalidTimePeriodException e) {
            }
        }
        if (options.containsKey("descriptionProperty")) {
            descriptionProperty = options.get("descriptionProperty");
        }
        try {
            for (Episode ep : aggregateEvents(caseStrings, timeOutPeriod, timeOutReference, watchList, descriptionProperty, customDataTag, excludeCodes)) {
                response.add(ep.asMap());
            }
        } catch (EventNotCreatedException e) {
            throw new AggregationException("Event could not be created", e);
        }
        return response;
    }

    public ArrayList<Episode> aggregateEvents(Iterable<String> eventStrings, long timeOutPeriod, long timeOutReference, String watchList, String descriptionProperty, String customDataTag, String excludeCodes) throws EventNotCreatedException {
        ArrayList<Event> eventList = new ArrayList<Event>();
        for (String eventString : eventStrings) {
            try {
                Event event = new Event(eventString);
                eventList.add(event);
            } catch (IOException ex) {
                throw new EventNotCreatedException(eventString, ex);
            } catch (ParseException ex) {
                throw new EventNotCreatedException(eventString + ": bad time format", ex);
            }
        }
        Collections.sort(eventList);
        //boolean openEpisode = false;
        long previousEventTime = 0;
        long previousTagTime = 0;
        ArrayList<Episode> episodes = new ArrayList<Episode>();
        Episode currentEpisode = null;
        for (Event event : eventList) {
            if (currentEpisode == null) {
                currentEpisode = new Episode(event.getOwner(), event.getAgent(), event.getSeries(), event.getEventTime(), event.getTagTime(), watchList, customDataTag);
            } else {
                if (event.getEventTime() - previousEventTime > timeOutPeriod) {   //timedout
                    currentEpisode.close(event.getEventTime() - previousEventTime, event.getTagTime());
                    episodes.add(currentEpisode);
                    currentEpisode = new Episode(event.getOwner(), event.getAgent(), event.getSeries(), event.getEventTime(), event.getTagTime(), watchList, customDataTag);
                } else {  //not Timed out
                    if (!excludeCodes.contains(event.getCode())){
                        currentEpisode.recordInterval(previousEventTime, event.getEventTime(), event.getTagTime());
                    }
                }
            }
            if (excludeCodes.contains(event.getCode())){
                currentEpisode.markTime(event.getEventTime(), event.getTagTime());
            } else {
                currentEpisode.recordState(event.getState(), event.getCode(), event.getEventTime(), event.getTagTime(), event.getCaseMap(), descriptionProperty);
            }
            previousEventTime = event.getEventTime();
            previousTagTime = event.getTagTime();
        }
        if (currentEpisode != null) {
            if (currentEpisode.isOpen()) {
                if (timeOutReference - currentEpisode.getLastTagTime() > timeOutPeriod) {
                    currentEpisode.close(timeOutReference - currentEpisode.getLastTagTime(), previousTagTime);
                }
            }
            episodes.add(currentEpisode);
        }
        return episodes;
    }

}
