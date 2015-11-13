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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.oddball.util.OddUtil;

/**
 *
 * @author Andrew
 */
public class EpisodeAggregatorTest{
    
    public EpisodeAggregatorTest() {
    }

    @Test
    public void testAggregateEvents() throws EventNotCreatedException {
        HashSet<String> events = new HashSet<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "", null, null, "");
        assertTrue(episodes.size()==1);
        Episode ep = episodes.get(0);
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
        List<Episode> episodes2 = ea.aggregateEvents(events, 30000, 1409589263988L, "", null, null, "");
        assertTrue(episodes2.size()==1);
        Episode ep2 = episodes2.get(0);
        assertEquals("A0B0CX", ep2.getStateCodes());
        System.out.println(ep2.asMap());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertFalse(ep2.isOpen());
        System.out.println(ep2.asMap());
        List<Episode> episodes3 = ea.aggregateEvents(events, 150, 1409589263988L, "", null, null, "");
        assertTrue(episodes3.size()==2);
        Episode ep3a = episodes3.get(0);
        assertEquals("A0BX", ep3a.getStateCodes());
        Episode ep3b = episodes3.get(1);
        assertEquals("CX", ep3b.getStateCodes());
        System.out.println(ep3b.getTimeoutLimit()-ep3b.getEndTime());
        assertTrue(ep3b.getTimeoutLimit()-ep3b.getEndTime()==150L);
        
       
        
    }

    @Test
    public void testAggregateEventsWithExclude() throws EventNotCreatedException {
        HashSet<String> events = new HashSet<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "", null, null, "B");
        assertTrue(episodes.size()==1);
        Episode ep = episodes.get(0);
        assertEquals("A0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
        List<Episode> episodes2 = ea.aggregateEvents(events, 30000, 1409589263988L, "", null, null, "B");
        assertTrue(episodes2.size()==1);
        Episode ep2 = episodes2.get(0);
        assertEquals("A0CX", ep2.getStateCodes());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertFalse(ep2.isOpen());
        System.out.println(ep2.asMap());
        List<Episode> episodes3 = ea.aggregateEvents(events, 150, 1409589263988L, "", null, null, "B");
        assertTrue(episodes3.size()==2);
        Episode ep3a = episodes3.get(0);
        assertEquals("AX", ep3a.getStateCodes());
        Episode ep3b = episodes3.get(1);
        assertEquals("CX", ep3b.getStateCodes());
        
       
        
    }
    
    
    @Test
    public void testAggregateEventsWatchList() throws EventNotCreatedException {
        HashSet<String> events = new HashSet<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"place\": \"paris\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"place\": \"lyon\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"place\": \"lyon\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "place", null, null, "");
        assertTrue(episodes.size()==1);
        Episode ep = episodes.get(0);
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
        List<Episode> episodes2 = ea.aggregateEvents(events, 30000, 1409589263988L,"place", "href", null, "");
        assertTrue(episodes2.size()==1);
        Episode ep2 = episodes2.get(0);
        assertEquals("A0B0CX", ep2.getStateCodes());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertFalse(ep2.isOpen());
        System.out.println(ep2.asMap());
        assertEquals("http://www.revolutionarysystems.co.uk/", ((Map)((List)ep2.asMap().get("signals")).get(0)).get("description"));
        assertEquals("[paris, lyon, lyon]",(new OddUtil().getDeepProperty(ep2.asMap(), "watches.place")).toString());
        assertEquals("lyon",(new OddUtil().getDeepProperty(ep2.asMap(), "watchValues.place")).toString());
        assertEquals("{valueChanged-place={1=paris,lyon}}",(new OddUtil().getDeepProperty(ep2.asMap(), "alerts")).toString());
        List<Episode> episodes3 = ea.aggregateEvents(events, 150, 1409589263988L, "place", null, null, "");
        assertTrue(episodes3.size()==2);
        Episode ep3a = episodes3.get(0);
        assertEquals("A0BX", ep3a.getStateCodes());
        Episode ep3b = episodes3.get(1);
        assertEquals("CX", ep3b.getStateCodes());
        
       
        
    }

    
    
    @Test
    public void testAggregateEventsWatchListCustomData() throws EventNotCreatedException {
        HashSet<String> events = new HashSet<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"place\": \"paris\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"place\": \"lyon\","
                + "\"tagTime\": \"1409579263100\","
                + "\"customData\": {\"wotsit\":\"foo\", \"thingum\":\"A\"},"
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"place\": \"lyon\","
                + "\"tagTime\": \"1409579263300\","
                + "\"customData\": {\"wotsit\":\"foo\", \"thingum\":\"B\"},"
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "place", null, "customData", "");
        assertTrue(episodes.size()==1);
        Episode ep = episodes.get(0);
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
        List<Episode> episodes2 = ea.aggregateEvents(events, 30000, 1409589263988L,"place", "href", "customData", "");
        assertTrue(episodes2.size()==1);
        Episode ep2 = episodes2.get(0);
        assertEquals("A0B0CX", ep2.getStateCodes());
        System.out.println(ep2.asMap());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertFalse(ep2.isOpen());
        System.out.println(ep2.asMap());
        assertEquals("http://www.revolutionarysystems.co.uk/", ((Map)((List)ep2.asMap().get("signals")).get(0)).get("description"));
        assertEquals("[paris, lyon, lyon]",(new OddUtil().getDeepProperty(ep2.asMap(), "watches.place")).toString());
        assertEquals("lyon",(new OddUtil().getDeepProperty(ep2.asMap(), "watchValues.place")).toString());
        assertEquals("{valueChanged-place={1=paris,lyon}}",(new OddUtil().getDeepProperty(ep2.asMap(), "alerts")).toString());
        List<Episode> episodes3 = ea.aggregateEvents(events, 150, 1409589263988L, "place", null, null, "");
        assertEquals("foo",(new OddUtil().getDeepProperty(ep2.asMap(), "customDataWatchValues.wotsit")).toString());
        assertEquals("B",(new OddUtil().getDeepProperty(ep2.asMap(), "customDataWatchValues.thingum")).toString());
        assertTrue(episodes3.size()==2);
        Episode ep3a = episodes3.get(0);
        assertEquals("A0BX", ep3a.getStateCodes());
        Episode ep3b = episodes3.get(1);
        assertEquals("CX", ep3b.getStateCodes());
        
       
        
    }

    
    @Test
    public void testAggregateEventsRecreateEpisode() throws EventNotCreatedException {
        HashSet<String> events = new HashSet<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "", null, null, "");
        assertTrue(episodes.size()==1);
        Episode ep = episodes.get(0);
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
        Episode ep2 = new Episode(ep.asMap(), "", null);
        assertEquals("A0B0C", ep2.getStateCodes());
        assertTrue(1409579263000L==ep2.getFirstTagTime());
        assertTrue(1409579263300L==ep2.getLastTagTime());
        assertTrue(1409579263000L==ep2.getStartTime());
        assertEquals("revsys-master-account", ep2.getOwner());
        assertEquals("user1", ep2.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep2.getSeries());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertTrue(ep2.isOpen());
        System.out.println(ep2.asMap());
    }
    

    @Test
    public void testAggregateEventsIncrementally() throws EventNotCreatedException, IOException, ParseException {
        ArrayList<String> events = new ArrayList<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        Map<String, Object> episodeMap = null;
        Episode ep = null;
        for (String event: events){
            ep = ea.incrementEpisode(event, episodeMap, new HashMap());
            episodeMap = ep.asMap();
        }
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
    }
    
    @Test
    public void testAggregateEventsIncrementallyPlusBaggage() throws EventNotCreatedException, IOException, ParseException {
        ArrayList<String> events = new ArrayList<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        String extraEvent = "{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}";
        EpisodeAggregator ea = new EpisodeAggregator();
        Map<String, Object> episodeMap = null;
        Episode ep = null;
        for (String event: events){
            ep = ea.incrementEpisode(event, episodeMap, new HashMap());
            episodeMap = ep.asMap();
        }
        episodeMap.put("baggage", "Some extra data to be preserved");
        ep = ea.incrementEpisode(extraEvent, episodeMap, new HashMap());
        episodeMap = ep.asMap();
        System.out.println(episodeMap.toString());
        assertEquals("Some extra data to be preserved", episodeMap.get("baggage"));
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263000L==ep.getFirstTagTime());
        assertTrue(1409579263300L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
    }
    

    @Test
    public void testAggregateEventsIncrementallyOutOfOrder() throws EventNotCreatedException, IOException, ParseException {
        ArrayList<String> events = new ArrayList<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263500\","
                + "\"state\": \"Home\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        Map<String, Object> episodeMap = null;
        Episode ep = null;
        for (String event: events){
            ep = ea.incrementEpisode(event, episodeMap, new HashMap());
            episodeMap = ep.asMap();
        }
        System.out.println(episodeMap);
        assertEquals("A0B0C", ep.getStateCodes());
        assertTrue(1409579263100L==ep.getFirstTagTime());
        assertTrue(1409579263500L==ep.getLastTagTime());
        assertTrue(1409579263000L==ep.getStartTime());
        assertEquals("revsys-master-account", ep.getOwner());
        assertEquals("user1", ep.getAgent());
        assertEquals("e47877b6-4f17-4d2d-a6f3-3d35aa919be6", ep.getSeries());
        assertTrue(1409579263300L==ep.getEndTime());
        assertTrue(300L==ep.getDuration());
        assertTrue(ep.isOpen());
        System.out.println(ep.asMap());
    }
    
    
    @Test
    public void testAggregateEventsIncrementallyAsMapTagWrapFalse() throws EventNotCreatedException, IOException, ParseException {
        ArrayList<String> events = new ArrayList<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        Map<String, Object> episodeMap = null;
        ArrayList<Map> episodeMaps = null;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("tagWrap", "false");
        for (String event: events){
            episodeMaps = ea.incrementAggregation(event, episodeMap, options);
            episodeMap = (Map<String, Object>) episodeMaps.get(0);
        }
        System.out.println(episodeMap);
        assertEquals("A0B0C", episodeMap.get("stateCodes"));
        assertTrue(1409579263000L==(Long)episodeMap.get("firstTagTime"));
    }
    
    @Test
    public void testAggregateEventsIncrementallyAsMapTagWrapTrue() throws EventNotCreatedException, IOException, ParseException {
        ArrayList<String> events = new ArrayList<String>();
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"A\","
                + "\"_id\": \"540478ff6d9f1cf545423e6c\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263000\","
                + "\"tagTime\": \"1409579263000\","
                + "\"state\": \"Home\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"B\","
                + "\"_id\": \"540478ff6d9f1cf545423e6d\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263100\","
                + "\"tagTime\": \"1409579263100\","
                + "\"state\": \"Info\"}");
        events.add("{\"accountId\": \"revsys-master-account\","
                + "\"code\": \"C\","
                + "\"_id\": \"540478ff6d9f1cf545423e6e\","
                + "\"sessionId\": \"e47877b6-4f17-4d2d-a6f3-3d35aa919be6\","
                + "\"userId\": \"user1\","
                + "\"href\": \"http://www.revolutionarysystems.co.uk/\","
                + "\"time\": \"1409579263300\","
                + "\"tagTime\": \"1409579263300\","
                + "\"state\": \"Quit\"}");
        EpisodeAggregator ea = new EpisodeAggregator();
        Map<String, Object> episodeMap = null;
        ArrayList<Map> episodeMaps = null;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("tagWrap", "true");
        for (String event: events){
            episodeMaps = ea.incrementAggregation(event, episodeMap, options);
            episodeMap = (Map<String, Object>) episodeMaps.get(0).get("case");
        }
        System.out.println(episodeMap);
        assertEquals("A0B0C", episodeMap.get("stateCodes"));
        assertTrue(1409579263000L==(Long)episodeMap.get("firstTagTime"));
    }
    
}
