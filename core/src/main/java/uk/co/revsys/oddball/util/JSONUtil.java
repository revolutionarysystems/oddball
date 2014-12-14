/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
                            out.append("\"");
                            out.append(item.toString());
                            out.append("\"");
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
    
    
}
