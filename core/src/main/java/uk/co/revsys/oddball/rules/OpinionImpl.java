/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    
}
