/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.identifier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.co.revsys.oddball.FilterException;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.OwnerMissingException;
import uk.co.revsys.oddball.ProcessorNotLoadedException;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.aggregator.*;
import uk.co.revsys.oddball.bins.BinSetNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.resource.repository.LocalDiskResourceRepository;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class SummaryIdentifierTest{
    
    public SummaryIdentifierTest() {
    }

    ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("src/test/resources"));

    
    @Test
    public void testIdentifySignals() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException, InvalidCaseException, BinSetNotLoadedException, TransformerNotLoadedException, RuleSetNotLoadedException, IdentificationSchemeNotLoadedException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, OwnerMissingException {
        HashSet<String> signals = new HashSet<String>();
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"time\": 1000000,"
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"ref\": \"1234\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"time\": 1001000,"
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"180\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"chrome\"}");
        String signalOfInterest = "{\"case\":{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"chrome\"}}";
        //signals.add(signalOfInterest);
        String ruleSetName="TestIdentifier.txt";
        for (String signal:signals){
            Case aCase = new MapCase(signal);
            Collection<String> result = instance.assessCase(ruleSetName, null, null, aCase);
            System.out.println("Added");
            System.out.println(result);
            
        }
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef6.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("identificationPeriod", "1000Y");
        options.put("identificationScheme", "idScheme.scheme");
        options.put("owner", "_all");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println("Signals");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        SummaryIdentifier si = new SummaryIdentifier();
        Map identification = si.identify(instance.reloadRuleSet("TestIdentifier.txt"), signalOfInterest, cases0, options, instance, resourceRepository);
        System.out.println("identification");
        System.out.println(identification);
        assertEquals(2, ((Map)((Map)identification.get("identification")).get("ref")).get("count"));
        assertEquals(0.25, ((Map)((Map)identification.get("identification")).get("ref")).get("power"));
        assertEquals(3, ((Map)((Map)identification.get("identification")).get("city")).get("count"));
    }

    @Test
    public void testIdentifySignalsDataMissing() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException, InvalidCaseException, BinSetNotLoadedException, TransformerNotLoadedException, RuleSetNotLoadedException, IdentificationSchemeNotLoadedException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, OwnerMissingException {
        HashSet<String> signals = new HashSet<String>();
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"time\": 1000000,"
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"ref\": \"1234\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"time\": 1001000,"
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"180\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"chrome\"}");
        String signalOfInterest = "{\"case\":{\"accountId\": \"revsys-master-account\"," // missing city
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"157\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"chrome\"}}";
        //signals.add(signalOfInterest);
        String ruleSetName="TestIdentifier.txt";
        for (String signal:signals){
            Case aCase = new MapCase(signal);
            Collection<String> result = instance.assessCase(ruleSetName, null, null, aCase);
            System.out.println("Added");
            System.out.println(result);
            
        }
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef6.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("identificationPeriod", "1000Y");
        options.put("identificationScheme", "idScheme.scheme");
        options.put("owner", "_all");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println("Signals");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        SummaryIdentifier si = new SummaryIdentifier();
        Map identification = si.identify(instance.reloadRuleSet("TestIdentifier.txt"), signalOfInterest, cases0, options, instance, resourceRepository);
        System.out.println("identification");
        System.out.println(identification);
        assertEquals(1, ((Map)((Map)identification.get("identification")).get("ref")).get("count"));
        assertTrue(0.001> 0.333 - (Double)((Map)((Map)identification.get("identification")).get("ref")).get("power"));
        assertTrue(((Map)((Map)identification.get("identification")).get("city"))==null);
    }

    @Test
    public void testIdentifySignalsDataMissing2() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException, InvalidCaseException, BinSetNotLoadedException, TransformerNotLoadedException, RuleSetNotLoadedException, IdentificationSchemeNotLoadedException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, OwnerMissingException {
        HashSet<String> signals = new HashSet<String>();
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000000\","
                + "\"time\": 1000000,"
                + "\"response\": \"100\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"100\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"150\","
                + "\"state\": \"login\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"90\","
                + "\"ref\": \"1234\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001000\","
                + "\"time\": 1001000,"
                + "\"response\": \"125\","
                + "\"state\": \"login\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"180\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"chrome\"}");
        String signalOfInterest = "{\"case\":{\"accountId\": \"revsys-master-account\"," // missing city
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"chrome\"}}";
        //signals.add(signalOfInterest);
        String ruleSetName="TestIdentifier.txt";
        for (String signal:signals){
            Case aCase = new MapCase(signal);
            Collection<String> result = instance.assessCase(ruleSetName, null, null, aCase);
            System.out.println("Added");
            System.out.println(result);
            
        }
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef6.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("identificationPeriod", "1000Y");
        options.put("identificationScheme", "idScheme.scheme");
        options.put("owner", "_all");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println("Signals");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        SummaryIdentifier si = new SummaryIdentifier();
        Map identification = si.identify(instance.reloadRuleSet("TestIdentifier.txt"), signalOfInterest, cases0, options, instance, resourceRepository);
        System.out.println("identification");
        System.out.println(identification);
        assertEquals(1, ((Map)((Map)identification.get("identification")).get("ref")).get("count"));
        assertTrue(0.001> 0.25 - (Double)((Map)((Map)identification.get("identification")).get("ref")).get("power"));
        assertTrue(((Map)((Map)identification.get("identification")).get("city"))==null);
    }

 
    
    @Test
    public void testIdentifySignals2() throws EventNotCreatedException, ComparisonException, IOException, InvalidTimePeriodException, InvalidCaseException, BinSetNotLoadedException, TransformerNotLoadedException, RuleSetNotLoadedException, IdentificationSchemeNotLoadedException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, OwnerMissingException {
        HashSet<String> signals = new HashSet<String>();
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000001\","
                + "\"time\": 1000001,"
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"time\": 1000002,"
                + "\"response\": \"100\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"time\": 1000002,"
                + "\"response\": \"150\","
                + "\"city\": \"london\","
                + "\"state\": \"login\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"ie\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1000002\","
                + "\"time\": 1000002,"
                + "\"response\": \"90\","
                + "\"city\": \"london\","
                + "\"ref\": \"1234\","
                + "\"state\": \"faq\","
                + "\"agent-name\": \"firefox\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001001\","
                + "\"time\": 1001001,"
                + "\"response\": \"125\","
                + "\"city\": \"bristol\","
                + "\"state\": \"login\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"opera\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"time\": 1001002,"
                + "\"response\": \"180\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1236\","
                + "\"agent-name\": \"chrome\"}");
        signals.add("{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"time\": 1001002,"
                + "\"response\": \"157\","
                + "\"city\": \"bristol\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1235\","
                + "\"agent-name\": \"chrome\"}");
        String signalOfInterest = "{\"case\":{\"accountId\": \"revsys-master-account\","
                + "\"timestamp\": \"1001002\","
                + "\"time\": 1001002,"
                + "\"response\": \"157\","
                + "\"city\": \"london\","
                + "\"state\": \"buy\","
                + "\"ref\": \"1234\","
                + "\"agent-name\": \"chrome\"}}";
        //signals.add(signalOfInterest);
        String ruleSetName="TestIdentifier.txt";
        for (String signal:signals){
            Case aCase = new MapCase(signal);
            Collection<String> result = instance.assessCase(ruleSetName, null, null, aCase);
            System.out.println("Added");
            System.out.println(result);
            
        }
        Map<String, String> options = new HashMap<String, String>();
        options.put("summaryDefinition", "testSummaryDef6.json");
        options.put("periodStart", "1000000");
        options.put("periodEnd", "1002000");
        options.put("identificationPeriod", "1000Y");
        options.put("identificationScheme", "idScheme.scheme");
        options.put("identificationLabel", "alternateId");
        options.put("owner", "_all");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println("Signals");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        SummaryIdentifier si = new SummaryIdentifier();
        Map identification = si.identify(instance.reloadRuleSet("TestIdentifier.txt"), signalOfInterest, cases0, options, instance, resourceRepository);
        System.out.println("identification");
        System.out.println(identification);
        assertEquals(3, ((Map)((Map)identification.get("alternateId")).get("ref")).get("count"));
        assertEquals(0.5, ((Map)((Map)identification.get("alternateId")).get("ref")).get("power"));
        assertEquals(4, ((Map)((Map)identification.get("alternateId")).get("city")).get("count"));
        assertEquals("{\"$or\":[{\"case.ref\":\"1234\"}]}", ((Map)((Map)identification.get("alternateId")).get("combined")).get("query"));
    }

  
}