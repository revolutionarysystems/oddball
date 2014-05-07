/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

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
public class RegExRuleTest {
    
    public RegExRuleTest() {
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
     * Test of apply method, of class RegExRule.
     */
    @Test
    public void testApply() {
        System.out.println("apply");
        Case aCase = new StringCase("abc123");
        RegExRule instance = new RegExRule(".*", "string");
        Assessment expResult = new Assessment("abc123", ".*", "string");
        Assessment result = (Assessment) instance.apply(aCase);
        assertEquals(expResult.getCaseStr(), result.getCaseStr());
        assertEquals(expResult.getRuleStr(), result.getRuleStr());
        assertEquals(expResult.getLabelStr(), result.getLabelStr());
    }

    /**
     * Test of getRegEx method, of class RegExRule.
     */
    @Test
    public void testGetRegEx() {
        System.out.println("getRegEx");
        RegExRule instance = new RegExRule("*", "string");
        String expResult = "*";
        String result = instance.getRegEx();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRegEx method, of class RegExRule.
     */
    @Test
    public void testSetRegEx() {
        System.out.println("setRegEx");
        String regEx = "a.*";
        RegExRule instance = new RegExRule(".*", "string");
        instance.setRegEx(regEx);
        String expResult = "a.*";
        String result = instance.getRegEx();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLabel method, of class RegExRule.
     */
    @Test
    public void testGetLabel() {
        System.out.println("getLabel");
        RegExRule instance = new RegExRule("*", "string");
        String expResult = "string";
        String result = instance.getLabel();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLabel method, of class RegExRule.
     */
    @Test
    public void testSetLabel() {
        System.out.println("setLabel");
        String label = "aString";
        RegExRule instance = new RegExRule("*", "string");
        instance.setLabel(label);
        String expResult = "aString";
        String result = instance.getLabel();
        assertEquals(expResult, result);
    }
    
}
