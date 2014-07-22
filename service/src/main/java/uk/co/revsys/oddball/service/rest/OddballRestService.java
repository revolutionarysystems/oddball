package uk.co.revsys.oddball.service.rest;

import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.bins.BinSetNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;

@Path("/")
public class OddballRestService extends AbstractRestService {

    public OddballRestService(Oddball oddball){
        LOGGER.debug("Initialising");
        this.oddball = oddball;
    }

    private final Oddball oddball;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response announce(){
        return Response.ok("Welcome to odDball").build();
    }

    @GET
    @Path("/{ruleSet}/case/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCases(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        Iterable<String> cases;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            cases = oddball.findCases(ruleSet, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}/sessionId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctSessionId(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("transformer") String transformer){
        Iterable<String> cases;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            cases = oddball.findDistinct(ruleSet, "case.sessionId", recent, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}/sessionId/{sessionId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        Iterable<String> cases;
        try {
            cases = oddball.findQueryCases(ruleSet, "{ \"case.sessionId\" : \""+sessionId+"\" }", options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}/sessionId/{sessionId}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
            String caseStr;
        try {
            caseStr = oddball.findLatestQueryCase(ruleSet, "{ \"case.sessionId\" : \""+sessionId+"\" }", options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(caseStr).build();
    }

    @GET
    @Path("/{ruleSet}/userId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctUserId(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("transformer") String transformer){
        Iterable<String> cases;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            cases = oddball.findDistinct(ruleSet, "case.userId",recent, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}/userId/{userId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        Iterable<String> cases;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            cases = oddball.findQueryCases(ruleSet, "{ \"case.userId\" : \""+userId+"\" }", options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}/userId/{userId}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        String caseStr;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            caseStr = oddball.findLatestQueryCase(ruleSet, "{ \"case.userId\" : \""+userId+"\" }", options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(caseStr).build();
    }

    @GET
    @Path("/{ruleSet}/query/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForQuery(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        Iterable<String> cases;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            cases = oddball.findQueryCases(ruleSet, query, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}/bin/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctBins(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        Iterable<String> binLabels; 
        try {
            binLabels = oddball.listBinLabels(owner);
        } catch (BinSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String binLabel : binLabels){
            out.append("\""+binLabel+"\"");
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }


    @GET
    @Path("/{ruleSet}/bin/{binLabel}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        Iterable<String> cases;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            cases = oddball.findCasesInBin(ruleSet, binLabel, options);
        } catch (UnknownBinException ex) {
            return buildErrorResponse(ex);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        } catch (BinSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
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
    @Path("/{ruleSet}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response applyRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr){
        if (caseStr==null){
            return Response.ok(ruleSet).build();
        } else {
            Opinion op;
            try {
                op = oddball.assessCase(ruleSet, new StringCase(caseStr));
            } catch (RuleSetNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (InvalidCaseException ex) {
                return buildErrorResponse(ex);
            }
            String enrichedCase =  op.getEnrichedCase(ruleSet, caseStr);
            RESULTSLOGGER.info(enrichedCase);
            return buildResponse(enrichedCase);
        }
    }

    @GET
    @Path("/{ruleSet}/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr){
        oddball.clearRuleSet(ruleSet);
        return Response.ok("Rule Set "+ruleSet+" cleared.").build();
    }

    @GET
    @Path("/{ruleSet}/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reloadRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr){
        try {
            oddball.reloadRuleSet(ruleSet);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok("Rule Set "+ruleSet+" reloaded.").build();
    }

    @GET
    @Path("/{ruleSet}/bin/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reloadBinSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr){
        try {
            oddball.reloadBinSet();
        } catch (BinSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok("Bin Set reloaded.").build();
    }

    @GET
    @Path("/transformer/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearTransformers(){
        oddball.clearTransformers();
        return Response.ok("Transformers cleared.").build();
    }

    static final Logger RESULTSLOGGER = LoggerFactory.getLogger("oddball-results");
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
