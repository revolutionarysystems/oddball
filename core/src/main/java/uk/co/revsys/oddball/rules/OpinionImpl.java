/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.lang.Long;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

    public List<String> getTags() {
        return tags;
    }

    public String getLabel() {
        StringBuffer tagStr = new StringBuffer("{ \"tags\" : [");
        for (String tag : tags){
            tagStr.append("\""+tag+"\", ");
        }
        tagStr.delete(tagStr.length()-2, tagStr.length());
        tagStr.append(" ] }");
        return tagStr.toString();
    }

    public String getEnrichedCase(String ruleSet, String caseStr) {
        String tags = getLabel();
        caseStr = caseStr.replace("\"", "\\\"");
        if (assessTime==0){
            assessTime=new Date().getTime();
        }
        String timeStr = Long.toString(assessTime);
        String enrichedCase =  "{ \"timeStamp\" : \"" + timeStr + "\", " + "\"ruleSet\" : \"" + ruleSet + "\", " + "\"case\" : \"" + caseStr + "\", " + tags.substring(1, tags.length() - 1)+" }";
        return enrichedCase;
    }

    public String getEnrichedCase(String ruleSet, Case aCase) {
        String tags = getLabel();
        String caseStr = aCase.getJSONisedContent();
        if (assessTime==0){
            assessTime=new Date().getTime();
        }
        String timeStr = Long.toString(assessTime);
        String enrichedCase =  "{ \"timeStamp\" : \"" + timeStr + "\", " + "\"ruleSet\" : \"" + ruleSet + "\", " + "\"case\" : " + caseStr + ", " + tags.substring(1, tags.length() - 1)+" }";
        return enrichedCase;
    }


    
    public HashSet<Object> getEvidence() {
        return evidence;
    }
    
    @Override
    public void incorporate(Assessment as) {
        if (as.getLabelStr()!=null){
            tags.add(as.getLabelStr());
            evidence.add(as);
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
    
}
