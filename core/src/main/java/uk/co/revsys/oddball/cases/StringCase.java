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

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Object getContentObject() {
        return content;
    }

    @Override
    public String getJSONisedContent(){
        return "\""+content.replace("\"", "\\\"")+"\"";
    }
    
    @Override
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString(){
        return "Case-"+content;
    }
}
