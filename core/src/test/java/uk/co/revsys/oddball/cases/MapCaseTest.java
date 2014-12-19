/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cases;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.revsys.oddball.util.JSONUtil;

/**
 *
 * @author Andrew
 */
public class MapCaseTest {
    
    public MapCaseTest() {
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
     * Test of getContent method, of class MapCase.
     */
    @Test
    public void testGetContent() throws InvalidCaseException {
        System.out.println("getContent");
        MapCase instance = new MapCase("{\"prop\":\"a\"}");
        String expResult = "{\"prop\":\"a\"}";
        String result = instance.getContent();
        assertEquals(expResult, result);
    }

    /**
     * Test of setContent method, of class MapCase.
     */
    @Test
    public void testSetContent() throws InvalidCaseException {
        System.out.println("setContent");
        String content = "{\"prop\":\"b\"}";
        MapCase instance = new MapCase("{\"prop\":\"a\"}");
        instance.setContent(content);
        String expResult = "{\"prop\":\"b\"}";
        String result = instance.getContent();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetContentWithQuote() throws InvalidCaseException, IOException {
        System.out.println("getContent");
        MapCase instance = new MapCase("{\"prop\":\"Type \\\"a\\\"\"}");
        String expResult = "{\"prop\":\"Type \\\"a\\\"\"}";
        String result = instance.getContent();
        System.out.println(instance.getContent());
        System.out.println(JSONUtil.json2map(instance.getContent()));
        assertEquals(expResult, result);
    }

}
