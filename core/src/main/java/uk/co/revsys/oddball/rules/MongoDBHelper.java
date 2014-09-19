/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.rules;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.bson.LazyBSONObject;
import org.bson.types.ObjectId;
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

    public MongoDBHelper(String dbName, boolean inMemory, String host, int port) throws UnknownHostException {
        db = new MongoDBFactory().getDBInstance(dbName, inMemory, host, port);
        Jongo jongo = new Jongo(db);
        cases = jongo.getCollection("cases");
    }

    public String getName() {
        return db.getName();
    }

    public void dropCases(){
        cases.drop();
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

    public String checkAlreadyExists(String duplicateQuery){
        System.out.println(duplicateQuery);
        FindOne found = cases.findOne(duplicateQuery);
        if (found.as(Map.class)==null){
            return null;
        } else {
            return (String) found.as(Map.class).get("_id").toString();
        }
    }
    
    public void removeCase(String caseId) {
//        cases.remove("{ \"_id\": \"" + caseId + "\" }");
        cases.remove("{_id: #}", new ObjectId(caseId));

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

    public Collection<String> findCasesForOwner(String owner) throws DaoException {
        try {
            String query = "{}";
            if (!owner.equals(Oddball.ALL)) {
                Map queryMap = JSONUtil.json2map(query);
                queryMap.put("case." + OWNERPROPERTY, owner);
                query = JSONUtil.map2json(queryMap);
            }
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

    public Collection<String> findCasesForOwner(String owner, String queryString, Map<String, String> options) throws IOException, DaoException {
        if (queryString == null) {
            queryString="{ }";
        }
        BasicDBObject query = new BasicDBObject(JSONUtil.json2map(queryString));
        if (!owner.equals(Oddball.ALL)) {
            query.append("case." + OWNERPROPERTY, owner);
        }
        if (options.get("recent") != null) {
            addRecentQuery(query, options.get("recent"));
        }
        if (options.get("since") != null) {
            addSinceQuery(query, options.get("since"));
        }
        if (options.get("series") != null) {
            addSeriesQuery(query, options.get("series"));
        }
        if (options.get("agent") != null) {
            addAgentQuery(query, options.get("agent"));
        }
        List<DBObject> foundCases = cases.getDBCollection().find(query).toArray();
        ArrayList<String> caseList = new ArrayList<String>();
        for (DBObject foundCase : foundCases) {
            Map result = JSONUtil.json2map(foundCase.toString());
            result.put("_id", foundCase.get("_id").toString());
            String json = JSONUtil.map2json(result);
            if (json.contains(":  }")) {
                LOGGER.debug("Reading bad json object");
                LOGGER.debug(json);
                json = json.replace(":  }", ": {}");
            }
            caseList.add(json);
        }
        return caseList;
    }

    public Collection<String> findCaseById(String owner, String id) throws DaoException {
//        try {
            Find found = cases.find("{_id: #}", new ObjectId(id));
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
//        } catch (IOException ex) {
//            throw new DaoException("Unable to find cases for owner " + owner, ex);
//        }
    }

    public Collection<String> deleteCaseById(String owner, String id) throws DaoException {
        cases.remove("{_id: #}", new ObjectId(id));
        String response = "case removed";
        ArrayList<String> caseList = new ArrayList<String>();
        caseList.add(response);
        return caseList;
    }

   public String findLatestCaseForOwner(String owner, String queryString, String recent, String since) throws DaoException {
        try {
            Collection<String> result = findLatestQuery(owner, queryString, "timestamp", recent, since);
            return result.iterator().next();
        } catch (NoSuchElementException ex) {
            return "{ }";
        } catch (IOException ex) {
            throw new DaoException("Unable to find cases for owner " + owner, ex);
        }
   }

    private void addRecentQuery(BasicDBObject query, String recent){
            int minutes = Integer.parseInt(recent);
            long millis = minutes * 60 * 1000;
            long now = Calendar.getInstance().getTimeInMillis();
            long cutoff = now - millis;
            BasicDBObject subQuery = new BasicDBObject("$gt", Long.toString(cutoff));
//            BasicDBObject subQuery = new BasicDBObject("$gt", cutoff);
            query.append("timestamp", subQuery);
    }
   
    private void addSinceQuery(BasicDBObject query, String since){
            long cutoff = 0;
            try {
                cutoff = Long.parseLong(since);
            } catch (java.lang.NumberFormatException e) {}
            BasicDBObject subQuery = new BasicDBObject("$gt", Long.toString(cutoff));
//            BasicDBObject subQuery = new BasicDBObject("$gt", cutoff);
            query.append("timestamp", subQuery);
    }
   
    private void addSeriesQuery(BasicDBObject query, String series){
            query.append("case.series", series);
    }
   
    private void addAgentQuery(BasicDBObject query, String agent){
            query.append("case.agent", agent);
    }
   
    public Collection<String> findDistinctQuery(String owner, String queryString, String field, String recent, String since) throws DaoException, IOException {
        BasicDBObject query = new BasicDBObject(JSONUtil.json2map(queryString));
        if (!owner.equals(Oddball.ALL)) {
            //query = new BasicDBObject("case." + OWNERPROPERTY, owner);
            query.append("case." + OWNERPROPERTY, owner);
        }
        if (recent != null) {
            addRecentQuery(query, recent);
        }
        if (since != null) {
            addSinceQuery(query, since);
        }
        List foundCases = cases.getDBCollection().distinct(field, query);
        ArrayList<String> caseList = new ArrayList<String>();
        for (Object foundCase : foundCases) {
            caseList.add("\"" + foundCase.toString() + "\"");
        }
        return caseList;
    }

    public Collection<String> findLatestQuery(String owner, String queryString, String field, String recent, String since) throws DaoException, IOException {
        BasicDBObject query = new BasicDBObject(JSONUtil.json2map(queryString));
        BasicDBObject sort = new BasicDBObject(field, -1);
        if (!owner.equals(Oddball.ALL)) {
            //query = new BasicDBObject("case." + OWNERPROPERTY, owner);
            query.append("case." + OWNERPROPERTY, owner);
        }
        if (recent != null) {
            addRecentQuery(query, recent);
        }
        if (since != null) {
            addSinceQuery(query, since);
        }
        List<DBObject> foundCases = cases.getDBCollection().find(query).sort(sort).limit(1).toArray();
        ArrayList<String> caseList = new ArrayList<String>();
        for (DBObject foundCase : foundCases) {
            Map result = JSONUtil.json2map(foundCase.toString());
            result.put("_id", foundCase.get("_id").toString());
            String json = JSONUtil.map2json(result);
            if (json.contains(":  }")) {
                LOGGER.debug("Reading bad json object");
                LOGGER.debug(json);
                json = json.replace(":  }", ": {}");
            }
            caseList.add(json);
        }
        
        
        return caseList;
    }

    public static String OWNERPROPERTY = "accountId";

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
