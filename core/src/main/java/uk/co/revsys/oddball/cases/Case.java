/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cases;

/**
 *
 * @author Andrew
 */
public interface Case {

    public String getContent();
    public Object getContentObject();
    public void setContent(String content) throws InvalidCaseException;
    public String getJSONisedContent();
    
}
