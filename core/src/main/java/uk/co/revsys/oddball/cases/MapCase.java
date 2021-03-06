/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cases;

import com.fasterxml.jackson.core.JsonParseException;
import java.io.IOException;
import java.util.Map;
import uk.co.revsys.oddball.util.JSONUtil;

/**
 *
 * @author Andrew
 */
public class MapCase implements Case{

    public MapCase(String content) throws InvalidCaseException{
        setContent(content);
    }
    
    private String content;
    private Map<String, Object> mapContent;

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Object getContentObject(){
        return mapContent;
    }
    
    
    @Override
    public String getJSONisedContent(){
        return content;
    }
    
    @Override
    public void setContent(String content)  throws InvalidCaseException{
        this.content = content;
        try {
            mapContent = JSONUtil.json2map(content);
        } catch (JsonParseException e) {
            throw new InvalidCaseException("Invalid Map Case - empty content - "+content, e);
        } catch (IOException ex) {
            try {
                mapContent = JSONUtil.json2map(content.replace("\\", "\\\\"));
            } catch (IOException ex2) {
                throw new InvalidCaseException("Invalid Map Case -"+content, ex);
            }
            
        }
    }
    
    @Override
    public String toString(){
        return content;
    }

    @Override
    public void ensureOwner(String owner){
        if (owner!=null){
            mapContent.put("owner", owner);
            content = JSONUtil.map2json(mapContent);
        }
    }

    @Override
    public String getOwner(){
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
    
    
}
