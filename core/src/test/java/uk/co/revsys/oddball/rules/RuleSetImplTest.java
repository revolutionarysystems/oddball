/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.StringCase;

/**
 *
 * @author Andrew
 */
public class RuleSetImplTest {
    
    public RuleSetImplTest() {
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
     * Test of addRule method, of class RuleSetImpl.
     */
    @Test
    public void testAddRule() throws Exception{
        System.out.println("addRule");
        RuleSetImpl instance = new RuleSetImpl("Test", true, "", 0);
        instance.addRule(new RegExRule(".*", "string"));
        instance.addRule(new RegExRule("a.*", "aString"));
        Set rules = instance.getRules();
        assertTrue(rules.size()==2);
    }

    /**
     * Test of assessCase method, of class RuleSetImpl.
     */
    @Test
    public void testAssessCase() throws Exception{
        System.out.println("assessCase");
        Case aCase = new StringCase("abc123");
        RuleSetImpl instance = new RuleSetImpl("Test", true, "", 0);
        instance.addRule(new RegExRule(".*", "string"));
        instance.addRule(new RegExRule("a.*", "aString"));
        Opinion result = instance.assessCase(aCase, null, "Test", RuleSet.ALWAYSPERSIST, null);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        System.out.println(result.getEnrichedCase("Test", aCase));
        assertTrue(result.getEnrichedCase("Test", aCase).contains("Test"));
    }

    /**
     * Test of assessCase method, of class RuleSetImpl.
     */
//    @Test
//    public void testAssessCasePersistentDB() throws Exception{
//        System.out.println("assessCase");
//        Case aCase = new StringCase("abc123");
//        RuleSetImpl instance = new RuleSetImpl("ob-regex-tests", false, "localhost", 27017);
//        instance.addRule(new RegExRule(".*", "string"));
//        instance.addRule(new RegExRule("a.*", "aString"));
//        Opinion result = instance.assessCase(aCase, null, "Test", RuleSet.ALWAYSPERSIST, null);
//        assertTrue(result.getLabel().contains("string"));
//        assertTrue(result.getLabel().contains("aString"));
//        System.out.println(result.getEnrichedCase("Test", aCase));
//        assertTrue(result.getEnrichedCase("Test", aCase).contains("Test"));
//        instance.getPersist().dropCases();
//    }

}
