/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import com.fasterxml.jackson.core.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.util.JSONUtil;

/**
 *
 * @author Andrew
 */
public class OpinionImpl implements Opinion{

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    List<String> tags= new ArrayList<String>();
    
    HashSet<Object> evidence = new HashSet();
    
    private long assessTime=0;
    String id = null;

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getLabel() {
        StringBuilder tagStr = new StringBuilder("{ \"tags\" : [");
        for (String tag : tags){
            tagStr.append("\""+tag+"\", ");
        }
        if (tagStr.length()>12){
            tagStr.delete(tagStr.length()-2, tagStr.length());
        }
        tagStr.append("] }");
        return tagStr.toString();
    }

    public String derivedProperties(List<String> tags){
        HashMap<String, String> alreadyAdded = new HashMap<String, String>();
        for (String tag: tags){
            if (tag.contains(".")){
                String[] parsed = tag.split("\\.",2);
                String propname = parsed[0];
                String propvalue = parsed[1];
                if (!alreadyAdded.containsKey(propname)){ // if first instance of this derived tag
                    if (!propvalue.contains(">")){ // tag refinement doesn't apply if parent tag not present
                        alreadyAdded.put(propname, propvalue);
                    }
                } else {
                    if (propvalue.contains(">")){ // tag refinement
                        String[] parts= propvalue.split(">");
                        propvalue = parts[1];
                        String broadervalue = parts[0];
                        String currentvalue = alreadyAdded.get(propname);
                        if (currentvalue.contains(broadervalue)){
                            alreadyAdded.put(propname, currentvalue.replace(broadervalue, propvalue));
                        }
                    } else {
                        if (!alreadyAdded.get(propname).contains(propvalue)){  // if we've not seen this value before
                            alreadyAdded.put(propname, alreadyAdded.get(propname)+","+propvalue);
                        }
                    }
                }
            } 
        }

        StringBuilder propStr = new StringBuilder("{ \"derived\" : {");
        if (alreadyAdded.size()>0){
            for (String key : alreadyAdded.keySet()){
                propStr.append("\""+key+"\"");
                propStr.append(" : ");
                propStr.append("\""+alreadyAdded.get(key)+"\"");
                propStr.append(", ");
            }
            propStr.delete(propStr.length()-2, propStr.length());
        }
        propStr.append(" } }");
        return propStr.toString();
    }
    
//    @Override
//    public String getEnrichedCase(String ruleSet, String caseStr) {
//        String tags = getLabel();
//        caseStr = caseStr.replace("\"", "\\\"");
//        if (assessTime==0){
//            assessTime=new Date().getTime();
//        }
//        String timeStr = Long.toString(assessTime);
//        StringBuilder enrichedCase =  new StringBuilder("{");
//        enrichedCase.append("\"timestamp\" : \"" + timeStr + "\", ");
//        enrichedCase.append("\"ruleSet\" : \"" + ruleSet + "\", ");
//        enrichedCase.append("\"case\" : " + caseStr + ", ");
//        enrichedCase.append(tags.substring(1, tags.length() - 1)+ ", ");
//        String propStr = derivedProperties();
//        enrichedCase.append(propStr.substring(1, propStr.length() - 1));
//        enrichedCase.append(" }");
//        return enrichedCase.toString();
//    }

    @Override
    public String getEnrichedCase(String ruleSet, Case aCase, boolean generateUid, String forcedUid, boolean reEnrich) {
        if (reEnrich){
            return  enrichTaggedCase(ruleSet, aCase, generateUid, forcedUid); 
            
        } else {
            return getEnrichedCase(ruleSet, aCase, generateUid, forcedUid);
        }
    }

    @Override
    public String getEnrichedCase(String ruleSet, Case aCase, boolean generateUid, String forcedUid) {
        String tagsString = getLabel();
        String caseStr = aCase.getJSONisedContent();
        if (assessTime==0){
            assessTime=new Date().getTime();
        }
        String timeStr = Long.toString(assessTime);
        String caseTimeStr = null;
        StringBuilder caseTime = new StringBuilder();
        try{
            HashMap caseMap = (HashMap)aCase.getContentObject();
            if (caseMap.containsKey("time")&& caseMap.get("time")!=null){
                caseTimeStr = caseMap.get("time").toString();
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTimeInMillis(Long.parseLong(caseTimeStr));
                caseTime.append("\"caseTime\" : { ");
                String displayTime = new Date(gc.getTimeInMillis()).toString();
                caseTime.append("\"display\" : \"" + displayTime + "\", ");
//                String hod = Integer.toString(gc.get(GregorianCalendar.HOUR_OF_DAY));
                String hod = String.format("%02d",gc.get(GregorianCalendar.HOUR_OF_DAY));
                caseTime.append("\"hod\" : \"" + hod + "\", ");
                String dow = Integer.toString(gc.get(GregorianCalendar.DAY_OF_WEEK));
                caseTime.append("\"dow\" : \"" + dow + "\"");
                caseTime.append(" }, ");
            }
        } catch (ClassCastException ex){
            // just don't include the time
        }
//        String enrichedCase =  "{ \"timestamp\" : \"" + timeStr + "\", " + "\"ruleSet\" : \"" + ruleSet + "\", " + "\"case\" : " + caseStr + ", " + tags.substring(1, tags.length() - 1)+" }";
//        return enrichedCase;
        StringBuilder enrichedCase =  new StringBuilder("{");
        enrichedCase.append("\"timestamp\" : \"" + timeStr + "\", ");
        if (caseTimeStr!=null){
            enrichedCase.append(caseTime);
        }
        enrichedCase.append("\"ruleSet\" : \"" + ruleSet + "\", ");
        enrichedCase.append("\"case\" : " + caseStr + ", ");
        enrichedCase.append("\"owner\" : \"" + aCase.getOwner() + "\", ");
        enrichedCase.append(tagsString.substring(1, tagsString.length() - 1)+ ", ");
        if (id!=null){
            enrichedCase.append("\"_id\" : \"" + id + "\", ");
        } else {
            if (generateUid){
                id = UUID.randomUUID().toString();
                enrichedCase.append("\"_id\" : \"" + id + "\", ");
            } else {
                if (forcedUid!=null){
                    id = forcedUid;
                    enrichedCase.append("\"_id\" : \"" + forcedUid + "\", ");
                }
            }
        }
        String propStr = derivedProperties(getTags());
        enrichedCase.append(propStr.substring(1, propStr.length() - 1));
        enrichedCase.append(" }");
        return enrichedCase.toString();
    }


    @Override
    public String enrichTaggedCase(String ruleSet, Case aCase, boolean generateUid, String forcedUid){
        String caseStr = aCase.getJSONisedContent();
        Map caseMap = new HashMap();
        caseMap.putAll((HashMap)aCase.getContentObject());
        if (assessTime==0){
            assessTime=new Date().getTime();
        }
        String timeStr = Long.toString(assessTime);
        String caseTimeStr = null;
        Map<String, String>  caseTimeMap = new HashMap<String, String>();
        Map<String, Object> subCaseMap = null;
        try {
            subCaseMap = (Map<String, Object>)caseMap.get("case");
        } 
        catch (ClassCastException e){
            try {
                subCaseMap = JSONUtil.json2map((String)caseMap.get("case"));
            }
            catch (JsonParseException ex){
                LOGGER.warn("Could not parse case string");
                subCaseMap = new HashMap<String, Object>();
            }
        }
            
        try{
            if (subCaseMap.containsKey("time")&& subCaseMap.get("time")!=null){
                caseTimeStr = subCaseMap.get("time").toString();
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTimeInMillis(Long.parseLong(caseTimeStr));
                String displayTime = new Date(gc.getTimeInMillis()).toString();
                caseTimeMap.put("display", displayTime);
                String hod = String.format("%02d",gc.get(GregorianCalendar.HOUR_OF_DAY));
                caseTimeMap.put("hod",hod);
                String dow = Integer.toString(gc.get(GregorianCalendar.DAY_OF_WEEK));
                caseTimeMap.put("dow",dow);
            }
        } catch (ClassCastException ex){
            // just don't include the time
        }
        caseMap.put("timestamp",  timeStr);
        caseMap.put("caseTime", caseTimeMap);
        caseMap.put("ruleSet", ((String) caseMap.get("ruleSet"))+","+ruleSet);
        caseMap.put("owner", aCase.getOwner());
        List<String> prevTags = (List<String>)caseMap.get("tags");
        List<String> tagsToRemove = new ArrayList<String>();
//        this.tags.addAll(prevTags);
        for (String tag : this.tags){
            if (tag.contains(".")){
                String prefix = tag.substring(0,tag.indexOf("."));
                for (String prevTag : prevTags){
                    if (prevTag.contains(prefix)){
                        tagsToRemove.add(prevTag);
                    }
                }
            }
        }
        for (String tag : tagsToRemove){
            prevTags.remove(tag);
        }
        
        prevTags.addAll(this.tags);
        caseMap.put("tags", prevTags);
        if (id!=null){
            caseMap.put("_id", id);
        } else {
            if (generateUid){
                id = UUID.randomUUID().toString();
                caseMap.put("_id", id);
            } else {
                if (forcedUid!=null){
                    id = forcedUid;
                    caseMap.put("_id", id);
                }
            }
        }
        String propStr = derivedProperties(prevTags);
        try {
            Map<String, Object> props = JSONUtil.json2map(propStr);
            caseMap.put("derived", props.get("derived"));
        } catch (JsonParseException ex) {
            LOGGER.warn("JSON parse failed for properties:"+propStr, ex);
        }
        return JSONUtil.map2json(caseMap);
    }


    
    public HashSet<Object> getEvidence() {
        return evidence;
    }
    
    @Override
    public void incorporate(Assessment as) {
        if (as.getLabelStr()!=null){
            String tag = as.getLabelStr();
            if (tag.contains(">")){ // tag refinement
                String[] parts= tag.split("[.>]");
                String refinedvalue = parts[2];
                String broadervalue = parts[1];
                String prefix = parts[0];
                if (tags.contains(prefix+"."+broadervalue)){
                    tags.remove(prefix+"."+broadervalue);
                    as.setLabelStr(prefix+"."+refinedvalue);
                    tags.add(as.getLabelStr());
                    evidence.add(as);
                }
            } else { 
                if (tag.indexOf("<")>=0){ // tag replacement
                    String[] parts= tag.split("<");
                    String prefixPlus = parts[0];
                    String value = parts[1];
                    HashSet <String> toRemove = new HashSet<String>();
                    for (String t : tags){
                        if (t.indexOf(prefixPlus)==0){
                            toRemove.add(t);
                        }
                    }
                    for (String t: toRemove){
                        tags.remove(t);
                    }
                    as.setLabelStr(prefixPlus+value);
                    tags.add(as.getLabelStr());
                    evidence.add(as);
                }
                else {
                   tags.add(as.getLabelStr());
                   evidence.add(as);
                }
            }
            
        }
    }

    /**
     * @return the assessTime
     */
    public long getAssessTime() {
        return assessTime;
    }

    /**
     * @param assessTime the assessTime to set
     */
    public void setAssessTime(long assessTime) {
        this.assessTime = assessTime;
    }
    
    public void setId(String id){
        this.id = id; 
    }

    
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");
}
