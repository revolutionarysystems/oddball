/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
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
    public void testAggregateSignals() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        Map cityMap =(Map) summaryMap.get("city");
//        assertEquals(3, cityMap.get("london"));

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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        cityMap =(Map) summaryMap.get("city");
//        assertEquals(2, cityMap.get("london"));
        
    }
        
    @Test
    public void testAggregateSignalsNumerics() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"response\": 100,"
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": 150,"
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": 90,"
                + "\"city\": \"london\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": 125,"
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": 180,"
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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        Map cityMap =(Map) summaryMap.get("city");
//        assertEquals(3, cityMap.get("london"));

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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
        Map responseMap =(Map) summaryMap.get("response");
        assertEquals("152.5", responseMap.get("ave"));
        
    }
        

    @Test
    public void testAggregateSignalsMissingField() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        Map cityMap =(Map) summaryMap.get("city");
//        assertEquals(3, cityMap.get("london"));

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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        cityMap =(Map) summaryMap.get("city");
//        assertEquals(2, cityMap.get("london"));
        
    }
        
    
    
    @Test
    public void testAggregateSignalsWithStats() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
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
        assertEquals("129.0", statsMap.get("ave"));
    }
        
    @Test
    public void testAggregateSignalsWithStats2() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"response\": \"180\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef4.json");
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
        assertEquals("129.0", statsMap.get("ave"));
        assertEquals("88625.0", statsMap.get("sumsquares"));
        assertEquals("1355.0", statsMap.get("var"));
        assertEquals("36.81032", ((String)statsMap.get("std")).substring(0, 8));
    }
        
       
    @Test
    public void testAggregateSignalsWithLogNormalStats() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"response\": 100,"
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": 100,"
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": 150,"
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": 90,"
                + "\"city\": \"london\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": 125,"
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": 180,"
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef5.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd",  "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        System.out.println(summaries.size());
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
        assertEquals("test-account", (String)summaryMap.get("owner"));
        Map statsMap =(Map) summaryMap.get("response");
        assertEquals("5", statsMap.get("nonNulls"));
        assertEquals("10.482", ((String)statsMap.get("total")).substring(0, 6));
        assertEquals("2.0965", ((String)statsMap.get("avelog")).substring(0, 6));
        assertEquals("22.037", ((String)statsMap.get("sumsquares")).substring(0, 6));
        assertEquals("0.0152", ((String)statsMap.get("varlog")).substring(0, 6));
        assertEquals("0.1235", ((String)statsMap.get("stdlog")).substring(0, 6));
        assertEquals("124.88", ((String)statsMap.get("centre")).substring(0, 6));
        assertEquals("93.95", ((String)statsMap.get("lowMargin")).substring(0, 5));
        assertEquals("165.99", ((String)statsMap.get("highMargin")).substring(0, 6));
    }
        
       
       
    @Test
    public void testAggregateSignalsNestedObjects() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
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
//        assertEquals(3, cityMap.get("london"));
//        assertEquals(3, cityMap.get("london"));
//        Map stateMap =(Map) summaryMap.get("state");
//        assertEquals(3, stateMap.get("login"));

        
    }
        
        
    @Test
    public void testAggregateSignalsEmpty() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
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
    

    @Test
    public void testAggregateSignalsLevelledCollator() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        ArrayList<String> signals = new ArrayList<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"100\","
                + "\"location\": \"uk.eng.london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"150\","
                + "\"location\": \"uk.eng.london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"90\","
                + "\"location\": \"uk.eng.bristol\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": \"125\","
                + "\"location\": \"uk.eng.bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"uk.eng.manchester\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"uk.sct.glasgow\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"fr.101.lyon\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef7.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        Map cityMap =(Map) summaryMap.get("city");
//        assertEquals(3, cityMap.get("london"));

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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        cityMap =(Map) summaryMap.get("city");
//        assertEquals(2, cityMap.get("london"));
        
    }
        

    @Test
    public void testAggregateSignalsLevelledCollatorDiffSeparator() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        ArrayList<String> signals = new ArrayList<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"100\","
                + "\"location\": \"uk/eng/london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"150\","
                + "\"location\": \"uk/eng/london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"90\","
                + "\"location\": \"uk/eng/bristol\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": \"125\","
                + "\"location\": \"uk/eng/bristol\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"uk/eng/manchester\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"uk/sct/glasgow\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"fr/101/lyon\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef9.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        Map cityMap =(Map) summaryMap.get("city");
//        assertEquals(3, cityMap.get("london"));

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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        cityMap =(Map) summaryMap.get("city");
//        assertEquals(2, cityMap.get("london"));
        
    }
        

    
    @Test
    public void testAggregateSignalsLevelledCollatorReversed() throws EventNotCreatedException, AggregationException, IOException, InvalidTimePeriodException {
        ArrayList<String> signals = new ArrayList<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"100\","
                + "\"location\": \"london.eng.uk\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"150\","
                + "\"location\": \"london.eng.uk\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"90\","
                + "\"location\": \"bristol.eng.uk\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"response\": \"125\","
                + "\"location\": \"bristol.eng.uk\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"manchester.eng.uk\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"glasgow.sct.uk\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"180\","
                + "\"location\": \"lyon.101.fr\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}");
        SummaryAggregator sa = new SummaryAggregator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef8.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("division", "2000");
        options.put("owner", "test-account");
        List<Map> summaries = sa.aggregateCases(signals, options, resourceRepository);
        System.out.println("summaries");
        System.out.println(summaries);
        assertTrue(summaries.size()==1);
        Map summaryMap = summaries.get(0);
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        Map cityMap =(Map) summaryMap.get("city");
//        assertEquals(3, cityMap.get("london"));

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
//        assertEquals("test-account", (String)summaryMap.get("owner"));
//        cityMap =(Map) summaryMap.get("city");
//        assertEquals(2, cityMap.get("london"));
        
    }
        

}
