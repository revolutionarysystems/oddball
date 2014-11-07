
package uk.co.revsys.oddball.rules;

import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.MongoClient;


/**
 *
 * @author Andrew
 */
public class MongoDBFactory {

    private static MongoClient mongoClient;
    
    public static void setMongoClient(MongoClient mongoClient){
        MongoDBFactory.mongoClient = mongoClient;
    }
    
    public static DB getDBInstance(String dbName, boolean inMemory){
        dbName = dbName.replace(".", "-");
        if (inMemory){
            return new Fongo("Fongo").getDB(dbName);
        } else {
            if(mongoClient == null){
                throw new IllegalStateException("No instance of MongoClient found");
            }
            return mongoClient.getDB(dbName);
        }
    }
    
}
