/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cascading;

import cascading.scheme.local.TextDelimited;
import cascading.scheme.local.TextLine;
import cascading.tap.Tap;
import cascading.tap.local.FileTap;
import cascading.tuple.Fields;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Andrew
 */
public class FileFlowTest {
    
    public FileFlowTest() {
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
     * Test of connect method, of class FileFlow.
     */
    //@Test
    public void xtestConnect() {
        System.out.println("connect");
        String pathIn = "/data/oddball/oddball.log";
        String pathOut = "/data/oddball/oddball.copy.log";
        String pathOther = "/data/oddball/oddball.copy.2.log";
        
        
        TextDelimited tdIn = new TextDelimited(new Fields("date", "time", "level", "duff1", "duff2", "name", "ruleSet", "case", "assessment", "duff3"), " ", "\"");
        TextDelimited tdOut = new TextDelimited(new Fields("date", "time", "ruleSet", "case", "assessment"), ",", "\"");
        TextDelimited tdOther = new TextDelimited(new Fields("ruleSet", "case", "assessment"), ",", "\"");
        
        FileFlow instance = new FileFlow(new FileTap(tdIn, pathIn), new FileTap(tdOut, pathOut), new FileTap(tdOther, pathOther));
        instance.connect();
    }
    
}
