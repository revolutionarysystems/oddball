/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew
 */
public class OddUtil {

    public long parseTimePeriod(String period, String defaultUnit) throws InvalidTimePeriodException{
        period=period.trim();
        long millis = 0;
        if (period.matches("\\d*")){
            period=period+defaultUnit;
        }
        if (period.matches("\\d*[~smhDMWY]")){
            long periodNum = Long.parseLong(period.substring(0, period.length()-1));
            String periodUnit = period.substring(period.length()-1);
            if (periodUnit.equals("s")){
                millis = periodNum * 1000;
            } else {
            if (periodUnit.equals("~")){
                millis = periodNum;
                } else {
                    if (periodUnit.equals("m")){
                        millis = periodNum * 60 * 1000;
                    } else {
                        if (periodUnit.equals("h")){
                            millis = periodNum * 60 * 60 * 1000;
                        } else {
                            if (periodUnit.equals("D")){
                                millis = periodNum * 24 * 60 * 60 * 1000;
                            } else {
                                if (periodUnit.equals("W")){
                                    millis = periodNum * 7 * 24 * 60 * 60 * 1000;
                                } else {
                                    if (periodUnit.equals("M")){
                                        millis = periodNum * 30 * 24 * 60 * 60 * 1000;
                                    } else {
                                        if (periodUnit.equals("Y")){
                                            millis = periodNum * 365 * 24 * 60 * 60 * 1000;
                                        }
                                    }
                                }
                            }
                        } 
                    }
                }
            } 
        } else {
            throw new InvalidTimePeriodException(period);
        }
        return millis;
    }
    
    private String replacePlaceholder(String templateString, Map<String, Object> aCase){
        int openBrace = 1+templateString.substring(1).indexOf("<");
        int closeBrace = 1+templateString.substring(1).indexOf(">");
        String targetName = templateString.substring(openBrace+1, closeBrace);
        String replacement = "null";
        Object replacementObj= new OddUtil().getDeepProperty(aCase, targetName);
        if (replacementObj!=null){
            replacement = replacementObj.toString();
        } 
        return templateString.substring(0, openBrace)+replacement+templateString.substring(closeBrace+1);
    }
    
    public String replacePlaceholders(String templateString, Map<String, Object> aCase){
        while (templateString.substring(1).indexOf("<")>=0){
            templateString=replacePlaceholder(templateString, aCase);
        }
        return templateString;
    }

    public Object getDeepProperty (Map aMap, String propertyPath){
        Map subMap = aMap;
        Object propertyValue=null;
        while (propertyPath.contains(".")&& subMap!=null){
            String key = propertyPath.substring(0, propertyPath.indexOf("."));
            subMap = (Map) subMap.get(key);
            propertyPath = propertyPath.substring(propertyPath.indexOf(".")+1);
//            if (subMap!=null){
//                LOGGER.debug(subMap.toString());
//                LOGGER.debug(propertyPath);
//            }
        }
//        if (subMap!=null){
//            LOGGER.debug(subMap.toString());
//            LOGGER.debug(propertyPath);
//        }
    if (subMap!=null){
            propertyValue = subMap.get(propertyPath);
        }
        return propertyValue;
    }
    
    
    public Map<String, Object> list2map(List<String> aList){
        Map<String, Object> aMap = new HashMap<String, Object>();
        for (int i=0; i<aList.size(); i++){
            aMap.put(Integer.toString(i), aList.get(i));
        }
        return aMap;
    }
    
    public boolean isDeep(List l){
        for (Object item:l){
            if (item instanceof Map){
                return true;
            }
            if (item instanceof Collection){
                return true;
            }
        }
        return false;
    }

    
    public Map<String, String> flatten (Map<String, Object> aMap){
        Map<String, String> flatMap = new HashMap<String, String>();
        for(String key : aMap.keySet()){
            if (aMap.get(key) instanceof Map){
                Map<String, String> flatSubMap = flatten((Map)aMap.get(key));
                for(String innerKey : flatSubMap.keySet()){
                    flatMap.put(key+"."+innerKey.replace(",","-"), flatSubMap.get(innerKey));
                }
            } else {
                if (aMap.get(key) instanceof List){
                    if (isDeep((List)aMap.get(key))){
                        Map<String, String> flatSubMap = flatten(list2map((List)aMap.get(key)));
                        for(String innerKey : flatSubMap.keySet()){
                            flatMap.put(key+"."+innerKey.replace(",","-"), flatSubMap.get(innerKey));
                        }
                    } else {
                        try {
                            flatMap.put(key, aMap.get(key).toString());
                        }
                        catch (NullPointerException ex){
                            flatMap.put(key, "null");
                        }
                    }
                } else {
                    try {
                        flatMap.put(key, aMap.get(key).toString());
                    }
                    catch (NullPointerException ex){
                        flatMap.put(key, "null");
                    }
                }
            }
        }
        return flatMap;
    }
    
    public String removeChar(String value, int ch) {
        value = value.replaceAll(String.valueOf(java.lang.Character.toChars(ch)), "");
        return value;
    }

    public String removeStr(String value, String str) {
        value = value.replaceAll(String.valueOf(str), "");
        return value;
    }

    public String ipRange(String ip, int length) {
        int version = 4;
        if (ip.contains(":")){
            version = 6;
        }
        String separatorRE = "\\.";
        String separator = ".";
        int ipLength = 4;
        if (version==6){
            separatorRE = ":";
            separator = ":";
            ipLength = 8;
        }
        String[] ipParts= ip.split(separatorRE);
        length = Math.min(length, ipLength);
        StringBuilder s = new StringBuilder("");
        s.append(ipParts[0]);
        for (int i=1; i<length; i++){
            s.append(separator);
            s.append(ipParts[i]);
        }
        return s.toString();
    }

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

    
}
