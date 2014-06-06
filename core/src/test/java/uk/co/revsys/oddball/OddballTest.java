/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.revsys.oddball.cases.Case;
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
        ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("/data/test/oddball"));
        Oddball instance = new Oddball(resourceRepository);
        Opinion result = instance.assessCase(ruleSetName, aCase);
        System.out.println(result.getLabel());
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
        ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("/data/test/oddball"));
        Oddball instance = new Oddball(resourceRepository);
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
    public void testAssessCaseOddballMongo() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";
        
        Case aCase = new StringCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        
        ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("/data/test/oddball"));
        Oddball instance = new Oddball(resourceRepository);
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
        
        Case aCase = new StringCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");
        
        ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("/data/test/oddball"));
        Oddball instance = new Oddball(resourceRepository);
        Opinion result = instance.assessCase(ruleSetName, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
    }
    
}
