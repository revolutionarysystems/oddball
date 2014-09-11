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
import uk.co.revsys.oddball.cases.MapCase;

/**
 *
 * @author Andrew
 */
public class MongoRuleTest {
    
    public MongoRuleTest() {
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
     * Test of apply method, of class MongoRule.
     */
    @Test
    public void testApply() throws Exception{
        System.out.println("apply");
        Case aCase = new MapCase("{\"browser\":\"chrome\"}");
        MongoRule instance = new MongoRule("{\"browser\":\"chrome\"}", "chrome");
        MongoRuleSet mrs = new MongoRuleSet("test", true, "", 0);
        String caseId = mrs.getAssess().insertCase(((MapCase)aCase).getContent());
        Assessment expResult = new Assessment("{\"browser\":\"chrome\"}", "{\"browser\":\"chrome\"}", "chrome");
        Assessment result = (Assessment) instance.apply(aCase, mrs, caseId);
        assertEquals(expResult.getCaseStr(), result.getCaseStr());
        assertEquals(expResult.getRuleStr(), result.getRuleStr());
        assertEquals(expResult.getLabelStr(), result.getLabelStr());
    }

    /**
     * Test of apply method, of class MongoRule.
     */
    @Test
    public void testApplyMismatch() throws Exception {
        System.out.println("apply");
        Case aCase = new MapCase("{\"browser\":\"IE6\"}");
        MongoRule instance = new MongoRule("{\"browser\":\"chrome\"}", "chrome");
        MongoRuleSet mrs = new MongoRuleSet("test", true, "", 0);
        String caseId = mrs.getAssess().insertCase(((MapCase)aCase).getContent());
        Assessment expResult = new Assessment("{\"browser\":\"IE6\"}", "{\"browser\":\"chrome\"}", null);
        Assessment result = (Assessment) instance.apply(aCase, mrs, caseId);
        mrs.getAssess().removeCase(caseId);
        assertEquals(expResult.getCaseStr(), result.getCaseStr());
        assertEquals(expResult.getRuleStr(), result.getRuleStr());
        assertEquals(expResult.getLabelStr(), result.getLabelStr());
    }

    /**
     * Test of getRuleString method, of class MongoRule.
     */
    @Test
    public void testGetRuleString() {
        System.out.println("getRuleString");
        MongoRule instance = new MongoRule("*", "string");
        String expResult = "*";
        String result = instance.getRuleString();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRuleString method, of class MongoRule.
     */
    @Test
    public void testSetRuleString() throws Exception{
        System.out.println("setRuleString");
        String ruleString = "rule.txt";
        MongoRule instance = new MongoRule();
        instance.setRuleString(ruleString, null);
        String expResult = "rule.txt";
        String result = instance.getRuleString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLabel method, of class MongoRule.
     */
    @Test
    public void testGetLabel() {
        System.out.println("getLabel");
        MongoRule instance = new MongoRule("*", "string");
        String expResult = "string";
        String result = instance.getLabel();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLabel method, of class MongoRule.
     */
    @Test
    public void testSetLabel() {
        System.out.println("setLabel");
        String label = "aString";
        MongoRule instance = new MongoRule("*", "string");
        instance.setLabel(label);
        String expResult = "aString";
        String result = instance.getLabel();
        assertEquals(expResult, result);
    }
    
}
