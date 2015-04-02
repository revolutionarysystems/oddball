/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.cases.Case;

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

    public String derivedProperties(){
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
                        System.out.println(alreadyAdded.get(propname));
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
    
    @Override
    public String getEnrichedCase(String ruleSet, String caseStr) {
        String tags = getLabel();
        caseStr = caseStr.replace("\"", "\\\"");
        if (assessTime==0){
            assessTime=new Date().getTime();
        }
        String timeStr = Long.toString(assessTime);
        StringBuilder enrichedCase =  new StringBuilder("{");
        enrichedCase.append("\"timestamp\" : \"" + timeStr + "\", ");
        enrichedCase.append("\"ruleSet\" : \"" + ruleSet + "\", ");
        enrichedCase.append("\"case\" : " + caseStr + ", ");
        enrichedCase.append(tags.substring(1, tags.length() - 1)+ ", ");
        String propStr = derivedProperties();
        enrichedCase.append(propStr.substring(1, propStr.length() - 1));
        enrichedCase.append(" }");
        return enrichedCase.toString();
    }

    @Override
    public String getEnrichedCase(String ruleSet, Case aCase, boolean generateUid, String forcedUid) {
        String tags = getLabel();
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
        enrichedCase.append(tags.substring(1, tags.length() - 1)+ ", ");
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
        String propStr = derivedProperties();
        enrichedCase.append(propStr.substring(1, propStr.length() - 1));
        enrichedCase.append(" }");
        return enrichedCase.toString();
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
                tags.add(as.getLabelStr());
                evidence.add(as);
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
