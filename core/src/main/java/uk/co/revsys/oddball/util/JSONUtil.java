/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.CDL;
import org.json.JSONArray;

/**
 *
 * @author Andrew
 */
public class JSONUtil {


    public static Map<String, Object> json2map(String json) throws IOException{
//	Map<String,Object> map = new HashMap<String,Object>();
	ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(json, new TypeReference<HashMap<String,Object>>(){});
        return map;
    }
    
    public static String map2json(Map<String, Object> map){
        if (map==null){
            return "{}";
        } else {
            StringBuilder out =new StringBuilder("{ ");
            for (String key : map.keySet()){
                out.append("\""+key+"\" : ");
                if (map.get(key) instanceof Map){
    //                out.append("\""+map2json((Map<String, Object>) map.get(key))+"\" ");
                    out.append(map2json((Map<String, Object>) map.get(key)));
                } else {
                    if (map.get(key) instanceof List){
                        out.append ("[");
                        for (Object item : (List) map.get(key)){
                            if (item instanceof String){
                                out.append("\""+((String)item).replace("\"", "\\\"")+"\" ");
                            } else {
                                if (item instanceof Map){
                                    out.append(map2json((Map)item));
                                } else {
//                            out.append("\"");
                                out.append(item.toString());
//                            out.append("\"");
                                }
                            }
                            out.append(", ");
                        }
                        if (!((List) map.get(key)).isEmpty()){
                            out.delete(out.length()-2, out.length());
                        }
                        out.append ("]");
                    } else {
                        if (map.get(key) instanceof String){
                            out.append("\""+map.get(key).toString().replace("\"", "\\\"")+"\" ");
                        } else {
                            try{
                                out.append(map.get(key).toString());
                            }
                            catch (NullPointerException npe){
                                out.append("null");
                            }
                        }
                    }
                }
                out.append(", ");
            }
            if (!(map.keySet().isEmpty())){
                out.delete(out.length()-2, out.length());
            }
            out.append(" }");
            return out.toString();
        }
    }

    
    public String jsonWrap(Collection<String> cases){
        StringBuilder out = new StringBuilder("[ ");
        for (String aCase : cases) {
            out.append(aCase);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return out.toString();
    }

    public String json2csv(List<String> cases) throws IOException{
        JSONArray array = new JSONArray();
        OddUtil ou = new OddUtil();
        Set<String> uniqueNames = new HashSet<String>();
        List<String> sortedNames = new ArrayList<String>();
        for (String aCase : cases){
            array = new JSONArray();
            array.put(ou.flatten(json2map(aCase)));
            String headerPlus1 =CDL.toString(array);
            String[] lines = headerPlus1.split("\n");
            String[]names=lines[0].split(",");
            for(String name: names){
                uniqueNames.add(name);
            }
        }
//        array.put(ou.flatten(json2map(cases.get(0))));
        for (String uniqueName:uniqueNames){
            sortedNames.add(uniqueName);
        }
        Collections.sort(sortedNames);
        
//        JSONArray names = new JSONArray("["+lines[0]+"]");
        StringBuilder s = new StringBuilder();
        for (String name: sortedNames){
            s.append(name+",");
        }
        array = new JSONArray();
        for (String aCase : cases){
            array.put(ou.flatten(json2map(aCase)));
        }
        JSONArray names2 = new JSONArray(sortedNames);
        return s.append("\n").append(CDL.toString(names2, array)).toString();
    }
    
    
    
    
}
