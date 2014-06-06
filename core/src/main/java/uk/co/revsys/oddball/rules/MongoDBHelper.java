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
import java.util.Map;
import org.jongo.Find;
import org.jongo.FindOne;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import uk.co.revsys.oddball.cases.StringCase;
 
/**
 *
 * @author Andrew
 */
public class MongoDBHelper {

    MongoCollection cases;
    DB db;

    public MongoDBHelper() {
        db = new Fongo("Test").getDB("Database");
        Jongo jongo = new Jongo(db);
        cases = jongo.getCollection("cases");
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
    
    
}
