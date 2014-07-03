/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball;

import java.io.File;
import java.util.Collection;
import java.util.Map;
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
import uk.co.revsys.resource.repository.LocalDiskResourceRepository;
import uk.co.revsys.resource.repository.ResourceRepository;

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
        Opinion result = instance.assessCase(ruleSetName, aCase);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        assertTrue(result.getLabel().contains("abc-ish"));
        assertFalse(result.getLabel().contains("null"));
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
        Opinion result = instance.assessCase(ruleSetName, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
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
        Opinion result = instance.assessCase(ruleSetName, aCase);
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
        Opinion result = instance.assessCase(ruleSetName, aCase);
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
    public void testAssessCaseOddballMongo() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";
        
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, aCase);
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
        Opinion result = instance.assessCase(ruleSetName, aCase);
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
        Opinion result = instance.assessCase(ruleSetName, theCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases = instance.findAllCases(ruleSetName);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    
    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQueryBeforeAssess() throws Exception {
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        String ruleSetName = "TestMongo.txt";
        System.out.println("findCasesBeforeAssess");
        Iterable<String> cases = instance.findAllCases(ruleSetName);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        assertFalse(cases.iterator().hasNext());
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");
        
        Opinion result = instance.assessCase(ruleSetName, theCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases2 = instance.findAllCases(ruleSetName);
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
        Opinion result = instance.assessCase(ruleSetName, theCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases0 = instance.findAllQueryCases(ruleSetName, "{}");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findAllQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}");
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
        System.out.println("findCases done");
    }
    

    @Test
    public void testFindDistinctQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, otherCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases0 = instance.findAllQueryCases(ruleSetName, "{}");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findDistinct(ruleSetName, Oddball.ALL, "case.sessionId", null);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
    }
    
    @Test
    public void testFindDistinctRecentQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");
        
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCase(ruleSetName, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, otherCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases0 = instance.findAllQueryCases(ruleSetName, "{}");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findDistinct(ruleSetName, Oddball.ALL, "case.sessionId", "5");
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
        Opinion result = instance.assessCase(ruleSetName, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, otherCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases0 = instance.findAllQueryCases(ruleSetName, "{}");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findDistinct(ruleSetName, "Trial", "case.sessionId", null);
        for (String aCase : cases){
            System.out.println(aCase);
        }
        
        assertTrue(cases.iterator().hasNext());
        cases = instance.findDistinct(ruleSetName, "Real", "case.sessionId", null);
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
        Opinion result = instance.assessCase(ruleSetName, theCase);
        Opinion result2 = instance.assessCase(ruleSetName, otherCase);
        instance.assessCase(ruleSetName, theCase);
        Iterable<String> cases0 = instance.findAllQueryCases(ruleSetName, "{}");
        for (String aCase : cases0){
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findDistinct(ruleSetName, "Trial", "case.sessionId", "5");
        for (String aCase : cases){
            System.out.println(aCase);
        }
        assertTrue(cases.iterator().hasNext());
        
        cases = instance.findDistinct(ruleSetName, "Real", "case.sessionId", "5");
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

        Opinion result = instance.assessCase(ruleSetName, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
        result = instance.assessCase(ruleSetName, anotherCase);

        BinSet binSet = instance.binSet;
        assertTrue(binSet!=null);
        assertTrue(binSet.getBins().size()==3);
        Bin firstBin = binSet.getBins().get("bin1");
        assertEquals("bin1", firstBin.getLabel());
        String binQuery = firstBin.getBinString();
        
        Iterable<String> cases = instance.findAllQueryCases(ruleSetName, binQuery);
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

        Opinion result = instance.assessCase(ruleSetName, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
        result = instance.assessCase(ruleSetName, anotherCase);

        BinSet binSet = instance.binSet;
        
        Iterable<String> cases = instance.findAllCasesInBin(ruleSetName, "bin1");
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases){
            System.out.println("in bin1:");
            System.out.println(foundCase);
        }
       
        cases = instance.findAllCasesInBin(ruleSetName, "bin2");
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases){
            System.out.println("in bin2:");
            System.out.println(foundCase);
        }
       
        cases = instance.findAllCasesInBin(ruleSetName, "bin3");
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases){
            System.out.println("in bin3:");
            System.out.println(foundCase);
        }
        
    }



}
