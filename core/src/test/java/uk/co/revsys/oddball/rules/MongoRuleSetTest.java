/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.MapCase;

/**
 *
 * @author Andrew
 */
public class MongoRuleSetTest {
    
    public MongoRuleSetTest() {
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

    @Test
    public void testAssessCase() throws Exception {
        System.out.println("assessCase");
        Case aCase = new MapCase("{ \"platform\" : \"Win32\", \"userId\" : \"10A\" }");
        MongoRuleSet instance = new MongoRuleSet("Test", true);
        instance.addRule(new MongoRule("{\"platform\": {$in:[\"Win32\", \"Win64\"]}}", "WinXX"));
        Opinion result = instance.assessCase(aCase, null, "Test", RuleSet.ALWAYSPERSIST, null, null, null, false);
        assertTrue(result.getLabel().contains("WinXX"));
        assertTrue(result.getEnrichedCase("Test", aCase, false, null).contains("Test"));
        MongoDBHelper assessDb = instance.getAssess();
        assertFalse(assessDb.testCase("{ \"ruleSet\" : \"Test\"}"));
        assertFalse(assessDb.testCase("{ \"case\" : { \"platform\" : \"Win32\", \"userId\" : \"10A\" } }"));
        MongoDBHelper persistDb = instance.getPersist();
        assertTrue(persistDb.testCase("{ \"ruleSet\" : \"Test\"}"));
        assertTrue(persistDb.testCase("{ \"case\" : { \"platform\" : \"Win32\", \"userId\" : \"10A\" } }"));
    }


}
