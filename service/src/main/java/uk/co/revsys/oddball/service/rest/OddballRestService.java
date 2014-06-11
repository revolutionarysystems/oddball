package uk.co.revsys.oddball.service.rest;

import de.neuland.jade4j.spring.view.JadeViewResolver;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.util.OddballException;
import uk.co.revsys.resource.repository.ResourceRepository;

@Path("/")
public class OddballRestService extends AbstractRestService {

    public OddballRestService(ResourceRepository resourceRepository) {
        LOGGER.log(Priority.DEBUG, "Initialising");
        this.resourceRepository = resourceRepository;
        this.oddball = new Oddball(resourceRepository);
    }

    private final Oddball oddball;
    private ResourceRepository resourceRepository;
    private JadeViewResolver viewResolver;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response announce() throws OddballException {
        return Response.ok("Welcome to odDball").build();
    }

    @GET
    @Path("/ruleSet/{ruleSet}/case/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCases(@PathParam("ruleSet") String ruleSet) throws OddballException {
        Iterable<String> cases = oddball.findCases(ruleSet);
        StringBuffer out = new StringBuffer("[ ");
        for (String aCase : cases){
            out.append(aCase);
            out.append(", ");
        }
        out.delete(out.length()-2, out.length());
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/ruleSet/{ruleSet}/sessionId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctSessionId(@PathParam("ruleSet") String ruleSet) throws OddballException {
        Iterable<String> cases = oddball.findDistinct(ruleSet, "case.sessionId");
        StringBuffer out = new StringBuffer("[ ");
        for (String aCase : cases){
            out.append(aCase);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/ruleSet/{ruleSet}/sessionId/{sessionId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCases(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSet) throws OddballException {
        Iterable<String> cases = oddball.findCases(ruleSet, "{ \"case.sessionId\" : \""+sessionId+"\" }");
        StringBuffer out = new StringBuffer("[ ");
        for (String aCase : cases){
            out.append(aCase);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/ruleSet/{ruleSet}/userId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctUserId(@PathParam("ruleSet") String ruleSet) throws OddballException {
        Iterable<String> cases = oddball.findDistinct(ruleSet, "case.userId");
        StringBuffer out = new StringBuffer("[ ");
        for (String aCase : cases){
            out.append(aCase);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/ruleSet/{ruleSet}/userId/{userId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSet) throws OddballException {
        Iterable<String> cases = oddball.findCases(ruleSet, "{ \"case.userId\" : \""+userId+"\" }");
        StringBuffer out = new StringBuffer("[ ");
        for (String aCase : cases){
            out.append(aCase);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/ruleSet/{ruleSet}/query/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForQuery(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSet) throws OddballException {
        Iterable<String> cases = oddball.findCases(ruleSet, query);
        StringBuffer out = new StringBuffer("[ ");
        for (String aCase : cases){
            out.append(aCase);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }


    @GET
    @Path("/ruleSet/{ruleSet}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response applyRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr, @QueryParam("action") String actionStr) throws OddballException {
        if (caseStr==null && actionStr==null){
            return Response.ok(ruleSet).build();
        } else {
            if (actionStr!=null && (actionStr.equals("clear"))){
                oddball.clearRuleSet(ruleSet);
                return Response.ok("Rule Set "+ruleSet+" cleared.").build();
            }
            if (caseStr!=null){
                Opinion op = oddball.assessCase(ruleSet, new StringCase(caseStr));
                String enrichedCase =  op.getEnrichedCase(ruleSet, caseStr);
                RESULTSLOGGER.log(Priority.INFO,enrichedCase);
                return buildResponse(enrichedCase);
                //return buildResponse(op.getLabel());
            }
            return buildResponse("invalid arguments");
        }
    }

//    @GET
//    @Path("/{type}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response findAll(@PathParam("type") String type, @QueryParam("view") String viewName) {
//        try {
//            Class view = getView(viewName);
//            List<OlogyInstance> results = service.findAll(type, view);
//            return buildResponse(results);
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (ViewNotFoundException ex) {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//    }
//
//    @POST
//    @Path("/{type}/query")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response query(@PathParam("type") String type, @QueryParam("view") String viewName, String json) {
//        try {
//            Class view = getView(viewName);
//            Query query = new QueryImpl(json);
//            List<OlogyInstance> results = service.find(type, query, view);
//            return buildResponse(results);
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (ViewNotFoundException ex) {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//    }
//
//    @GET
//    @Path("/{type}/query")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response query(@PathParam("type") String type, @QueryParam("view") String viewName, @Context UriInfo ui) {
//        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
//        queryParams.remove("view");
//        if (queryParams.isEmpty()) {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//        JSONQuery query = new JSONQuery();
//        for (Entry<String, List<String>> queryParam : queryParams.entrySet()) {
//            query.put(queryParam.getKey(), queryParam.getValue().get(0));
//        }
//        try {
//            Class view = getView(viewName);
//            List<OlogyInstance> results = service.find(type, query, view);
//            return buildResponse(results);
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (ViewNotFoundException ex) {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//    }
//
//    @POST
//    @Path("/{type}")
//    @Consumes(MediaType.TEXT_XML)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createFromXML(String xml) {
//        try {
//            OlogyInstance object = xmlObjectMapper.deserialise(xml, OlogyInstance.class);
//            object = service.create(object);
//            return buildResponse(object);
//        } catch (DeserialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @POST
//    @Path("/{type}")
//    @Consumes(MediaType.WILDCARD)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createFromXMLPlain(String xml) {
//        try {
//            OlogyInstance object = xmlObjectMapper.deserialise(xml, OlogyInstance.class);
//            object = service.create(object);
//            return buildResponse(object);
//        } catch (DeserialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @POST
//    @Path("/{type}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createFromJSON(String json) {
//        try {
//            OlogyInstance object = getJsonObjectMapper().deserialise(json, OlogyInstance.class);
//            object = service.create(object);
//            return buildResponse(object);
//        } catch (DeserialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GET
//    @Path("/{type}/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response findById(@PathParam("type") String type, @PathParam("id") String id) {
//        try {
//            OlogyInstance result = service.findById(type, id);
//            if (result == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }
//            return buildResponse(result);
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @POST
//    @Path("/{type}/{id}")
//    @Consumes(MediaType.TEXT_XML)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateFromXML(@PathParam("type") String type, @PathParam("id") String id, String xml) {
//        try {
//            OlogyInstance existingObject = service.findById(type, id);
//            Node existingObjectXML = DocumentHelper.parseText(xmlObjectMapper.serialise(existingObject)).getRootElement();
//            System.out.println("existingObjectXML = " + existingObjectXML.asXML());
//            Node newObjectXML = DocumentHelper.parseText(xml).getRootElement();
//            System.out.println("newObjectXML = " + newObjectXML.asXML());
//            Node combinedXML = mergeXML(existingObjectXML, newObjectXML);
//            System.out.println("combinedXML = " + combinedXML.asXML());
//            OlogyInstance object = xmlObjectMapper.deserialise(combinedXML.asXML(), OlogyInstance.class);
//            object.setId(id);
//            object = service.update(object);
//            return buildResponse(object);
//        } catch (DeserialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (SerialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DocumentException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @POST
//    @Path("/{type}/{id}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateFromJSON(@PathParam("type") String type, @PathParam("id") String id, String json) {
//        try {
//            OlogyInstance existingObject = service.findById(type, id);
//            JSONObject existingObjectJSON = new JSONObject(getJsonObjectMapper().serialise(existingObject));
//            JSONObject newObjectJSON = new JSONObject(json);
//            JSONObject combinedJSON = mergeJSON(existingObjectJSON, newObjectJSON);
//            OlogyInstance object = getJsonObjectMapper().deserialise(combinedJSON.toString(), OlogyInstance.class);
//            object.setId(id);
//            object = service.update(object);
//            return buildResponse(object);
//        } catch (DeserialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (SerialiserException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @DELETE
//    @Path("/{type}/{id}")
//    public Response delete(@PathParam("type") String type, @PathParam("id") String id) {
//        try {
//            OlogyInstance result = service.findById(type, id);
//            service.delete(result);
//            return Response.status(Response.Status.NO_CONTENT).build();
//        } catch (DaoException ex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    protected JSONObject mergeJSON(JSONObject json1, JSONObject json2) {
//        for (Object key : json2.keySet()) {
//            String keyString = (String) key;
//            Object json2Part = json2.get(keyString);
//            if (json2Part instanceof JSONObject) {
//                Object json1Part = json1.get(keyString);
//                JSONObject json2PartObject = (JSONObject) json2Part;
//                if (json1Part instanceof JSONArray && json2PartObject.has("$add")) {
//                    JSONArray json1PartArray = (JSONArray) json1Part;
//                    JSONArray json2PartArray = json2PartObject.getJSONArray("$add");
//                    for (int i = 0; i < json2PartArray.length(); i++) {
//                        json1PartArray.put(json2PartArray.get(i));
//                    }
//                    json1.put(keyString, json1PartArray);
//                } else {
//                    json1.put(keyString, mergeJSON((JSONObject) json1Part, (JSONObject) json2Part));
//                }
//            } else {
//                json1.put(keyString, json2Part);
//            }
//        }
//        return json1;
//    }
//
//    protected Node mergeXML(Node xml1, Node xml2) {
//        List<Node> children = xml2.selectNodes("*");
//        if (children.isEmpty()) {
//            return xml2;
//        }
//        String action = null;
//        Node actionNode = ((Element)xml2).attribute("action");
//        if (actionNode != null) {
//            action = actionNode.getText();
//        }
//        for (Node xml2Child : children) {
//            String path = xml2Child.getName();
//            Node xml1Child = xml1.selectSingleNode(path);
//            if (xml1Child == null || (action!=null && action.equals("add"))) {
//                xml2Child.detach();
//                ((Element) xml1).add(xml2Child);
//            } else {
//                xml2Child.detach();
//                xml1Child.detach();
//                ((Element) xml1).add(mergeXML(xml1Child, xml2Child));
//
//            }
//        }
//        return xml1;
//    }
//

    static final Logger RESULTSLOGGER = Logger.getLogger("oddball-results");
    static final Logger LOGGER = Logger.getLogger("oddball");

}
