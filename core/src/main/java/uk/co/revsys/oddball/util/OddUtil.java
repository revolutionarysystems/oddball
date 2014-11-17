/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.util;

import java.util.Map;

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
        int openBrace = 1+templateString.substring(1).indexOf("{");
        int closeBrace = 1+templateString.substring(1).indexOf("}");
        String targetName = templateString.substring(openBrace+1, closeBrace);
        String replacement = (String) aCase.get(targetName);
        if (replacement==null){
            replacement = "null";
        }
        return templateString.substring(0, openBrace)+replacement+templateString.substring(closeBrace+1);
    }
    
    public String replacePlaceholders(String templateString, Map<String, Object> aCase){
        while (templateString.substring(1).indexOf("{")>=0){
            templateString=replacePlaceholder(templateString, aCase);
        }
        return templateString;
    }
    
}
