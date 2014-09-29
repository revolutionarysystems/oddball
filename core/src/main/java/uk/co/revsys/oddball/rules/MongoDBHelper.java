/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.rules;

<<<<<<< HEAD
=======
import com.github.fakemongo.Fongo;
>>>>>>> a2120c8d56f12e11fea2ca75f358e9b7f969cf47
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
<<<<<<< HEAD
=======
import java.util.NoSuchElementException;
import org.bson.LazyBSONObject;
>>>>>>> a2120c8d56f12e11fea2ca75f358e9b7f969cf47
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

<<<<<<< HEAD
    private final MongoCollection cases;
    private final DB db;
=======
    private MongoCollection cases;
    private DB db;
>>>>>>> a2120c8d56f12e11fea2ca75f358e9b7f969cf47

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
        if (options.get("binQuery") != null) {
            queryString = addBinQuery(queryString, options.get("binQuery"));
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
        if (options.get("sessionId") != null) {
            addSessionIdQuery(query, options.get("sessionId"));
        }
        if (options.get("userId") != null) {
            addUserIdQuery(query, options.get("userId"));
        }

        ArrayList<String> caseList = new ArrayList<String>();
        if (options.get("distinct") !=null){
            List foundCases = cases.getDBCollection().distinct(options.get("distinct"), query);
            for (Object foundCase : foundCases) {
                caseList.add("\"" + foundCase.toString() + "\"");
            }
        } else if (options.get("selector")!=null){
            String selector = options.get("selector");
            BasicDBObject sort = new BasicDBObject("timestamp", -1);
<<<<<<< HEAD
            if (selector.contains("earliest")){
                sort = new BasicDBObject("timestamp", 1);
            }
            int retrieveCount = 1;
            if (selector.contains(" ")){
                try {
                    retrieveCount = Integer.parseInt(selector.substring(selector.indexOf(" ")+1, selector.length()));
                } catch (NumberFormatException e){}
=======
            if (selector.indexOf("earliest")>=0){
                sort = new BasicDBObject("timestamp", 1);
            }
            int retrieveCount = 1;
            if (selector.indexOf(" ")>=0){
                try {
                    retrieveCount = Integer.parseInt(selector.substring(selector.indexOf(" ")+1, selector.length()));
                } catch (Exception e){}
>>>>>>> a2120c8d56f12e11fea2ca75f358e9b7f969cf47
            }
            List<DBObject> foundCases = cases.getDBCollection().find(query).sort(sort).limit(retrieveCount).toArray();
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
        } else {
            List<DBObject> foundCases = cases.getDBCollection().find(query).toArray();
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

//   public String findLatestCaseForOwner(String owner, String queryString, String recent, String since) throws DaoException {
//        try {
//            Collection<String> result = findLatestQuery(owner, queryString, "timestamp", recent, since);
//            return result.iterator().next();
//        } catch (NoSuchElementException ex) {
//            return "{ }";
//        } catch (IOException ex) {
//            throw new DaoException("Unable to find cases for owner " + owner, ex);
//        }
//   }

    private void addRecentQuery(BasicDBObject query, String recent){
            int minutes = Integer.parseInt(recent);
            long millis = minutes * 60 * 1000;
            long now = Calendar.getInstance().getTimeInMillis();
            long cutoff = now - millis;
            BasicDBObject subQuery = new BasicDBObject("$gt", Long.toString(cutoff));
            query.append("timestamp", subQuery);
    }
   
    private void addSinceQuery(BasicDBObject query, String since){
            long cutoff = 0;
            try {
                cutoff = Long.parseLong(since);
            } catch (java.lang.NumberFormatException e) {}
            BasicDBObject subQuery = new BasicDBObject("$gt", Long.toString(cutoff));
            query.append("timestamp", subQuery);
    }
   
    private void addSeriesQuery(BasicDBObject query, String series){
            query.append("case.series", series);
    }
   
    private void addAgentQuery(BasicDBObject query, String agent){
            query.append("case.agent", agent);
    }
   
    private void addSessionIdQuery(BasicDBObject query, String sessionId){
            query.append("case.sessionId", sessionId);
    }
   
    private void addUserIdQuery(BasicDBObject query, String userId){
            query.append("case.userId", userId);
    }

    private String addBinQuery(String queryString, String binQuery){
        queryString = queryString.trim();
        if (queryString.equals("{ }")){
            return binQuery;
        } else {
            binQuery = binQuery.trim();
            queryString = queryString.substring(0, queryString.length()-1)+", "+binQuery.substring(1);
            return queryString;
        }
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

//    public Collection<String> findLatestQueryToRemove(String owner, String queryString, String field, String recent, String since) throws DaoException, IOException {
//        BasicDBObject query = new BasicDBObject(JSONUtil.json2map(queryString));
//        if (!owner.equals(Oddball.ALL)) {
//            //query = new BasicDBObject("case." + OWNERPROPERTY, owner);
//            query.append("case." + OWNERPROPERTY, owner);
//        }
//        if (recent != null) {
//            addRecentQuery(query, recent);
//        }
//        if (since != null) {
//            addSinceQuery(query, since);
//        }
//        BasicDBObject sort = new BasicDBObject(field, -1);
//        List<DBObject> foundCases = cases.getDBCollection().find(query).sort(sort).limit(1).toArray();
//        ArrayList<String> caseList = new ArrayList<String>();
//        for (DBObject foundCase : foundCases) {
//            Map result = JSONUtil.json2map(foundCase.toString());
//            result.put("_id", foundCase.get("_id").toString());
//            String json = JSONUtil.map2json(result);
//            if (json.contains(":  }")) {
//                LOGGER.debug("Reading bad json object");
//                LOGGER.debug(json);
//                json = json.replace(":  }", ": {}");
//            }
//            caseList.add(json);
//        }
//        
//        
//        return caseList;
//    }

    public static String OWNERPROPERTY = "accountId";

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
