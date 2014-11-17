/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cases;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Andrew
 */
public class StringCaseTest {
    
    public StringCaseTest() {
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
     * Test of getContent method, of class StringCase.
     */
    @Test
    public void testGetContent() {
        System.out.println("getContent");
        StringCase instance = new StringCase("test");
        String expResult = "test";
        String result = instance.getContent();
        assertEquals(expResult, result);
    }

    /**
     * Test of setContent method, of class StringCase.
     */
    @Test
    public void testSetContent() {
        System.out.println("setContent");
        String content = "revised";
        StringCase instance = new StringCase("test");
        instance.setContent(content);
        String expResult = "revised";
        String result = instance.getContent();
        assertEquals(expResult, result);
    }
    
}
