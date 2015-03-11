/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
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
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "");
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
        List<Episode> episodes2 = ea.aggregateEvents(events, 30000, 1409589263988L, "");
        assertTrue(episodes2.size()==1);
        Episode ep2 = episodes2.get(0);
        assertEquals("A0B0CX", ep2.getStateCodes());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertFalse(ep2.isOpen());
        System.out.println(ep2.asMap());
        List<Episode> episodes3 = ea.aggregateEvents(events, 150, 1409589263988L, "");
        assertTrue(episodes3.size()==2);
        Episode ep3a = episodes3.get(0);
        assertEquals("A0BX", ep3a.getStateCodes());
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
        List<Episode> episodes = ea.aggregateEvents(events, 30000, 1409579264000L, "place");
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
        List<Episode> episodes2 = ea.aggregateEvents(events, 30000, 1409589263988L,"place");
        assertTrue(episodes2.size()==1);
        Episode ep2 = episodes2.get(0);
        assertEquals("A0B0CX", ep2.getStateCodes());
        assertTrue(1409579263300L==ep2.getEndTime());
        assertTrue(300L==ep2.getDuration());
        assertFalse(ep2.isOpen());
        System.out.println(ep2.asMap());
        assertEquals("[paris, lyon, lyon]",(new OddUtil().getDeepProperty(ep2.asMap(), "watches.place")).toString());
        assertEquals("paris",(new OddUtil().getDeepProperty(ep2.asMap(), "watchValues.place")).toString());
        assertEquals("{valueChanged-place={1=paris,lyon}}",(new OddUtil().getDeepProperty(ep2.asMap(), "alerts")).toString());
        List<Episode> episodes3 = ea.aggregateEvents(events, 150, 1409589263988L, "place");
        assertTrue(episodes3.size()==2);
        Episode ep3a = episodes3.get(0);
        assertEquals("A0BX", ep3a.getStateCodes());
        Episode ep3b = episodes3.get(1);
        assertEquals("CX", ep3b.getStateCodes());
        
       
        
    }

    
    
    
}
