package uk.co.revsys.oddball.service.rest;

import com.fasterxml.jackson.core.JsonParseException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.FilterException;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.OwnerMissingException;
import uk.co.revsys.oddball.ProcessorNotLoadedException;
import uk.co.revsys.oddball.ResourceNotLoadedException;
import uk.co.revsys.oddball.ResourceNotUploadedException;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.aggregator.AggregationException;
import uk.co.revsys.oddball.aggregator.ComparisonException;
import uk.co.revsys.oddball.bins.BinSetNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.identifier.IdentificationSchemeNotLoadedException;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.oddball.util.OddUtil;
import uk.co.revsys.user.manager.model.User;

@Path("/")
public class OddballRestService extends AbstractRestService {

    /**
     * ********************
     */
    // Security
    /**
     * ********************
     */
    private AuthorisationHandler authorisationHandler;

    private String getAccount() {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            return null;
        }
        User user = subject.getPrincipals().oneByType(User.class);
        if (user != null) {
            return user.getAccount();
        }
        return null;
    }

    private String getOwner(String ownerParam, HashMap<String, String> options) {
        if (ownerParam == null) {
            if (options.get("owner") != null) {
                ownerParam = options.get("owner");
            } else {
                if (options.get("account") != null) {
                    ownerParam = options.get("account");
                }
            }
        }
        if (authorisationHandler.isAdministrator()) {
            if (ownerParam == null) {
                String owner = getAccount();
                if (owner == null) {
                    return "_all";
                }
                return owner;
            } else {
                return ownerParam;
            }
        } else {
            String owner = getAccount();
            if (owner == null) {
                return null;
            }
            return owner;
        }
    }

    /**
     * ********************
     */
    // General Functions
    /**
     * ********************
     */
    private HashMap<String, String> decodeOptions(UriInfo ui) throws UnsupportedEncodingException {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, java.net.URLDecoder.decode(queryParams.getFirst(key), "UTF-8"));
        }
        return options;
    }

    private String assembleFromList(Collection<String> cases) {
        StringBuilder out = new StringBuilder("[ ");
        for (String aCase : cases) {
            out.append(aCase);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return out.toString();
    }

    public OddballRestService(Oddball oddball, AuthorisationHandler authorisationHandler) {
        LOGGER.debug("Initialising");
        this.oddball = oddball;
        this.authorisationHandler = authorisationHandler;
    }

    private final Oddball oddball;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response announce() {
        return Response.ok("Welcome to odDball").build();
    }

    @POST
    @Path("/login")
    public Response login() {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            SecurityUtils.getSubject().getSession(true);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{ruleSet}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response applyRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSet, @QueryParam("case") String caseStr, @QueryParam("inboundTransformer") String inboundTransformer, @QueryParam("persist") String persist, @QueryParam("duplicateRule") String duplicateRule, @QueryParam("avoidRule") String avoidRule, @QueryParam("ensureIndexes") String ensureIndexes, @Context UriInfo ui) throws IOException {
        HashMap<String, String> options = decodeOptions(ui);
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = altRuleSet;
        }
        if (ruleSet.contains("/")) {
            String ownerPrefix = ruleSet.substring(0, ruleSet.indexOf("/") + 1);
            ruleSet = ruleSet.substring(ruleSet.indexOf("/") + 1);
            for (String key : options.keySet()) {
                options.put(key, options.get(key).replace("{owner}", ownerPrefix).replace("{account}", ownerPrefix));
            }
            options.put("ownerDir", ownerPrefix.substring(0, ownerPrefix.length() - 1));
        }
        int persistOption = RuleSet.ALWAYSPERSIST;
        if (persist != null) {
            if (persist.equals("never")) {
                persistOption = RuleSet.NEVERPERSIST;
            } else {
                if (persist.equals("update")) {
                    persistOption = RuleSet.UPDATEPERSIST;
                }
            }
        }
        if (caseStr == null) {
            return Response.ok(ruleSet).build();
        } else {
            Collection<String> assessment;
            try {
                assessment = oddball.assessCase(ruleSet, inboundTransformer, new StringCase(caseStr), persistOption, duplicateRule, null, options);
            } catch (RuleSetNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (ParseException ex) {
                return buildErrorResponse(ex);
            } catch (TransformerNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (InvalidCaseException ex) {
                return buildErrorResponse(ex);
            } catch (ComparisonException ex) {
                return buildErrorResponse(ex);
            } catch (InvalidTimePeriodException ex) {
                return buildErrorResponse(ex);
            } catch (UnknownBinException ex) {
                return buildErrorResponse(ex);
            } catch (DaoException ex) {
                return buildErrorResponse(ex);
            } catch (AggregationException ex) {
                return buildErrorResponse(ex);
            } catch (ProcessorNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (FilterException ex) {
                return buildErrorResponse(ex);
            } catch (IdentificationSchemeNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (OwnerMissingException ex) {
                return buildErrorResponse(ex);
            }
            if (assessment.size() == 1 && !options.containsKey("processor")) {
                RESULTSLOGGER.info(assessment.iterator().next());
                return buildResponse(assessment.iterator().next());
            } else {
                return Response.ok(assembleFromList(assessment)).build();
            }
        }
    }

    @GET
    @Path("/{ownerDir}/{ruleSet}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response applyOwnerRuleSet(@PathParam("ownerDir") String ownerDir, @PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSet, @QueryParam("case") String caseStr, @QueryParam("inboundTransformer") String inboundTransformer, @QueryParam("persist") String persist, @QueryParam("duplicateRule") String duplicateRule, @QueryParam("avoidRule") String avoidRule, @QueryParam("ensureIndexes") String ensureIndexes, @Context UriInfo ui) throws IOException {
        HashMap<String, String> options = decodeOptions(ui);
        for (String key : options.keySet()) {
            options.put(key, options.get(key).replace("{owner}", ownerDir + "/").replace("{account}", ownerDir + "/"));
        }
        options.put("ownerDir", ownerDir);
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = altRuleSet;
        }
        int persistOption = RuleSet.ALWAYSPERSIST;
        if (persist != null) {
            if (persist.equals("never")) {
                persistOption = RuleSet.NEVERPERSIST;
            } else {
                if (persist.equals("update")) {
                    persistOption = RuleSet.UPDATEPERSIST;
                }
            }
        }
        if (caseStr == null) {
            return Response.ok(ruleSet).build();
        } else {
            Collection<String> assessment;
            try {
                assessment = oddball.assessCase(ownerDir + "/" + ruleSet, inboundTransformer, new StringCase(caseStr), persistOption, duplicateRule, null, options);
            } catch (RuleSetNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (ParseException ex) {
                return buildErrorResponse(ex);
            } catch (TransformerNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (InvalidCaseException ex) {
                return buildErrorResponse(ex);
            } catch (ComparisonException ex) {
                return buildErrorResponse(ex);
            } catch (InvalidTimePeriodException ex) {
                return buildErrorResponse(ex);
            } catch (UnknownBinException ex) {
                return buildErrorResponse(ex);
            } catch (DaoException ex) {
                return buildErrorResponse(ex);
            } catch (AggregationException ex) {
                return buildErrorResponse(ex);
            } catch (ProcessorNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (FilterException ex) {
                return buildErrorResponse(ex);
            } catch (IdentificationSchemeNotLoadedException ex) {
                return buildErrorResponse(ex);
            } catch (OwnerMissingException ex) {
                return buildErrorResponse(ex);
            }
            if (assessment.size() == 1 && !options.containsKey("processor")) {
                RESULTSLOGGER.info(assessment.iterator().next());
                return buildResponse(assessment.iterator().next());
            } else {
                return Response.ok(assembleFromList(assessment)).build();
            }
        }
    }

    @GET
    @Path("/{ruleSet}/id/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCase(@PathParam("ruleSet") String ruleSets, @PathParam("id") String id, @QueryParam("account") String owner, @QueryParam("transformer") String transformer, @QueryParam("action") String action) {
        HashMap<String, String> options = new HashMap<String, String>();
        owner = getOwner(owner, options);
        if (owner == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        options.put("owner", owner);
        options.put("transformer", transformer);
        ArrayList<String> cases = new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            if (action != null && action.equals("delete")) {
                for (String ruleSet : ruleSetNames) {
                    oddball.deleteCaseById(ruleSet, id, options);
                }
            } else {
                for (String ruleSet : ruleSetNames) {
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
//        StringBuilder out = new StringBuilder("[ ");
//        for (String aCase : cases) {
//            out.append(aCase);
//            out.append(", ");
//        }
//        if (out.length() > 2) {
//            out.delete(out.length() - 2, out.length());
//        }
//        out.append("]");
        return Response.ok(assembleFromList(cases)).build();
    }

    @GET
    @Path("/{ownerDir}/{ruleSet}/id/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findOwnerCase(@PathParam("ownerDir") String ownerDir, @PathParam("ruleSet") String ruleSets, @PathParam("id") String id, @QueryParam("account") String owner, @QueryParam("transformer") String transformer, @QueryParam("action") String action) {
        HashMap<String, String> options = new HashMap<String, String>();
        owner = getOwner(owner, options);
        if (owner == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        options.put("owner", owner);
        options.put("transformer", transformer);
        options.put("ownerDir", ownerDir);
        ArrayList<String> cases = new ArrayList<String>();
        try {
            String[] ruleSetNames = ruleSets.split(",");
            if (action != null && action.equals("delete")) {
                for (String ruleSet : ruleSetNames) {
                    oddball.deleteCaseById(ownerDir + "/" + ruleSet, id, options);
                }
            } else {
                for (String ruleSet : ruleSetNames) {
                    cases.addAll(oddball.findCaseById(ownerDir + "/" + ruleSet, id, options));
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
        for (String aCase : cases) {
            out.append(aCase);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{ruleSet}/case/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCases(@PathParam("ruleSet") String ruleSet, @Context UriInfo ui) throws UnsupportedEncodingException {
        HashMap<String, String> options = decodeOptions(ui);
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        ruleSet = ruleSet.replace(" ", "+");
        String ownerPrefix = "";
        if (ruleSet.contains("/")) {
            ownerPrefix = ruleSet.substring(0, ruleSet.indexOf("/") + 1);
            ruleSet = ruleSet.substring(ruleSet.indexOf("/") + 1);
            for (String key : options.keySet()) {
                options.put(key, options.get(key).replace("{owner}", ownerPrefix).replace("{account}", ownerPrefix));
            }
            options.put("ownerDir", ownerPrefix.substring(0, ownerPrefix.length() - 1));
        }
        return findCasesService(ruleSet, options, ownerPrefix);
    }

    @GET
    @Path("/{ownerDir}/{ruleSet}/case/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCases2(@PathParam("ruleSet") String ruleSet, @PathParam("ownerDir") String ownerDir, @Context UriInfo ui) throws UnsupportedEncodingException {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, java.net.URLDecoder.decode(queryParams.getFirst(key), "UTF-8"));
//            options.put(key, queryParams.getFirst(key).replaceAll("\\+", " "));
        }
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        for (String key : options.keySet()) {
            options.put(key, options.get(key).replace("{owner}", ownerDir + "/").replace("{account}", ownerDir + "/"));
        }
        options.put("ownerDir", ownerDir);
        return findCasesService(ruleSet, options, ownerDir + "/");
    }

    public Response findCasesService(String ruleSets, HashMap<String, String> options, String ruleOwnerPrefix) {
        String owner = getOwner(null, options);
        if (owner == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        options.put("owner", owner);
        String query = options.get("query");
        ArrayList<String> cases = new ArrayList<String>();
        String output = "";
        try {
            String[] ruleSetNames = ruleSets.split(",");
            for (String ruleSet : ruleSetNames) {
                if (options.get("action") != null && options.get("action").equals("delete")) {
                    oddball.deleteQueryCases(ruleOwnerPrefix + ruleSet.trim(), query, options);
                } else {
                    if (options.get("forEach") != null) {
                        cases.addAll(oddball.findQueryCasesForEach(ruleOwnerPrefix + ruleSet.trim(), query, options));
                    } else {
                        cases.addAll(oddball.findQueryCases(ruleOwnerPrefix + ruleSet.trim(), query, options));
                    }
                }
            }
            String format = "json";
            if (options.get("format") != null) {
                format = options.get("format");
            }
            if (format.equals("json")) {
                output = new JSONUtil().jsonWrap(cases);
            } else {
                if (format.equals("csv")) {
                    output = new JSONUtil().json2csv(cases);
                } else {
                    throw new BadFormatException(format);
                }
            }
        } catch (InvalidTimePeriodException ex) {
            return buildErrorResponse(ex);
        } catch (ParseException ex) {
            return buildErrorResponse(ex);
        } catch (JsonParseException ex) {
            return buildErrorResponse(ex);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (InvalidCaseException ex) {
            return buildErrorResponse(ex);
        } catch (TransformerNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (ProcessorNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IdentificationSchemeNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (AggregationException ex) {
            return buildErrorResponse(ex);
        } catch (ComparisonException ex) {
            return buildErrorResponse(ex);
        } catch (UnknownBinException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
            return buildErrorResponse(ex);
        } catch (DaoException ex) {
            return buildErrorResponse(ex);
        } catch (FilterException ex) {
            return buildErrorResponse(ex);
        } catch (BadFormatException ex) {
            return buildErrorResponse(ex);
        } catch (OwnerMissingException ex) {
            return buildErrorResponse(ex);
        }

        return Response.ok(output).build();

//        StringBuilder out = new StringBuilder("[ ");
//        for (String aCase : cases) {
//            out.append(aCase);
//            out.append(", ");
//        }
//        if (out.length() > 2) {
//            out.delete(out.length() - 2, out.length());
//        }
//        out.append("]");
//        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{ruleSet}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForRuleSet(@PathParam("ruleSet") String ruleSet, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("selector", "latest");
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        return findCasesService(ruleSet, options, "");
    }

    /**
     * ********************
     */
    // Sessions and Series
    /**
     * ********************
     */
    @GET
    @Path("/{ruleSet}/sessionId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctSessionId(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        return findDistinctSeriesService(ruleSets, owner, "case.sessionId", ui);
    }

    @GET
    @Path("/{ruleSet}/series/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctSeries(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        return findDistinctSeriesService(ruleSets, owner, "case.series", ui);
    }

    public Response findDistinctSeriesService(String ruleSets, String owner, String seriesName, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("distinct", seriesName);
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSets == null || ruleSets.equals("null")) {
            ruleSets = options.get("ruleSet");
        }
        return findCasesService(ruleSets, options, "");

    }

    @GET
    @Path("/{ruleSet}/sessionId/{sessionId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        options.put("sessionId", sessionId);
        return findCasesService(ruleSet, options, "");
    }

    @GET
    @Path("/{ruleSet}/series/{series}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForSeries(@PathParam("series") String series, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        options.put("series", series);
        return findCasesService(ruleSet, options, "");
    }

    @GET
    @Path("/{ruleSet}/sessionId/{sessionId}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForSession(@PathParam("ruleSet") String ruleSets, @PathParam("sessionId") String series, @Context UriInfo ui) {
        return findLatestCaseForSeriesService(ruleSets, series, ui, "sessionId");
    }

    @GET
    @Path("/{ruleSet}/series/{series}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForSeries(@PathParam("ruleSet") String ruleSets, @PathParam("series") String series, @Context UriInfo ui) {
        return findLatestCaseForSeriesService(ruleSets, series, ui, "series");
    }

    public Response findLatestCaseForSeriesService(String ruleSets, String series, UriInfo ui, String seriesName) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("selector", "latest");
        options.put(seriesName, series);
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSets == null || ruleSets.equals("null")) {
            ruleSets = options.get("ruleSets");
        }
        if (series == null || series.equals("null")) {
            series = options.get("series");
        }
        if (series == null || series.equals("null")) {
            series = options.get("sessionId");
        }
        return findCasesService(ruleSets, options, "");

    }

    /**
     * ********************
     */
    // Users and Agents
    /**
     * ********************
     */
    @GET
    @Path("/{ruleSet}/userId/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctUserId(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        return findDistinctAgentService(ruleSets, owner, "case.userId", ui);
    }

    @GET
    @Path("/{ruleSet}/agent/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctAgent(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        return findDistinctAgentService(ruleSets, owner, "case.agent", ui);
    }

    public Response findDistinctAgentService(String ruleSets, String owner, String agentName, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("distinct", agentName);
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSets == null || ruleSets.equals("null")) {
            ruleSets = options.get("ruleSet");
        }
        return findCasesService(ruleSets, options, "");
    }

    @GET
    @Path("/{ruleSet}/userId/{userId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        options.put("userId", userId);
        return findCasesService(ruleSets, options, "");
    }

    @GET
    @Path("/{ruleSet}/agent/{agent}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForAgent(@PathParam("agent") String agent, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        options.put("agent", agent);
        return findCasesService(ruleSets, options, "");
    }

    @GET
    @Path("/{ruleSet}/userId/{userId}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForUser(@PathParam("ruleSet") String ruleSets, @PathParam("userId") String agent, @Context UriInfo ui) {
        return findLatestCaseForAgentService(ruleSets, agent, ui, "userId");
    }

    @GET
    @Path("/{ruleSet}/agent/{agent}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForAgent(@PathParam("ruleSet") String ruleSets, @PathParam("agent") String agent, @Context UriInfo ui) {
        return findLatestCaseForAgentService(ruleSets, agent, ui, "agent");
    }

    public Response findLatestCaseForAgentService(String ruleSets, String agent, UriInfo ui, String agentName) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("selector", "latest");
        options.put(agentName, agent);
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSets == null || ruleSets.equals("null")) {
            ruleSets = options.get("ruleSets");
        }
        if (agent == null || agent.equals("null")) {
            agent = options.get("agent");
        }
        if (agent == null || agent.equals("null")) {
            agent = options.get("userId");
        }
        return findCasesService(ruleSets, options, "");
    }

    /**
     * ********************
     */
    // Arbitrary Queries
    /**
     * ********************
     */
    @GET
    @Path("/{ruleSet}/query/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForQuery(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSet, @QueryParam("ruleSet") String altRuleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        return findCasesService(ruleSet, options, "");
    }

    @GET
    @Path("/{ruleSet}/query/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestQueryCase(@PathParam("ruleSet") String ruleSet, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        options.put("selector", "latest");
        if (ruleSet == null || ruleSet.equals("null")) {
            ruleSet = options.get("ruleSet");
        }
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        return findCasesService(ruleSet, options, "");
    }

    /**
     * ********************
     */
    // Bins
    /**
     * ********************
     */
    @GET
    @Path("/{ruleSet}/bin/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctBins(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner) {
//        HashMap<String, String> options = decodeOptions(ui);
        owner = getOwner(owner, new HashMap<String, String>());
        if (owner == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Iterable<String> binLabels;
        try {
            binLabels = oddball.listBinLabels(owner);
        } catch (BinSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String binLabel : binLabels) {
            out.append("\"" + binLabel + "\"");
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{ruleSet}/bin/{binLabel}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findCasesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        options.put("binLabel", binLabel);
        return findCasesService(ruleSets, options, "");
    }

    @GET
    @Path("/{ruleSet}/bin/{binLabel}/distinct")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDistinctPropertiesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @Context UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        HashMap<String, String> options = new HashMap<String, String>();
        for (String key : queryParams.keySet()) {
            options.put(key, queryParams.getFirst(key).replace("+", " "));
        }
        options.put("binLabel", binLabel);
        if (options.get("property") != null) {
            options.put("distinct", options.get("property"));
        }
        return findCasesService(ruleSets, options, "");

    }

    /**
     * ********************
     */
    // RuleSet Management
    /**
     * ********************
     */
    @GET
    @Path("/{ruleSet}/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner) {
        if (owner != null) {
            ruleSet = ruleSet.replaceAll("\\{owner\\}", owner + "/").replaceAll("\\{account\\}", owner + "/");
        }
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearRuleSet(ruleSet);
        return Response.ok("Rule Set " + ruleSet + " cleared.").build();
    }

    @GET
    @Path("/{owner}/{ruleSet}/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearRuleSet2(@PathParam("ruleSet") String ruleSet, @PathParam("owner") String owner) {
        if (owner != null) {
            ruleSet = owner + "/" + ruleSet;
        }
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearRuleSet(ruleSet);
        return Response.ok("Rule Set " + ruleSet + " cleared.").build();
    }

    @GET
    @Path("/{ruleSet}/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reloadRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner) {
        if (owner != null) {
            ruleSet = ruleSet.replaceAll("\\{owner\\}", owner + "/").replaceAll("\\{account\\}", owner + "/");
        }
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            oddball.reloadRuleSet(ruleSet);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok("Rule Set " + ruleSet + " reloaded.").build();
    }

    @GET
    @Path("/{owner}/{ruleSet}/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reloadRuleSet2(@PathParam("ruleSet") String ruleSet, @PathParam("owner") String owner) {
        if (owner != null) {
            ruleSet = owner + "/" + ruleSet;
        }
        if (!authorisationHandler.isAdministrator() && !authorisationHandler.isAccountOwner(owner)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            oddball.reloadRuleSet(ruleSet);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok("Rule Set " + ruleSet + " reloaded.").build();
    }

    @GET
    @Path("/{ruleSet}/bin/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reloadBinSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr) {
        if (!authorisationHandler.isAdministrator()) {
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
    @Path("/{ruleSet}/rule/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRules(@PathParam("ruleSet") String ruleSet, @QueryParam("transformer") String transformer, @QueryParam("prefix") String prefix, @QueryParam("source") String source) {
        if (!authorisationHandler.isAdministrator()) {
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
        for (String rule : rules) {
            out.append(rule);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{ruleSet}/rules/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRuleSet(@PathParam("ruleSet") String ruleSet) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String rules;
        HashMap<String, String> options = new HashMap<String, String>();
        try {
            rules = oddball.showRules(ruleSet, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
            return buildErrorResponse(ex);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        }
        return Response.ok(rules).build();
    }

    @GET
    @Path("/{ruleSet}/rule/{label}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRules(@PathParam("ruleSet") String ruleSet, @PathParam("label") String label, @QueryParam("transformer") String transformer) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Iterable<String> rules;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", transformer);
        options.put("label", label);
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
        for (String rule : rules) {
            out.append(rule);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{owner}/{ruleSet}/rule/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRules2(@PathParam("ruleSet") String ruleSet, @PathParam("owner") String owner, @QueryParam("transformer") String transformer, @QueryParam("prefix") String prefix, @QueryParam("source") String source) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (owner != null) {
            ruleSet = owner + "/" + ruleSet;
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
        for (String rule : rules) {
            out.append(rule);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{owner}/{ruleSet}/rules/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showOwnerRuleSet(@PathParam("owner") String owner, @PathParam("ruleSet") String ruleSet) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String rules;
        HashMap<String, String> options = new HashMap<String, String>();
        try {
            rules = oddball.showRules(owner + "/" + ruleSet, options);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        } catch (IOException ex) {
            return buildErrorResponse(ex);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        }
        return Response.ok(rules).build();
    }

    @GET
    @Path("/{owner}/{ruleSet}/rule/{label}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRule(@PathParam("ruleSet") String ruleSet, @PathParam("owner") String owner, @PathParam("label") String label, @QueryParam("transformer") String transformer) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (owner != null) {
            ruleSet = owner + "/" + ruleSet;
        }
        Iterable<String> rules;
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", transformer);
        options.put("label", label);
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
        for (String rule : rules) {
            out.append(rule);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/{ruleSet}/rule/save")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveRules(@PathParam("ruleSet") String ruleSet, @QueryParam("transformer") String transformer, @QueryParam("prefix") String prefix, @QueryParam("source") String source) {
        if (!authorisationHandler.isAdministrator()) {
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
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String rule : rules) {
            out.append(rule);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @POST
    @Path("/{ruleSet}/rule/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertRule(@PathParam("ruleSet") String ruleSet, @QueryParam("label") String label, @QueryParam("prefix") String prefix, @QueryParam("rule") String ruleString) {
        if (!authorisationHandler.isAdministrator()) {
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
        }
        StringBuilder out = new StringBuilder("[ ");
        for (String rule : rules) {
            out.append(rule);
            out.append(", ");
        }
        if (out.length() > 2) {
            out.delete(out.length() - 2, out.length());
        }
        out.append("]");
        return Response.ok(out.toString()).build();
    }

    @GET
    @Path("/resources/{resourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showConfigList(@PathParam("resourceName") String resourceName) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        List<String> resources = new ArrayList<String>();
        try {
            resources = oddball.showResourceList(resourceName);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        } catch (ResourceNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(resources.toString()).build();
    }

    @GET
    @Path("/resource/{resourceName}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showConfig(@PathParam("resourceName") String resourceName) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        String doc = "";
        try {
            doc = oddball.showResource(resourceName);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        } catch (ResourceNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(doc.toString()).build();
    }

    @POST
    @Path("/resource/{resourceName}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadConfig(@PathParam("resourceName") String resourceName, @QueryParam("resource") String resourceString) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        try {
            oddball.uploadResource(resourceName, resourceString);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        } catch (ResourceNotUploadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok("Resource loaded").build();
    }

    @GET
    @Path("/{owner}/resource/{resourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showConfig(@PathParam("owner") String owner, @PathParam("resourceName") String resourceName) {
        if (!authorisationHandler.isAdministrator() && !authorisationHandler.isAccountOwner(owner)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        String doc = "";
        try {
            doc = oddball.showResource(owner + "/" + resourceName);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        } catch (ResourceNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(doc.toString()).build();
    }

    @GET
    @Path("/{owner}/resources/{resourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showConfigList(@PathParam("owner") String owner, @PathParam("resourceName") String resourceName) {
        if (!authorisationHandler.isAdministrator() && !authorisationHandler.isAccountOwner(owner)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        List<String> resources = new ArrayList<String>();
        try {
            resources = oddball.showResourceList(owner + "/" + resourceName);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        } catch (ResourceNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(resources.toString()).build();
    }

    @POST
    @Path("/{owner}/resource/{resourceName}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadOwnerConfig(@PathParam("owner") String owner, @PathParam("resourceName") String resourceName, @QueryParam("resource") String resourceString) {
        if (!authorisationHandler.isAdministrator() && !authorisationHandler.isAccountOwner(owner)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        try {
            oddball.uploadResource(owner + "/" + resourceName, resourceString);
//        } catch (TransformerNotLoadedException ex) {
//            return buildErrorResponse(ex);
        } catch (ResourceNotUploadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok("Resource loaded").build();
    }

    /*
     **********************
     * Cache Management
     **********************
     */
    @GET
    @Path("/transformer/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearTransformers() {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearTransformers();
        return Response.ok("Transformers cleared.").build();
    }

    @GET
    @Path("/processor/clear")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearProcessors() {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearProcessors();
        return Response.ok("Processors cleared.").build();
    }

    static final Logger RESULTSLOGGER = LoggerFactory.getLogger("oddball-results");
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

    /*
     **********************
     * DB Stats
     **********************
     */
    @GET
    @Path("/{ruleSet}/dbStats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showRuleSetStats(@PathParam("ruleSet") String ruleSet) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String stats = "";
        try {
            stats = oddball.getDbStats(ruleSet);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(stats).build();
    }

    @GET
    @Path("/{owner}/{ruleSet}/dbStats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showOwnerRuleSetStats(@PathParam("ruleSet") String ruleSet, @PathParam("owner") String owner) {
//        HashMap<String, String> options = decodeOptions(ui);
        owner = getOwner(owner, new HashMap<String, String>());
        if (owner == null && !authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String stats = "";
        try {
            stats = oddball.getDbStats(owner + "/" + ruleSet);
        } catch (RuleSetNotLoadedException ex) {
            return buildErrorResponse(ex);
        }
        return Response.ok(stats).build();
    }

    @GET
    @Path("/dataSources/{resourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showDataBaseList(@PathParam("resourceName") String resourceName) {
        if (!authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        List<String> resources = new ArrayList<String>();
        resources = oddball.showDatabaseList(resourceName + "-persist");
        return Response.ok(resources.toString()).build();
    }

    @GET
    @Path("/{owner}/dataSources/{resourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showOwnerDataBaseList(@PathParam("owner") String owner, @PathParam("resourceName") String resourceName) {
//        HashMap<String, String> options = decodeOptions(ui);
        owner = getOwner(owner, new HashMap<String, String>());
        if (owner == null && !authorisationHandler.isAdministrator()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        List<String> resources = new ArrayList<String>();
        resources = oddball.showDatabaseList(owner + "-" + resourceName + "-persist");
        return Response.ok(resources.toString()).build();
    }

}
