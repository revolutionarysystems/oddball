/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.rules;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.jongo.Find;
import org.jongo.FindOne;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.oddball.util.OddUtil;

/**
 *
 * @author Andrew
 */
public class MongoDBHelper {

    private final MongoCollection cases;
    private final DB db;

    public MongoDBHelper(String dbName, boolean inMemory) {
        db = MongoDBFactory.getDBInstance(dbName, inMemory);
        Jongo jongo = new Jongo(db);
        cases = jongo.getCollection("cases");
    }

    public String getName() {
        return db.getName();
    }

    public void dropCases() {
        cases.drop();
    }

    public String insertCase(String content) throws IOException {
        // jongo interprets # as parameter.
        content = content.replace("#", "(hash)");
        Map<String, Object> mapCase = JSONUtil.json2map(content);
        if (!mapCase.containsKey("_id")) {
            String id = UUID.randomUUID().toString();
            mapCase.put("_id",id);
            content = JSONUtil.map2json(mapCase);
        }
//        if (!content.contains("_id")) {
//            String id = UUID.randomUUID().toString();
//            content = content.substring(0, content.lastIndexOf("}")) + ", \"_id\":\"" + id + "\" }";
//        }
        WriteResult wr = cases.insert(content);
//        if (content.contains("{}") || content.contains(":  }")) {
//            LOGGER.debug("Writing empty or bad json object");
//            LOGGER.debug(content);
//        }
        //cases.getDBCollection().insert(new BasicDBObject(JSONUtil.json2map(content)));
        // When supported by fongo
        //System.out.println(wr.getUpsertedId());
        //return wr.getUpsertedId().toString();
        // for now

        String id = (String) JSONUtil.json2map(content).get("_id");
        return id;

    }


    public void ensureIndex(Map indexMap) {
        cases.getDBCollection().createIndex(new BasicDBObject(indexMap));
        //System.out.println(cases.getDBCollection().getIndexInfo());
    }

    public String checkAlreadyExists(String duplicateQuery) {
        FindOne found = cases.findOne(duplicateQuery);
        if (found.as(Map.class) == null) {
            return null;
        } else {
            return (String) found.as(Map.class).get("_id").toString();
        }
    }

    public void removeCase(String caseId) {
        cases.remove("{ \"_id\": \"" + caseId + "\" }");
//        cases.remove("{_id: #}", new ObjectId(caseId));

    }

    public boolean testCase(String query, String caseId) {
        if (query.length() < 1) {
            query = "{ }";
        }
        String queryMod = "{ \"_id\" : \"" + caseId + "\", " + query.substring(1);
        FindOne found = cases.findOne(queryMod);
        boolean foundBool = found.as(Map.class) != null;
//        System.out.println("Test Query: "+queryMod+Boolean.toString(foundBool));
        return foundBool;
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

    private BasicDBObject buildQuery(String owner, String queryString, Map<String, String> options) throws InvalidTimePeriodException, IOException {
        if (queryString == null) {
            queryString = "{ }";
        }
        if (options.get("binQuery") != null) {
            queryString = addBinQuery(queryString, options.get("binQuery"));
        }
        BasicDBObject query = new BasicDBObject(JSONUtil.json2map(queryString));
        if (owner!=null && !owner.equals(Oddball.ALL)) {
            String ownerProperty = OWNERPROPERTY;
            if (options.containsKey("ownerProperty")){
                ownerProperty = options.get("ownerProperty");
            }
            query.append("case." + ownerProperty, owner);
        }
        if (options.get("recent") != null) {
            addRecentQuery(query, options.get("recent"));
        }
        if (options.get("ago") != null) {
            addAgoQuery(query, options.get("ago"));
        }
        if (options.get("since") != null) {
            addSinceQuery(query, options.get("since"));
        }
        if (options.get("before") != null) {
            addBeforeQuery(query, options.get("before"));
        }
        if (options.get("caseRecent") != null) {
            addCaseRecentQuery(query, options.get("caseRecent"));
        }
        if (options.get("caseAgo") != null) {
            addCaseAgoQuery(query, options.get("caseAgo"));
        }
        if (options.get("caseSince") != null) {
            addCaseSinceQuery(query, options.get("caseSince"));
        }
        if (options.get("caseBefore") != null) {
            addCaseBeforeQuery(query, options.get("caseBefore"));
        }
        if (options.get("caseInPeriod") != null) {
            addCaseInPeriodQuery(query, options.get("caseInPeriod"));
        }
        if (options.get("forEach") != null) {
            addForEachQuery(query, options.get("forEach"), options.get("forEachValue"));
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
        return query;
    }

    public Collection<String> findCasesForOwner(String owner, String queryString, Map<String, String> options) throws DaoException, InvalidTimePeriodException, IOException {
        BasicDBObject query = buildQuery(owner, queryString, options);
        ArrayList<String> caseList = new ArrayList<String>();
        if (options.get("count") != null && ((String)options.get("count")).equals("true")) {
            long foundCount = cases.getDBCollection().count(query);
            caseList.add(Long.toString(foundCount));
        } else if (options.get("distinct") != null) {
            long time = new Date().getTime();
            List foundCases = cases.getDBCollection().distinct(options.get("distinct"), query);
            for (Object foundCase : foundCases) {
                if (foundCase != null) {
                    if (foundCase instanceof String) {
                        caseList.add("\"" + foundCase.toString() + "\"");
                    } else {
                        caseList.add(foundCase.toString());
                    }
                } else {
                    caseList.add("\"" + "null" + "\"");
                }
            }
        } else if (options.get("selector") != null) {
            String selector = options.get("selector");
            BasicDBObject sort = new BasicDBObject("timestamp", -1);
            if (selector.contains("earliest")) {
                sort = new BasicDBObject("timestamp", 1);
            }
            if (selector.contains("caseLatest")) {
                sort = new BasicDBObject("case.time", -1);
            }
            if (selector.contains("caseEarliest")) {
                sort = new BasicDBObject("case.time", 1);
            }
            int retrieveCount = 1;
            int skipCount = 0;
            if (selector.contains(" ")) {
                try {
                    String retrieveCountStr = selector.substring(selector.indexOf(" ") + 1, selector.length());
                    if (retrieveCountStr.contains("-")) {
                        String[] limits = retrieveCountStr.split("-");
                        int upperLimit = Integer.parseInt(limits[1].trim());
                        skipCount = Integer.parseInt(limits[0].trim()) - 1;
                        retrieveCount = upperLimit - skipCount;
                        if (retrieveCount < 1) {
                            retrieveCount = 1;
                        }
                    } else {
                        retrieveCount = Integer.parseInt(retrieveCountStr);
                    }
                } catch (NumberFormatException e) {
                }
            }
            List<DBObject> foundCases;
            long time = new Date().getTime();
            if (skipCount <= 0) {
                foundCases = cases.getDBCollection().find(query).sort(sort).limit(retrieveCount).toArray();
            } else {
                foundCases = cases.getDBCollection().find(query).sort(sort).skip(skipCount).limit(retrieveCount).toArray();
            }
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
            long time = new Date().getTime();
            List<DBObject> foundCases = cases.getDBCollection().find(query).toArray();
            for (DBObject foundCase : foundCases) {
//                Map result = JSONUtil.json2map(foundCase.toString());
//                result.put("_id", foundCase.get("_id").toString());
//                String json = JSONUtil.map2json(result);
                String json = foundCase.toString();
//                if (json.contains(":  }")) {
//                    LOGGER.debug("Reading bad json object");
//                    LOGGER.debug(json);
//                    json = json.replace(":  }", ": {}");
//                }
                caseList.add(json);
            }
        }
        return caseList;
    }

    public void deleteCasesForOwner(String owner, String queryString, Map<String, String> options) throws IOException, DaoException, InvalidTimePeriodException {
        BasicDBObject query = buildQuery(owner, queryString, options);

//        ArrayList<String> caseList = new ArrayList<String>();
        WriteResult wr = cases.getDBCollection().remove(query);
    }

    public Collection<String> findCaseById(String owner, String id) throws DaoException {
        ArrayList<String> caseList = new ArrayList<String>();
        Find found;
        try {
            found = cases.find("{_id: #}", new ObjectId(id));
        } catch (IllegalArgumentException e) {
            found = cases.find("{_id: #}", id);
        }

        Iterable<Map> foundCases = found.as(Map.class);
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
    private void addRecentQuery(BasicDBObject query, String recent) throws InvalidTimePeriodException {
        long millis = new OddUtil().parseTimePeriod(recent, "m");
        long now = Calendar.getInstance().getTimeInMillis();
        long cutoff = now - millis;
        BasicDBObject subQuery = new BasicDBObject("$gt", Long.toString(cutoff));
        if (query.containsField("timestamp")) {
            ((BasicDBObject) query.get("timestamp")).append("$gt", Long.toString(cutoff));
        } else {
            query.append("timestamp", subQuery);
        }
    }

    private void addCaseRecentQuery(BasicDBObject query, String recent) throws InvalidTimePeriodException {
        long millis = new OddUtil().parseTimePeriod(recent, "m");
        long now = Calendar.getInstance().getTimeInMillis();
        long cutoff = now - millis;
        BasicDBObject subQuery = new BasicDBObject("$gt", cutoff);
        if (query.containsField("case.time")) {
            ((BasicDBObject) query.get("case.time")).append("$gt", cutoff);
        } else {
            query.append("case.time", subQuery);
        }
    }

    private void addAgoQuery(BasicDBObject query, String ago) throws InvalidTimePeriodException {
        long millis = new OddUtil().parseTimePeriod(ago, "m");
        long now = Calendar.getInstance().getTimeInMillis();
        long cutoff = now - millis;
        BasicDBObject subQuery = new BasicDBObject("$lte", Long.toString(cutoff));
        if (query.containsField("timestamp")) {
            ((BasicDBObject) query.get("timestamp")).append("$lte", Long.toString(cutoff));
        } else {
            query.append("timestamp", subQuery);
        }
    }

    private void addCaseAgoQuery(BasicDBObject query, String ago) throws InvalidTimePeriodException {
        long millis = new OddUtil().parseTimePeriod(ago, "m");
        long now = Calendar.getInstance().getTimeInMillis();
        long cutoff = now - millis;
        BasicDBObject subQuery = new BasicDBObject("$lte", cutoff);
        if (query.containsField("case.time")) {
            ((BasicDBObject) query.get("case.time")).append("$lte", cutoff);
        } else {
            query.append("case.time", subQuery);
        }
    }

    private void addSinceQuery(BasicDBObject query, String since) {
        long cutoff = 0;
        try {
            cutoff = Long.parseLong(since);
        } catch (java.lang.NumberFormatException e) {
        }
        BasicDBObject subQuery = new BasicDBObject("$gte", Long.toString(cutoff));
        if (query.containsField("timestamp")) {
            ((BasicDBObject) query.get("timestamp")).append("$gte", Long.toString(cutoff));
        } else {
            query.append("timestamp", subQuery);
        }
    }

    private void addCaseSinceQuery(BasicDBObject query, String since) {
        long cutoff = 0;
        try {
            cutoff = Long.parseLong(since);
        } catch (java.lang.NumberFormatException e) {
        }
        BasicDBObject subQuery = new BasicDBObject("$gte", cutoff);
        if (query.containsField("case.time")) {
            ((BasicDBObject) query.get("case.time")).append("$gte", cutoff);
        } else {
            query.append("case.time", subQuery);
        }
    }

    private void addBeforeQuery(BasicDBObject query, String before) {
        long cutoff = 0;
        try {
            cutoff = Long.parseLong(before);
        } catch (java.lang.NumberFormatException e) {
        }
        BasicDBObject subQuery = new BasicDBObject("$lt", Long.toString(cutoff));
        if (query.containsField("timestamp")) {
            ((BasicDBObject) query.get("timestamp")).append("$lt", Long.toString(cutoff));
        } else {
            query.append("time", subQuery);
        }
    }

    private void addCaseBeforeQuery(BasicDBObject query, String before) {
        long cutoff = 0;
        try {
            cutoff = Long.parseLong(before);
        } catch (java.lang.NumberFormatException e) {
        }
        BasicDBObject subQuery = new BasicDBObject("$lt", cutoff);
        if (query.containsField("case.time")) {
            ((BasicDBObject) query.get("case.time")).append("$lt", cutoff);
        } else {
            query.append("case.time", subQuery);
        }
    }

    private void addCaseInPeriodQuery(BasicDBObject query, String inPeriod) {
        long startCutoff = 0;
        long endCutoff = 0;
        try {
            String[]limits = inPeriod.split("-");
            startCutoff = Long.parseLong(limits[0]);
            endCutoff = Long.parseLong(limits[1]);
        } catch (java.lang.NumberFormatException e) {
        }
        BasicDBObject subQuery = new BasicDBObject("$lt", endCutoff);
        if (query.containsField("case.startTime")) {
            ((BasicDBObject) query.get("case.startTime")).append("$lt", endCutoff);
        } else {
            query.append("case.startTime", subQuery);
        }
        BasicDBObject subQuery2 = new BasicDBObject("$gte", startCutoff);
        if (query.containsField("case.endTime")) {
            ((BasicDBObject) query.get("case.endTime")).append("$gte", startCutoff);
        } else {
            query.append("case.endTime", subQuery2);
        }
    }

    private void addForEachQuery(BasicDBObject query, String forEach, String forEachValue) {
        query.append(forEach, forEachValue);
    }

    private void addSeriesQuery(BasicDBObject query, String series) {
        query.append("case.series", series);
    }

    private void addAgentQuery(BasicDBObject query, String agent) {
        query.append("case.agent", agent);
    }

    private void addSessionIdQuery(BasicDBObject query, String sessionId) {
        query.append("case.sessionId", sessionId);
    }

    private void addUserIdQuery(BasicDBObject query, String userId) {
        query.append("case.userId", userId);
    }

    private String addBinQuery(String queryString, String binQuery) {
        queryString = queryString.trim();
        if (queryString.equals("{ }")) {
            return binQuery;
        } else {
            binQuery = binQuery.trim();
            queryString = queryString.substring(0, queryString.length() - 1) + ", " + binQuery.substring(1);
            return queryString;
        }
    }

    public Collection<String> findDistinctQuery(String owner, String queryString, String field, String recent, String since) throws DaoException, IOException, InvalidTimePeriodException {
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

    public String getDbStats(){
        return db.getStats().toString();
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
