/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.rules;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.WriteResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.jongo.Find;
import org.jongo.FindOne;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
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

    public String getName() {
        return db.getName();
    }

    public String insertCase(String content) {
        // jongo interprets # as parameter.
        content = content.replace("#", "(hash)");
        WriteResult wr = cases.insert(content);
        if (content.contains("{}") || content.contains(":  }")) {
            LOGGER.debug("Writing empty or bad json object");
            LOGGER.debug(content);
        }
        //cases.getDBCollection().insert(new BasicDBObject(JSONUtil.json2map(content)));
        // When supported by fongo
        //System.out.println(wr.getUpsertedId());
        //return wr.getUpsertedId().toString();
        // for now
        FindOne found = cases.findOne(content);
        return (String) found.as(Map.class).get("_id").toString();
    }

    public void removeCase(String caseId) {
        cases.remove("{ \"_id\": \"" + caseId + "\" }");
    }

    public boolean testCase(String query, String caseId) {
        String queryMod = "{ \"_id\" : \"" + caseId + "\", " + query.substring(1);
        FindOne found = cases.findOne(queryMod);
        return found.as(Map.class) != null;
    }

    public boolean testCase(String query) {
        FindOne found = cases.findOne(query);
        return found.as(Map.class) != null;
    }

    public Map findOne(String query) {
        FindOne found = cases.findOne(query);
        return found.as(Map.class);
    }

    public Iterable<String> findCasesForOwner(String owner) throws DaoException {
        try {
            String query = "{}";
            LOGGER.debug("query =" + query);
            if (!owner.equals(Oddball.ALL)) {
                Map queryMap = JSONUtil.json2map(query);
                queryMap.put("case." + OWNERPROPERTY, owner);
                LOGGER.debug("queryMap =" + queryMap.toString());
                query = JSONUtil.map2json(queryMap);
            }
            LOGGER.debug("query =" + query);
            Find found = cases.find(query);
            Iterable<Map> foundCases = found.as(Map.class);
            ArrayList<String> caseList = new ArrayList<String>();
            for (Map foundCase : foundCases) {
                String json = JSONUtil.map2json(foundCase);
                if (json.contains(":  }")) {
                    LOGGER.debug("Reading bad json object");
                    LOGGER.debug(json);
                    json = json.replace(":  }", ": {}");
                }
                caseList.add(json);
            }
            return caseList;
        } catch (IOException ex) {
            throw new DaoException("Unable to find cases for owner " + owner, ex);
        }
    }

    public Iterable<String> findCasesForOwner(String owner, String query) throws DaoException {
        LOGGER.debug("query =" + query);
        try {
            if (!owner.equals(Oddball.ALL)) {
//            //Map queryMap = JSONUtil.json2map(query);
//            queryObj.put("case."+OWNERPROPERTY, owner);
//            //queryMap.put("case."+OWNERPROPERTY, owner);
//            LOGGER.debug("queryObj ="+queryObj.toString());
//            //query = JSONUtil.map2json(queryObj);
//            query = queryObj.toString();
                StringBuilder modQuery = new StringBuilder("{\"case." + OWNERPROPERTY + "\":\"" + owner + "\", ");
                modQuery.append(query.substring(1));
                query = modQuery.toString();
            }
            LOGGER.debug("modQuery =" + query);
            Find found = cases.find(query);
            Iterable<Map> foundCases = found.as(Map.class);
            ArrayList<String> caseList = new ArrayList<String>();
            for (Map foundCase : foundCases) {
                String json = JSONUtil.map2json(foundCase);
                if (json.contains(":  }")) {
                    LOGGER.warn("Reading bad json object");
                    LOGGER.warn(json);
                    json = json.replace(":  }", ": {}");
                }
                caseList.add(json);
            }
            return caseList;
        } catch (IOException ex) {
            throw new DaoException("Unable to find cases for owner " + owner, ex);
        }
    }

    public Iterable<String> findDistinct(String owner, String field, String recent) throws DaoException {
        BasicDBObject query = new BasicDBObject();
        if (!owner.equals(Oddball.ALL)) {
            query = new BasicDBObject("case." + OWNERPROPERTY, owner);
        }
        if (recent != null) {
            int minutes = Integer.parseInt(recent);
            long millis = minutes * 60 * 1000;
            long now = Calendar.getInstance().getTimeInMillis();
            long cutoff = now - millis;
            BasicDBObject subQuery = new BasicDBObject("$gt", Long.toString(cutoff));
            query.append("timeStamp", subQuery);
        }
        System.out.println(">>> query");
        System.out.println(query);
        List foundCases = cases.getDBCollection().distinct(field, query);
        System.out.println(foundCases);
        //Iterable<String> foundCases = found.as(String.class);

        ArrayList<String> caseList = new ArrayList<String>();
        for (Object foundCase : foundCases) {
            caseList.add("\"" + foundCase.toString() + "\"");
        }
        return caseList;
    }

    public static String OWNERPROPERTY = "accountId";

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
