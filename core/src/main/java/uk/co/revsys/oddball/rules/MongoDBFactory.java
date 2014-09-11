
package uk.co.revsys.oddball.rules;

import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;


/**
 *
 * @author Andrew
 */
public class MongoDBFactory {

    public DB getDBInstance(String dbName, boolean inMemory, String host, int port)throws UnknownHostException{
        DB db = null;
        dbName = dbName.replace(".", "-");
        if (inMemory){
            db = new Fongo("Fongo").getDB(dbName);
        } else {
            db = new MongoClient(host, port).getDB(dbName);
        }
        return db;
    }
    
}
