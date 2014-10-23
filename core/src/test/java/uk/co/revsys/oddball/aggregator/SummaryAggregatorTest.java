/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.LocalDiskResourceRepository;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class SummaryAggregatorTest{
    
    public SummaryAggregatorTest() {
    }

    ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("src/test/resources"));

    
    @Test
    public void testAggregateSignals() throws EventNotCreatedException, AggregationException, IOException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
        assertEquals("test-account", (String)summaryMap.get("owner"));
        Map cityMap =(Map) summaryMap.get("city");
        assertEquals("3", cityMap.get("london"));

        SummaryAggregator sa2 = new SummaryAggregator();
        options.put("periodStart", "1000001");
        options.put("periodEnd",   "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        summaries = sa2.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        summaryMap = summaries.get(0);
        assertEquals("test-account", (String)summaryMap.get("owner"));
        cityMap =(Map) summaryMap.get("city");
        assertEquals("2", cityMap.get("london"));
        
    }
        
    @Test
    public void testAggregateSignalsWithStats() throws EventNotCreatedException, AggregationException, IOException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef2.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
        assertEquals("test-account", (String)summaryMap.get("owner"));
        Map statsMap =(Map) summaryMap.get("response");
        assertEquals("5", statsMap.get("nonNulls"));
        assertEquals("645.0", statsMap.get("total"));
    }
        
       
    @Test
    public void testAggregateSignalsNestedObjects() throws EventNotCreatedException, AggregationException, IOException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"response\": \"100\","
                + "\"location\": {\"city\": \"london\"},"
                + "\"case\": {\"process\": {\"state\": \"login\"}},"
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"150\","
                + "\"location\": {\"city\": \"london\"},"
                + "\"case\": {\"process\": {\"state\": \"login\"}},"
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"90\","
                + "\"location\": {\"city\": \"london\"},"
                + "\"case\": {\"process\": {\"state\": \"faq\"}},"
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": \"125\","
                + "\"location\": {\"city\": \"bristol\"},"
                + "\"case\": {\"process\": {\"state\": \"login\"}},"
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": {\"city\": \"bristol\"},"
                + "\"case\": {\"process\": {\"state\": \"buy\"}},"
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef3.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
        assertEquals("test-account", (String)summaryMap.get("owner"));
        Map cityMap =(Map) summaryMap.get("city");
        assertEquals("3", cityMap.get("london"));
        Map stateMap =(Map) summaryMap.get("state");
        assertEquals("3", stateMap.get("login"));

        
    }
        
        
    @Test
    public void testAggregateSignalsEmpty() throws EventNotCreatedException, AggregationException, IOException {
        HashSet<String> signals = new HashSet<String>();
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
        assertEquals("test-account", (String)summaryMap.get("owner"));
        Map cityMap =(Map) summaryMap.get("city");
        assertEquals(null, cityMap.get("london"));

    }
    
}
