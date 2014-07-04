package uk.co.revsys.oddball.service.rest;

import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import uk.co.revsys.oddball.Oddball;
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
    public Response findCases(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        Iterable<String> cases;
        try {
            cases = oddball.findCasesForOwner(ruleSet, owner);
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
    @Path("/{ruleSet}/sessionId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctSessionId(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("recent") String recent){
        Iterable<String> cases;
        try {
            cases = oddball.findDistinct(ruleSet, owner, "case.sessionId", recent);
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
    public Response findCasesForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        Iterable<String> cases;
        try {
            cases = oddball.findQueryCasesForOwner(ruleSet, owner, "{ \"case.sessionId\" : \""+sessionId+"\" }");
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
    @Path("/{ruleSet}/userId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctUserId(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("recent") String recent){
        Iterable<String> cases;
        try {
            cases = oddball.findDistinct(ruleSet, owner, "case.userId",recent);
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
    public Response findCasesForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        Iterable<String> cases;
        try {
            cases = oddball.findQueryCasesForOwner(ruleSet, owner, "{ \"case.userId\" : \""+userId+"\" }");
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
    @Path("/{ruleSet}/query/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForQuery(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        Iterable<String> cases;
        try {
            cases = oddball.findQueryCasesForOwner(ruleSet, owner, query);
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
    public Response findCasesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        Iterable<String> cases;
        try {
            cases = oddball.findCasesInBinForOwner(ruleSet, owner, binLabel);
        } catch (UnknownBinException ex) {
            return buildErrorResponse(ex);
        } catch (RuleSetNotLoadedException ex) {
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
            RESULTSLOGGER.log(Priority.INFO,enrichedCase);
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

    static final Logger RESULTSLOGGER = Logger.getLogger("oddball-results");
    static final Logger LOGGER = Logger.getLogger("oddball");

}
