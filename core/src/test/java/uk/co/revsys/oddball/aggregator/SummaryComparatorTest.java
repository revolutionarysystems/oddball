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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.resource.repository.LocalDiskResourceRepository;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class SummaryComparatorTest{
    
    public SummaryComparatorTest() {
    }

    ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("src/test/resources"));

    
    @Test
    public void testCompareSignals() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException {
        HashSet<String> signals = new HashSet<String>();
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
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
        String signalOfInterest = "{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}";
        SummaryComparator sc = new SummaryComparator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("owner", "test-account");
        Map caseWithComparison = sc.compare(signalOfInterest, signals, options, resourceRepository);
        Map comparison=(Map)caseWithComparison.get("comparison");
        System.out.println("comparison");
        System.out.println(comparison);
        assertEquals("chrome", ((Map)comparison.get("agent-name")).get("value"));
        assertEquals(3, ((Map)comparison.get("agent-name")).get("count"));
        assertEquals((float)0.5, ((Map)comparison.get("agent-name")).get("proportion"));
        assertTrue(0.0001> Math.abs(1.0 - (Double)((Map)comparison.get("agent-name")).get("info")));
        assertTrue(0.0001> Math.abs(-0.79248 - (Double)((Map)comparison.get("agent-name")).get("relInfo")));
    }

    
    @Test
    public void testCompareSignals2() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException {
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
        String signalOfInterest = "{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"seamonkey\"}";
        SummaryComparator sc = new SummaryComparator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("owner", "test-account");
        Map caseWithComparison = sc.compare(signalOfInterest, signals, options, resourceRepository);
        Map comparison=(Map)caseWithComparison.get("comparison");
        System.out.println("comparison");
        System.out.println(comparison);
        assertEquals("seamonkey", ((Map)comparison.get("agent-name")).get("value"));
        assertEquals(0, ((Map)comparison.get("agent-name")).get("count"));
        assertEquals((float)0.0, ((Map)comparison.get("agent-name")).get("proportion"));
    }
    
    @Test
    public void testCompareSignalsWStats() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException {
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
        String signalOfInterest = "{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}";
        SummaryComparator sc = new SummaryComparator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef2.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("owner", "test-account");
        Map caseWithComparison = sc.compare(signalOfInterest, signals, options, resourceRepository);
        Map comparison=(Map)caseWithComparison.get("comparison");
        System.out.println("comparison");
        System.out.println(comparison);
        assertEquals("chrome", ((Map)comparison.get("agent-name")).get("value"));
        assertEquals(2, ((Map)comparison.get("agent-name")).get("count"));
        assertEquals((float)0.4, ((Map)comparison.get("agent-name")).get("proportion"));
        assertEquals("157", ((Map)comparison.get("response")).get("value"));
        assertEquals((float)1.2170542, ((Map)comparison.get("response")).get("ratioToAve"));
        assertEquals((float)1.7444445, ((Map)comparison.get("response")).get("ratioToMin"));
        assertEquals((float)0.87222224, ((Map)comparison.get("response")).get("ratioToMax"));
    }
        
    @Test
    public void testCompareSignalsWStats2() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException {
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
        String signalOfInterest = "{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}";
        SummaryComparator sc = new SummaryComparator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef4.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("owner", "test-account");
        Map caseWithComparison = sc.compare(signalOfInterest, signals, options, resourceRepository);
        Map comparison=(Map)caseWithComparison.get("comparison");
        System.out.println("comparison");
        System.out.println(comparison);
        assertEquals("chrome", ((Map)comparison.get("agent-name")).get("value"));
        assertEquals(2, ((Map)comparison.get("agent-name")).get("count"));
        assertEquals((float)0.4, ((Map)comparison.get("agent-name")).get("proportion"));
        assertEquals("157", ((Map)comparison.get("response")).get("value"));
        assertEquals((float)1.2170542, ((Map)comparison.get("response")).get("ratioToAve"));
        assertEquals((float)1.7444445, ((Map)comparison.get("response")).get("ratioToMin"));
        assertEquals((float)0.87222224, ((Map)comparison.get("response")).get("ratioToMax"));
        assertEquals((double)0.7606561548938257, ((Map)comparison.get("response")).get("standardisedDeviation"));
        assertEquals(0, ((Map)comparison.get("response")).get("deviationBand"));
    }
        
    @Test
    public void testCompareSignalsWStats3() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException {
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
        String signalOfInterest = "{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"agent-name\": \"chrome\"}";
        SummaryComparator sc = new SummaryComparator();
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef5.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("owner", "test-account");
        Map caseWithComparison = sc.compare(signalOfInterest, signals, options, resourceRepository);
        Map comparison=(Map)caseWithComparison.get("comparison");
        System.out.println("comparison");
        System.out.println(comparison);
        assertEquals("chrome", ((Map)comparison.get("agent-name")).get("value"));
        assertEquals(2, ((Map)comparison.get("agent-name")).get("count"));
        assertEquals((float)0.4, ((Map)comparison.get("agent-name")).get("proportion"));
        assertEquals("157", ((Map)comparison.get("response")).get("value"));
        assertEquals("1.04741", ((Map)comparison.get("response")).get("ratioLogToAveLog").toString().substring(0, 7));
        assertEquals((float)1.7444445, ((Map)comparison.get("response")).get("ratioToMin"));
        assertEquals((float)0.87222224, ((Map)comparison.get("response")).get("ratioToMax"));
        assertEquals("0.8042", ((Map)comparison.get("response")).get("standardisedDeviationLog").toString().substring(0, 6));
        assertEquals(0, ((Map)comparison.get("response")).get("deviationBand"));
    }
        
    
}