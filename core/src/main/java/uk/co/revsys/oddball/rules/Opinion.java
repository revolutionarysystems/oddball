/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.List;
import uk.co.revsys.oddball.cases.Case;

/**
 *
 * @author Andrew
 */
public interface Opinion {
    
    public void incorporate(Assessment as);
    public List<String> getTags();
    public void setTags(List<String> tags); 
    public String getLabel();
    public void setId(String id);
//    public String getEnrichedCase(String ruleSet, String caseStr);
    public String getEnrichedCase(String ruleSet, Case aCase, boolean generateUid, String forcedUid);
}
