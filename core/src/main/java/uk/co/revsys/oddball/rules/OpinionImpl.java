/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.HashSet;

/**
 *
 * @author Andrew
 */
public class OpinionImpl implements Opinion{

    public void setLabel(String label) {
        this.label = label;
    }

    String label="";
    
    HashSet<Object> evidence = new HashSet();

    public String getLabel() {
        return label;
    }

    public HashSet<Object> getEvidence() {
        return evidence;
    }
    
    @Override
    public void incorporate(Assessment as) {
        if (as.getLabelStr()!=null){
            if (!label.isEmpty()){
                label+=";";
            }
            label += as.getLabelStr();
            evidence.add(as);
        }
    }
    
}
