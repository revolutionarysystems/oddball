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
public class StringCase implements Case{

    public StringCase(String content) {
        this.content = content;
    }
    
    private String content;

    public String getContent() {
        return content;
    }

    public Object getContentObject() {
        return content;
    }

    public String getJSONisedContent(){
        return "\""+content.replace("\"", "\\\"")+"\"";
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString(){
        return "Case-"+content;
    }
}
