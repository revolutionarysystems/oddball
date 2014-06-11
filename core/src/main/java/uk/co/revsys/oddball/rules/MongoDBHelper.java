/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

 
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.WriteResult;
import de.undercouch.bson4jackson.types.ObjectId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jongo.Distinct;
import org.jongo.Find;
import org.jongo.FindOne;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.util.JSONUtil;
 
/**
 *
 * @author Andrew
 */
public class MongoDBHelper {

    private MongoCollection cases;
    private DB db;
    
    

    public MongoDBHelper(String dbName) {
        db = new Fongo("Test").getDB(dbName);
        Jongo jongo = new Jongo(db);
        cases = jongo.getCollection("cases");
    }
    
    public String getName(){
        return db.getName();
    }
    
    public String insertCase(String content){
        WriteResult wr = cases.insert(content);
        // When supported by fongo
        //System.out.println(wr.getUpsertedId());
        //return wr.getUpsertedId().toString();
        // for now
        FindOne found = cases.findOne(content);
        return (String) found.as(Map.class).get("_id").toString();
    }
    
    public void removeCase(String caseId){
        cases.remove("{ \"_id\": \""+caseId+"\" }");
    }
    
    public boolean testCase(String query, String caseId){
        String queryMod = "{ \"_id\" : \""+caseId+"\", "+query.substring(1);
        FindOne found = cases.findOne(queryMod);
        return found.as(Map.class)!=null;
    }
    
    public boolean testCase(String query){
        FindOne found = cases.findOne(query);
        return found.as(Map.class)!=null;
    }
    
    public Map findOne(String query){
        FindOne found = cases.findOne(query);
        return found.as(Map.class);
    }

    public Iterable<String> findCases() throws IOException{
        Find found = cases.find("{}");
        Iterable<Map> foundCases = found.as(Map.class);
        ArrayList<String> caseList = new ArrayList<String>();
        for (Map foundCase : foundCases){
            caseList.add(JSONUtil.map2json(foundCase));
        }
        return caseList;
    }

    public Iterable<String> findCases(String query) throws IOException{
        Find found = cases.find(query);
        Iterable<Map> foundCases = found.as(Map.class);

        ArrayList<String> caseList = new ArrayList<String>();
        for (Map foundCase : foundCases){
            caseList.add(JSONUtil.map2json(foundCase));
        }
        return caseList;
    }

    public Iterable<String> findDistinct(String field) throws IOException{
        Distinct found = cases.distinct(field);
        Iterable<String> foundCases = found.as(String.class);

        ArrayList<String> caseList = new ArrayList<String>();
        for (String foundCase : foundCases){
            caseList.add("\""+foundCase+"\"");
        }
        return caseList;
    }


}