package uk.co.revsys.oddball.service.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ws.rs.GET;
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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.aggregator.AggregationException;
import uk.co.revsys.oddball.bins.BinSetNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;
import uk.co.revsys.user.manager.model.User;

@Path("/")
public class OddballRestService extends AbstractRestService {

    private AuthorisationHandler authorisationHandler;
    
    public OddballRestService(Oddball oddball, AuthorisationHandler authorisationHandler){
        LOGGER.debug("Initialising");
        this.oddball = oddball;
        this.authorisationHandler = authorisationHandler;
    }

    private final Oddball oddball;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response announce(){
        return Response.ok("Welcome to odDball").build();
    }

    @GET
    @Path("/{ruleSet}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response applyRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSet, @QueryParam("case") String caseStr, @QueryParam("inboundTransformer") String inboundTransformer, @QueryParam("persist") String persist, @QueryParam("duplicateRule") String duplicateRule){
        if (ruleSet == null || ruleSet.equals("null")){
            ruleSet = altRuleSet;
        }
        int persistOption = RuleSet.ALWAYSPERSIST;
        if (persist!=null){
            if (persist.equals("never")){
                persistOption = RuleSet.NEVERPERSIST;
            } else {
                if (persist.equals("update")){
                    persistOption = RuleSet.UPDATEPERSIST;
                }
            }
        }
        if (caseStr==null){
            return Response.ok(ruleSet).build();
        } else {
            Opinion op;
            try {
                op = oddball.assessCase(ruleSet, inboundTransformer, new StringCase(caseStr), persistOption, duplicateRule);
            } catch (RuleSetNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (TransformerNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (InvalidCaseException ex) {
                return buildErrorResponse(ex);
            }
            String enrichedCase =  op.getEnrichedCase(ruleSet, caseStr);
            enrichedCase = enrichedCase.replace("\\\"", "\"");
            RESULTSLOGGER.info(enrichedCase);
            return buildResponse(enrichedCase);
        }
    }

    @GET
    @Path("/{ruleSet}/case/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCases(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findCases(ruleSet.trim(), options));
            }
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
    @Path("/{ruleSet}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer, @QueryParam("recent") String recent, @QueryParam("since") String since){
        if (ruleSet == null || ruleSet.equals("null")){
            ruleSet = altRuleSet;
        }
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        if (recent!=null){
            options.put("recent", recent);
        }
        if (since!=null){
            options.put("since", since);
        }
            String caseStr;
        try {
            caseStr = oddball.findLatestQueryCase(ruleSet, "{ }", options);
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
    @Path("/{ruleSet}/id/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCase(@PathParam("ruleSet") String ruleSets, @PathParam("id") String id, @QueryParam("account") String owner, @QueryParam("transformer") String transformer, @QueryParam("action") String action){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            if (action!=null && action.equals("delete")){
                for (String ruleSet : ruleSetNames){
                    oddball.deleteCaseById(ruleSet, id, options);
                }
            } else {
                for (String ruleSet : ruleSetNames){
                    cases.addAll(oddball.findCaseById(ruleSet, id, options));
                }
            }
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
    @Path("/{ruleSet}/rule/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRules(@PathParam("ruleSet") String ruleSet, @QueryParam("transformer") String transformer, @QueryParam("prefix") String prefix, @QueryParam("source") String source){
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Iterable<String> rules;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", transformer);
        options.put("prefix", prefix);
        options.put("source", source);
        try {
            rules = oddball.findRules(ruleSet, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
            return buildErrorResponse(ex);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String rule : rules){
            out.append(rule);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{ruleSet}/rule/save")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveRules(@PathParam("ruleSet") String ruleSet, @QueryParam("transformer") String transformer, @QueryParam("prefix") String prefix, @QueryParam("source") String source){
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Iterable<String> rules;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", transformer);
        options.put("prefix", prefix);
        options.put("source", source);
        try {
            rules = oddball.saveRules(ruleSet, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
            return buildErrorResponse(ex);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String rule : rules){
            out.append(rule);
            out.append(", ");
        }
        if (out.length()>2){
            out.delete(out.length()-2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @POST
    @Path("/{ruleSet}/rule/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertRule(@PathParam("ruleSet") String ruleSet, @QueryParam("label") String label, @QueryParam("prefix") String prefix, @QueryParam("rule") String ruleString){
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        Iterable<String> rules;
        options.put("prefix", prefix);
        try {
            oddball.addExtraRule(ruleSet, prefix, label, ruleString, "inserted");
            rules = oddball.findRules(ruleSet, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
            return buildErrorResponse(ex);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String rule : rules){
            out.append(rule);
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
    public Response findDistinctSessionId(@PathParam("ruleSet") String ruleSets, @QueryParam("ruleSet") String altRuleSets, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("since") String since, @QueryParam("transformer") String transformer){
        if (ruleSets == null || ruleSets.equals("null")){
            ruleSets = altRuleSets;
        }
        return findDistinctSeriesService(ruleSets, owner, recent, since, transformer);
    }

    @GET
    @Path("/{ruleSet}/series/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctSeries(@PathParam("ruleSet") String ruleSets, @QueryParam("ruleSet") String altRuleSets, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("since") String since, @QueryParam("transformer") String transformer){
        if (ruleSets == null || ruleSets.equals("null")){
            ruleSets = altRuleSets;
        }
        return findDistinctSeriesService(ruleSets, owner, recent, since, transformer);
    }

    public Response findDistinctSeriesService(String ruleSets, String owner, String recent, String since, String transformer){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        options.put("recent", recent);
        options.put("since", since);
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findDistinct(ruleSet, "case.series", options));
                cases.addAll(oddball.findDistinct(ruleSet, "case.sessionId", options));
            }
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
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
    public Response findCasesForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui){
        return findCasesForSeriesService(sessionId, ruleSets, owner, ui);
    }
     
    @GET
    @Path("/{ruleSet}/series/{series}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForSeries(@PathParam("series") String series, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui){
        return findCasesForSeriesService(series, ruleSets, owner, ui);
    }
     
    public Response findCasesForSeriesService(String sessionId, String ruleSets, String owner, UriInfo ui){
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        // options can include owner, transformer, aggregator and aggregator
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()){
            options.put(key, queryParams.getFirst(key));
        }
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
//        HashMap<String, String> options = new HashMap<String, String>();
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findQueryCases(ruleSet, "{ \"case.sessionId\" : \""+sessionId+"\" }", options));
                cases.addAll(oddball.findQueryCases(ruleSet, "{ \"case.series\" : \""+sessionId+"\" }", options));
            }
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (AggregationException ex) {
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
    public Response findLatestCaseForSession(@PathParam("sessionId") String series, @QueryParam("sessionId") String altSeries, @PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        if (ruleSet == null || ruleSet.equals("null")){
            ruleSet = altRuleSet;
        }
        if (series == null || series.equals("null")){
            series = altSeries;
        }
        return findLatestCaseForSeriesService(series, ruleSet, owner, transformer, "sessionId");
    }

    @GET
    @Path("/{ruleSet}/series/{series}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForSeries(@PathParam("series") String series, @QueryParam("series") String altSeries, @PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        if (ruleSet == null || ruleSet.equals("null")){
            ruleSet = altRuleSet;
        }
        if (series == null || series.equals("null")){
            series = altSeries;
        }
        return findLatestCaseForSeriesService(series, ruleSet, owner, transformer, "series");
    }
    

    public Response findLatestCaseForSeriesService(String series, String ruleSet, String owner, String transformer, String seriesName){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
            String caseStr;
        try {
            caseStr = oddball.findLatestQueryCase(ruleSet, "{ \"case."+seriesName+"\" : \""+series+"\" }", options);
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
    public Response findDistinctUserId(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("since") String since, @QueryParam("transformer") String transformer){
        return findDistinctAgentService(ruleSets, owner, recent, since, transformer);
    }

    @GET
    @Path("/{ruleSet}/agent/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctAgent(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("since") String since, @QueryParam("transformer") String transformer){
        return findDistinctAgentService(ruleSets, owner, recent, since, transformer);
    }

    public Response findDistinctAgentService(String ruleSets, String owner, String recent, String since, String transformer){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        options.put("recent", recent);
        options.put("since", since);
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findDistinct(ruleSet, "case.userId", options));
                cases.addAll(oddball.findDistinct(ruleSet, "case.agent", options));
            }
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
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
    public Response findCasesForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui){
            return findCasesForAgentService(userId, ruleSets, owner, ui);
    }

    @GET
    @Path("/{ruleSet}/agent/{agent}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForAgent(@PathParam("agent") String agent, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui){
            return findCasesForAgentService(agent, ruleSets, owner, ui);
    }

    public Response findCasesForAgentService(String agent, String ruleSets, String owner, UriInfo ui){
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        // options can include owner, transformer, aggregator and aggregator
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()){
            options.put(key, queryParams.getFirst(key));
        }
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findQueryCases(ruleSet, "{ \"case.userId\" : \""+agent+"\" }", options));
                cases.addAll(oddball.findQueryCases(ruleSet, "{ \"case.agent\" : \""+agent+"\" }", options));
            }
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (AggregationException ex) {
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
        return findLatestCaseForAgentService(userId, ruleSet, owner, transformer, "userId");
    }

    @GET
    @Path("/{ruleSet}/agent/{agent}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForAgent(@PathParam("agent") String agent, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        return findLatestCaseForAgentService(agent, ruleSet, owner, transformer, "agent");
    }

    public Response findLatestCaseForAgentService(String agent, String ruleSet, String owner, String transformer, String agentName){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String caseStr;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            caseStr = oddball.findLatestQueryCase(ruleSet, "{ \"case."+agentName+"\" : \""+agent+"\" }", options);
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
    public Response findCasesForQuery(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSets, @QueryParam("ruleSet") String altRuleSets, @QueryParam("account") String owner, @Context UriInfo ui){
        if (ruleSets == null || ruleSets.equals("null")){
            ruleSets = altRuleSets;
        }
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        // options can include owner, transformer, aggregator and aggregator
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()){
            options.put(key, queryParams.getFirst(key));
        }
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findQueryCases(ruleSet, query, options));
            }
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (AggregationException ex) {
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
    @Path("/{ruleSet}/query/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestQueryCase(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String caseStr;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("transformer", transformer);
        try {
            caseStr = oddball.findLatestQueryCase(ruleSet, query, options);
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
    @Path("/{ruleSet}/bin/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctBins(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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
    public Response findCasesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer, @Context UriInfo ui){
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        // options can include owner, transformer, aggregator and aggregator
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()){
            options.put(key, queryParams.getFirst(key));
        }
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        options.put("owner", owner);
        options.put("transformer", transformer);
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findCasesInBin(ruleSet, binLabel, options));
            }
        } catch (UnknownBinException ex) {
            return buildErrorResponse(ex);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (AggregationException ex) {
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
    @Path("/{ruleSet}/bin/{binLabel}/distinct")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctPropertiesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("property") String property, @QueryParam("recent") String recent, @QueryParam("since") String since){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("property", property);
        options.put("recent", recent);
        options.put("since", since);
        ArrayList<String> cases= new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames){
                cases.addAll(oddball.findDistinctPropertyInBin(ruleSet, binLabel, options));
            }
        } catch (UnknownBinException ex) {
            return buildErrorResponse(ex);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
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
    @Path("/{ruleSet}/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearRuleSet(@PathParam("ruleSet") String ruleSet){
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearRuleSet(ruleSet);
        return Response.ok("Rule Set "+ruleSet+" cleared.").build();
    }

    @GET
    @Path("/{ruleSet}/reload")
    @Produces(MediaType.TEXT_PLAIN)
    
    public Response reloadRuleSet(@PathParam("ruleSet") String ruleSet){
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearTransformers();
        return Response.ok("Transformers cleared.").build();
    }
    
    private String getOwner(String ownerParam){
        if(authorisationHandler.isAdministrator()){
            if(ownerParam == null){
                String owner = getAccount();
                if(owner == null){
                    return "_all";
                }
                return owner;
            }else{
                return ownerParam;
            }
        }else{
            String owner = getAccount();
            if(owner == null){
                return null;
            }
            return owner;
        }
    }
    
    private String getAccount(){
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()){
            return null;
        }
        User user = subject.getPrincipals().oneByType(User.class);
        if(user!=null){
            return user.getAccount();
        }
        return null;
    }


    
    static final Logger RESULTSLOGGER = LoggerFactory.getLogger("oddball-results");
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
