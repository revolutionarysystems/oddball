/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.revsys.oddball.bins.Bin;
import uk.co.revsys.oddball.bins.BinSet;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.rules.Assessment;
import uk.co.revsys.oddball.rules.MongoDBHelper;
import uk.co.revsys.oddball.rules.MongoRule;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.Rule;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.LocalDiskResourceRepository;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class OddballTest {
    
    ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("src/test/resources"));
    
    public OddballTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatch() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test1.txt";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        assertTrue(result.getLabel().contains("abc-ish"));
        assertFalse(result.getLabel().contains("null"));
    }
 
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatchPersistentRules() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test1persist";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        assertTrue(result.getLabel().contains("abc-ish"));
        assertFalse(result.getLabel().contains("null"));
        instance.ruleSets.get("Test1persist").getPersist().dropCases();
    }
 
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddball() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        System.out.println(result.getEnrichedCase(ruleSetName, aCase));
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
    }
    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballAddRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
        instance.addExtraRule(ruleSetName, "", "b-rule", "b-.*", "added");
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("*odDball*"));
        assertTrue(result.getLabel().contains("b-rule"));
    }
    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballAddIgnoreRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
        instance.addExtraRule(ruleSetName, "", "*ignore*", "b-.*", "added");
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("*ignore*"));
        assertEquals("{ \"tags\" : [] }", result.getLabel());
    }
    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatchPrefixRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3.txt";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("stringType.aString"));
        assertTrue(result.getLabel().contains("anotherType.abc-ish"));
        assertFalse(result.getLabel().contains("null"));
    }
 
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballPrefix() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("other.string"));
        assertTrue(result.getLabel().contains("anotherType.odDball"));
        assertFalse(result.getLabel().contains("null"));
    }
    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballPrefixAddRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("other.string"));
        assertTrue(result.getLabel().contains("anotherType.odDball"));
        assertFalse(result.getLabel().contains("null"));
        instance.addExtraRule(ruleSetName, "anotherType.", "b-rule", "b-.*", "added");
        result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("other.string"));
        assertFalse(result.getLabel().contains("anotherType.odDball"));
        assertTrue(result.getLabel().contains("anotherType.b-rule"));
    }
    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballMongo() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";
        
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
    }
    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballMongoOddball() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";
        
        Case aCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
    }
    
    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCases() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases = instance.findCases(ruleSetName, new HashMap<String, String>());
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    
    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesDuplicates() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases = instance.findCases(ruleSetName, new HashMap<String, String>());
        int count = 0;
        for (String aCase : cases){
            System.out.println(aCase);
            count++;
        }
        
        assertTrue(cases.iterator().hasNext());
        assertTrue(count==2);
    }
    
    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesDuplicatesNoPersist() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase, RuleSet.NEVERPERSIST, null);
        Iterable<String> cases = instance.findCases(ruleSetName, new HashMap<String, String>());
        int count = 0;
        for (String aCase : cases){
            System.out.println(aCase);
            count++;
        }
        
        assertTrue(cases.iterator().hasNext());
        assertTrue(count==1);
    }
    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesDuplicatesUpdate() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");
        Case theRevisedCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\", \"orientation\":\"landscape\"}");
        Case theDifferentCase = new MapCase("{\"series\":\"237890\", \"browser\":\"firefox\", \"platform\":\"android\", \"orientation\":\"landscape\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase); // should be added
        instance.assessCase(ruleSetName, null, theRevisedCase, RuleSet.UPDATEPERSIST, "{\"case.series\":\"123789\"}"); //should match and so, update not add
        instance.assessCase(ruleSetName, null, theDifferentCase, RuleSet.UPDATEPERSIST, "{\"case.series\":\"237890\"}"); //should not match and so, be added
        Iterable<String> cases = instance.findCases(ruleSetName, new HashMap<String, String>());
        int count = 0;
        for (String aCase : cases){
            System.out.println(aCase);
            count++;
        }
        
        assertTrue(cases.iterator().hasNext());
        assertTrue(count==2);
    }
    
    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQueryBeforeAssess() throws Exception {
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        String ruleSetName = "TestMongo.txt";
        System.out.println("findCasesBeforeAssess");
        Iterable<String> cases = instance.findCases(ruleSetName, new HashMap<String, String>());
        for (String aCase : cases){
            System.out.println(aCase);
        }
        assertFalse(cases.iterator().hasNext());
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");
        
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases2 = instance.findCases(ruleSetName, new HashMap<String, String>());
        for (String aCase : cases2){
            System.out.println(aCase);
        }
        
        assertTrue(cases2.iterator().hasNext());
    }
    

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQuery() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}", new HashMap<String, String>());
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
        System.out.println("findCases done");
    }
    

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQueryWithTransform() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", "testjsont.json");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    


    
    @Test
    public void testFindDistinctQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", new HashMap<String, String>());
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    
    @Test
    public void testFindLatestQuery() throws Exception {
        System.out.println("findLatest");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, otherCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        assertTrue(cases0.iterator().hasNext());
        String latestCase = instance.findLatestQueryCase(ruleSetName, "{}", new HashMap<String, String>());
        System.out.println(latestCase);
    }
    
    @Test
    public void testFindDistinctRecentQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("recent", "5");
        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    
    @Test
    public void testFindDistinctSinceQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        long since = new Date().getTime() - 5 * 60 * 1000;
        options.put("since", Long.toString(since));
        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    
    @Test
    public void testFindDistinctQueryForOwner() throws Exception {
        System.out.println("findDistinctOwner");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"accountId\":\"Trial\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\", \"accountId\":\"Trial\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "Trial");
        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
        options.put("owner", "Real");
        cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertFalse(cases.iterator().hasNext());
    }
    

    @Test
    public void testFindDistinctRecentQueryForOwner() throws Exception {
        System.out.println("findDistinctOwner");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"accountId\":\"Trial\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\", \"accountId\":\"Trial\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "Trial");
        options.put("recent", "5");
        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        assertTrue(cases.iterator().hasNext());
        
        options.put("owner", "Real");
        options.put("recent", "5");
        cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertFalse(cases.iterator().hasNext());
    }
    
    @Test
    public void testFindDistinctSinceQueryForOwner() throws Exception {
        System.out.println("findDistinctOwner");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"accountId\":\"Trial\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\", \"accountId\":\"Trial\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, null, otherCase);
        instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "Trial");
        long since = new Date().getTime() - 5 * 60 * 1000;
        options.put("since", Long.toString(since));
        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        assertTrue(cases.iterator().hasNext());
        
        options.put("owner", "Real");
        options.put("recent", "5");
        cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertFalse(cases.iterator().hasNext());
    }
    

    @Test
    public void testLoadBins() throws Exception {
        System.out.println("loadBins");
        String binSetName = "TestBins.txt";
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        BinSet binSet = instance.loadBinSet(binSetName, resourceRepository);
        assertTrue(binSet!=null);
        assertTrue(binSet.getBins().size()==3);
        Bin firstBin = binSet.getBins().get("bin1");
        assertEquals("bin1", firstBin.getLabel());
    }

    @Test
    public void testUseBin() throws Exception {

        System.out.println("applyBins");
        String ruleSetName = "TestMongo.txt";
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        Case anotherCase = new MapCase("{\"browser\":\"IE8\", \"platform\":\"windows\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
        result = instance.assessCase(ruleSetName, null, anotherCase);

        BinSet binSet = instance.binSet;
        assertTrue(binSet!=null);
        assertTrue(binSet.getBins().size()==3);
        Bin firstBin = binSet.getBins().get("bin1");
        assertEquals("bin1", firstBin.getLabel());
        String binQuery = firstBin.getBinString();
        
        Iterable<String> cases = instance.findQueryCases(ruleSetName, binQuery, new HashMap<String, String>());
        for (String foundCase : cases){
            System.out.println(foundCase);
        }
       
        assertTrue(cases.iterator().hasNext());
        
    }


    @Test
    public void testListBins() throws Exception {

        System.out.println("ListBins");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        BinSet binSet = instance.binSet;
        assertTrue(binSet!=null);
        Collection<String> binLabels = binSet.listBinLabels();
        assertTrue(binLabels.size()==3);
        assertTrue(binLabels.contains("bin1"));
        Collection<String> binLabelsIt = instance.listBinLabels("eCK-1005");
        System.out.println(binLabelsIt);
        assertEquals(7, binLabelsIt.size());
        assertTrue(binLabelsIt.contains("Mybin1"));
    }


    @Test
    public void testFindCasesInBin() throws Exception {

        System.out.println("applyBins");
        String ruleSetName = "TestMongo.txt";
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
        result = instance.assessCase(ruleSetName, null, anotherCase);

        BinSet binSet = instance.binSet;
        
        HashMap<String, String> options = new HashMap<String, String>();
        Iterable<String> cases = instance.findCasesInBin(ruleSetName, "bin1", options);
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases){
            System.out.println("in bin1:");
            System.out.println(foundCase);
        }
       
        cases = instance.findCasesInBin(ruleSetName, "bin2", options);
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases){
            System.out.println("in bin2:");
            System.out.println(foundCase);
        }
       
        cases = instance.findCasesInBin(ruleSetName, "bin3", options);
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases){
            System.out.println("in bin3:");
            System.out.println(foundCase);
        }
        
    }

    @Test
    public void testFindDistictPropertyInBin() throws Exception {

        System.out.println("applyBins");
        String ruleSetName = "TestMongo.txt";
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleB"));
        instance.assessCase(ruleSetName, null, anotherCase);
        instance.assessCase(ruleSetName, null, yetAnotherCase);

        BinSet binSet = instance.binSet;
        
        HashMap<String, String> options = new HashMap<String, String>();
        Iterable<String> cases = instance.findCasesInBin(ruleSetName, "bin2", options);
        assertTrue(cases.iterator().hasNext());
        int count=0;
        for (String foundCase : cases){
            System.out.println("in bin2:");
            System.out.println(foundCase);
            count++;
        }
        assertTrue(count==3);

        options.put("property", "case.platform");
        Iterable<String> platforms = instance.findDistinctPropertyInBin(ruleSetName, "bin2", options);
        assertTrue(platforms.iterator().hasNext());
        count=0;
        for (String platform : platforms){
            System.out.println("in bin2:");
            System.out.println(platform);
            count++;
        }
        assertTrue(count==2);
        
        
        
    }


    @Test
    public void testAddRuleTwice() throws Exception {
        System.out.println("addRuleTwice");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        Set<Rule> rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int n = rules.size();
        System.out.println("Rules before");
        System.out.println(n);
        for (Rule rule : rules){
            System.out.println(rule.toString());
        }
        instance.addExtraRule(ruleSetName, "", "b-rule", "b-.*", "added");
        rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int m = rules.size();
        assertTrue((m-n)==1);
        instance.addExtraRule(ruleSetName, "", "B-rule", "b-.*", "added");
        rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int l = rules.size();
        assertTrue((l-m)==0);
    }


   @Test
    public void testFindRules() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        Set<Rule> rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int n = rules.size();
        System.out.println("Rules before");
        System.out.println(n);
        for (Rule rule : rules){
            System.out.println(rule.toString());
        }
        instance.addExtraRule(ruleSetName, "initial.", "b-rule", "b-.*", "added");
        instance.addExtraRule(ruleSetName, "initial.", "B-rule", "B-.*", "added");
        instance.addExtraRule(ruleSetName, "terminal.", "rule-B", ".*-B", "added");
        instance.addExtraRule(ruleSetName, "initial.", "bb-rule", "bb-.*", "inserted");
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("prefix", "initial");
        options.put("source", "added");
        Iterable<String> foundRules = instance.findRules(ruleSetName, options);
        int count = 0;
        for (String foundRule : foundRules){
            count++;
            System.out.println("found: "+foundRule);
        }
        assertTrue(count==2);
            
    }


   @Test
    public void testSaveRules() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "Test4.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, aCase);
        Set<Rule> rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int n = rules.size();
        System.out.println("Rules before");
        System.out.println(n);
        for (Rule rule : rules){
            System.out.println(rule.toString());
        }
        instance.addExtraRule(ruleSetName, "initial.", "b-rule", "b-.*", "added");
        instance.addExtraRule(ruleSetName, "initial.", "B-rule", "B-.*", "added");
        instance.addExtraRule(ruleSetName, "terminal.", "rule-B", ".*-B", "added");
        instance.addExtraRule(ruleSetName, "initial.", "bb-rule", "bb-.*", "inserted");
        instance.addExtraRule(ruleSetName, "", "bb-rule", "aabb-.*", "added");
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("prefix", null);
        options.put("source", "added");
        Iterable<String> foundRules = instance.saveRules(ruleSetName, options);
        int count = 0;
        for (String foundRule : foundRules){
            count++;
            System.out.println("found: "+foundRule);
        }
        instance.resourceRepository.delete(new Resource("", "Test4.txt.ALL.added"));
        assertTrue(count==4);
        
            
    }


    @Test
    public void testFindCaseById() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        String randomSessionId = Integer.toString(new Random().nextInt());
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\""+randomSessionId+"\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases1 = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \""+randomSessionId+"\"}", new HashMap<String, String>());
        String id = "";
        for (String aCase : cases1){
            System.out.println(aCase);
            int idStart = aCase.indexOf("\"_id\" : \"")+9;
            int idEnd = aCase.substring(idStart).indexOf("\" , \"")+idStart;
            id = aCase.substring(idStart, idEnd);
            System.out.println("id = "+id);
        }
        
//        String query = "{ \"_id\" : \"ObjectId(\\\""+id+"\\\")\"}";
//        String query = "{ \"_id\" : \"ObjectId('"+id+"')\"}";
        Iterable<String> cases2 = instance.findCaseById(ruleSetName, id, new HashMap<String, String>());
        for (String aCase : cases2){
            System.out.println(aCase);
        }
        //assertTrue(1==0);
        assertTrue(cases2.iterator().hasNext());
        System.out.println("findCases done");
    }
    

    @Test
    public void testFindCasesQueryWithTransformAggregation() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, null, theCase);
        instance.assessCase(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", "event.json");
        options.put("aggregator", "episode");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0){
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("A0B", episodeMap.get("stateCodes"));
        }
        long future = new Date().getTime()+1000000;
        options.put("timeOutReference", Long.toString(future));
        Iterable<String> cases1 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases1){
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("A0BX", episodeMap.get("stateCodes"));
        }
    }
    



}
