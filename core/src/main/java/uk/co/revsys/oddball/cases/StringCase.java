/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cases;

import java.io.IOException;
import java.util.Map;
import uk.co.revsys.oddball.util.JSONUtil;

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
        return content;
    }

    @Override
    public void ensureOwner(String owner){
        // no way of consistently modifying a string case
    }
    
    @Override
    public String getOwner(){
        try{
            Map<String, Object> mapContent = JSONUtil.json2map(content);
            if (mapContent.containsKey("owner")&& mapContent.get("owner")!=null){
                return mapContent.get("owner").toString();
            } else {
                if (mapContent.containsKey("accountId")&& mapContent.get("accountId")!=null){
                    return mapContent.get("accountId").toString();
                } else {
                    return null;
                }
            }
        }
        catch (IOException e){
            return null;
        }
    }

}
