/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cases;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.revsys.oddball.util.JSONUtil;

/**
 *
 * @author Andrew
 */
public class MapCase implements Case{

    public MapCase(String content) throws InvalidCaseException{
        this.content = content;
        try {
            mapContent = JSONUtil.json2map(content);
        } catch (IOException ex) {
            throw new InvalidCaseException("Invalid Map Case", ex);
        }
    }
    
    private String content;
    private Map<String, Object> mapContent;

    public String getContent() {
        return content;
    }

    public String getJSONisedContent(){
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString(){
        return "Case-"+content;
    }
}
