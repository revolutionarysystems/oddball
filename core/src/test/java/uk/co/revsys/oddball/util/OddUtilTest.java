/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Andrew
 */
public class OddUtilTest {
    
    public OddUtilTest() {
    }

    @Test
    public void testFlattenMap() throws IOException {
        Map aMap = JSONUtil.json2map("{\"test\":{\"object\":{\"value\":1,\"type\":\"A\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}}}");
        Map aFlatMap = new OddUtil().flatten(aMap);
        System.out.println(aFlatMap);
        assertTrue(aFlatMap.get("test.object.type").equals("A"));
    }
    
    @Test
    public void testFlattenMap2() throws IOException {
        Map aMap = JSONUtil.json2map("{\"test\":[{\"subject\":{\"value\":1,\"type\":\"A\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}");
        Map aFlatMap = new OddUtil().flatten(aMap);
        System.out.println(aFlatMap);
        assertTrue(aFlatMap.get("test.1.object.valid").equals("true"));
    }

    @Test
    public void testFlattenMap3() throws IOException {
        Map aMap = JSONUtil.json2map("{\"test\":[{\"subject\":{\"value\":null,\"type\":\"A\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}");
        Map aFlatMap = new OddUtil().flatten(aMap);
        System.out.println(aFlatMap);
        assertTrue(aFlatMap.get("test.1.object.valid").equals("true"));
    }

    @Test
    public void testJson2csv() throws IOException {
        String case1 = "{\"test\":[{\"subject\":{\"value\":1,\"type\":\"A\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        String case2 = "{\"test\":[{\"subject\":{\"value\":3,\"type\":\"C\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        ArrayList cases = new ArrayList();
        cases.add(case1);
        cases.add(case2);
        String csv = new JSONUtil().json2csv(cases);
        System.out.println(csv);
        assertEquals("test.0.subject.tags,test.0.subject.type,test.0.subject.valid,test.0.subject.value,test.1.object.tags,test.1.object.type,test.1.object.valid,test.1.object.value,\n" +
                "\"[1, 1.5, false, Geronimo]\",A,true,1,\"[1, 1.7, true, Hiawatha]\",B,true,2\n" +
                "\"[1, 1.5, false, Geronimo]\",C,true,3,\"[1, 1.7, true, Hiawatha]\",B,true,2\n", csv);
    }


    @Test
    public void testJson2csvWithComma() throws IOException {
        String case1 = "{\"test\":[{\"subject\":{\"value\":1,\"type\":\"A,B\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        String case2 = "{\"test\":[{\"subject\":{\"value\":3,\"type\":\"C\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        ArrayList cases = new ArrayList();
        cases.add(case1);
        cases.add(case2);
        String csv = new JSONUtil().json2csv(cases);
        System.out.println(csv);
        //assertEquals("test.0.subject.tags.0,test.0.subject.tags.1,test.0.subject.tags.2,test.0.subject.tags.3,test.0.subject.type,test.0.subject.valid,test.0.subject.value,test.1.object.tags.0,test.1.object.tags.1,test.1.object.tags.2,test.1.object.tags.3,test.1.object.type,test.1.object.valid,test.1.object.value,\n1,1.5,false,Geronimo,\"A,B\",true,1,1,1.7,true,Hiawatha,B,true,2\n1,1.5,false,Geronimo,C,true,3,1,1.7,true,Hiawatha,B,true,2\n", csv);
        assertEquals("test.0.subject.tags,test.0.subject.type,test.0.subject.valid,test.0.subject.value,test.1.object.tags,test.1.object.type,test.1.object.valid,test.1.object.value,\n" +
            "\"[1, 1.5, false, Geronimo]\",\"A,B\",true,1,\"[1, 1.7, true, Hiawatha]\",B,true,2\n" +
            "\"[1, 1.5, false, Geronimo]\",C,true,3,\"[1, 1.7, true, Hiawatha]\",B,true,2\n", csv);
    }
    
    @Test
    public void testJson2csvWithCommaPropertyName() throws IOException {
        String case1 = "{\"test\":[{\"sub,ject\":{\"value\":1,\"type\":\"A,B\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        String case2 = "{\"test\":[{\"sub,ject\":{\"value\":3,\"type\":\"C\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        ArrayList cases = new ArrayList();
        cases.add(case1);
        cases.add(case2);
        String csv = new JSONUtil().json2csv(cases);
        System.out.println(csv);
        assertEquals("test.0.sub-ject.tags,test.0.sub-ject.type,test.0.sub-ject.valid,test.0.sub-ject.value,test.1.object.tags,test.1.object.type,test.1.object.valid,test.1.object.value,\n" +
            "\"[1, 1.5, false, Geronimo]\",\"A,B\",true,1,\"[1, 1.7, true, Hiawatha]\",B,true,2\n" +
            "\"[1, 1.5, false, Geronimo]\",C,true,3,\"[1, 1.7, true, Hiawatha]\",B,true,2\n", csv);
    }
    
    @Test
    public void testJson2csvWithQuote() throws IOException {
        String case1 = "{\"test\":[{\"subject\":{\"value\":1,\"type\":\"\\\"A\\\"\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        String case2 = "{\"test\":[{\"subject\":{\"value\":3,\"type\":\"C\",\"valid\":true, \"tags\":[1, 1.5, false, \"Geronimo\"]}},{\"object\":{\"value\":2,\"type\":\"B\",\"valid\":true, \"tags\":[1, 1.7, true, \"Hiawatha\"]}}]}";
        ArrayList cases = new ArrayList();
        cases.add(case1);
        cases.add(case2);
        String csv = new JSONUtil().json2csv(cases);
        System.out.println(csv);
        assertEquals("test.0.subject.tags,test.0.subject.type,test.0.subject.valid,test.0.subject.value,test.1.object.tags,test.1.object.type,test.1.object.valid,test.1.object.value,\n\"[1, 1.5, false, Geronimo]\",\"A\",true,1,\"[1, 1.7, true, Hiawatha]\",B,true,2\n\"[1, 1.5, false, Geronimo]\",C,true,3,\"[1, 1.7, true, Hiawatha]\",B,true,2\n", csv);
    }
                     
    @Test
    public void testIpRange(){
        String ip = "3.34.123.121";
        String ipRange = new OddUtil().ipRange(ip,2);
        System.out.println(ipRange);
        assertEquals("3.34", ipRange);
    }
    @Test

    public void testIpv6Range(){
        String ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        String ipRange = new OddUtil().ipRange(ip, 4);
        System.out.println(ipRange);
        assertEquals("2001:0db8:85a3:0000", ipRange);
    }
                     


}
