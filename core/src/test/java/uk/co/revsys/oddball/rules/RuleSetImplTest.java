/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

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
    public void testAddRule() {
        System.out.println("addRule");
        RuleSetImpl instance = new RuleSetImpl("Test");
        instance.addRule(new RegExRule(".*", "string"));
        instance.addRule(new RegExRule("a.*", "aString"));
        Set rules = instance.getRules();
        assertTrue(rules.size()==2);
    }

    /**
     * Test of assessCase method, of class RuleSetImpl.
     */
    @Test
    public void testAssessCase() {
        System.out.println("assessCase");
        Case aCase = new StringCase("abc123");
        RuleSetImpl instance = new RuleSetImpl("Test");
        instance.addRule(new RegExRule(".*", "string"));
        instance.addRule(new RegExRule("a.*", "aString"));
        Opinion result = instance.assessCase(aCase, null);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        assertTrue(result.getLabel().contains(";"));
    }

//    /**
//     * Test of getRules method, of class RuleSetImpl.
//     */
//    @Test
//    public void testGetRules() {
//        System.out.println("getRules");
//        RuleSetImpl instance = new RuleSetImpl();
//        Set<Rule> expResult = null;
//        Set<Rule> result = instance.getRules();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getName method, of class RuleSetImpl.
//     */
//    @Test
//    public void testGetName() {
//        System.out.println("getName");
//        RuleSetImpl instance = new RuleSetImpl();
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    
}
