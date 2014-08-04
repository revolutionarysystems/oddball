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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
<<<<<<< HEAD
    public Response insertRule(@PathParam("ruleSet") String ruleSet, @QueryParam("label") String label, @QueryParam("prefix") String prefix, @QueryParam("rule") String ruleString){
=======
    public Response addExtraRule(@PathParam("ruleSet") String ruleSet, @QueryParam("label") String label, @QueryParam("prefix") String prefix, @QueryParam("rule") String ruleString){
>>>>>>> fa6aab0df23924251eee565f77ab3d4cd2f7942a
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
    public Response findDistinctSessionId(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("transformer") String transformer){
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
                cases.addAll(oddball.findDistinct(ruleSet, "case.sessionId", recent, options));
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
    public Response findCasesForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
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
                cases.addAll(oddball.findQueryCases(ruleSet, "{ \"case.sessionId\" : \""+sessionId+"\" }", options));
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
    @Path("/{ruleSet}/sessionId/{sessionId}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForSession(@PathParam("sessionId") String sessionId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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
    public Response findDistinctUserId(@PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("recent") String recent, @QueryParam("transformer") String transformer){
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
                cases.addAll(oddball.findDistinct(ruleSet, "case.userId",recent, options));
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
    public Response findCasesForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
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
                cases.addAll(oddball.findQueryCases(ruleSet, "{ \"case.userId\" : \""+userId+"\" }", options));
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
    @Path("/{ruleSet}/userId/{userId}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findLatestCaseForUser(@PathParam("userId") String userId, @PathParam("ruleSet") String ruleSet, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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
    public Response findCasesForQuery(@QueryParam("query") String query, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
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
                cases.addAll(oddball.findQueryCases(ruleSet, query, options));
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
    public Response findCasesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("transformer") String transformer){
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
                cases.addAll(oddball.findCasesInBin(ruleSet, binLabel, options));
            }
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
    @Path("/{ruleSet}/bin/{binLabel}/distinct")
    @Produces(MediaType.APPLICATION_JSON)
<<<<<<< HEAD
    public Response findDistinctPropertiesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("property") String property, @QueryParam("recent") String recent){
=======
    public Response findDistinctPropertiesForBin(@PathParam("binLabel") String binLabel, @PathParam("ruleSet") String ruleSets, @QueryParam("account") String owner, @QueryParam("property") String property, @QueryParam("r") String recent){
>>>>>>> fa6aab0df23924251eee565f77ab3d4cd2f7942a
        owner = getOwner(owner);
        if(owner == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", owner);
        options.put("property", property);
        options.put("recent", recent);
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
<<<<<<< HEAD
    public Response clearRuleSet(@PathParam("ruleSet") String ruleSet){
=======
    public Response clearRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr){
>>>>>>> fa6aab0df23924251eee565f77ab3d4cd2f7942a
        if(!authorisationHandler.isAdministrator()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        oddball.clearRuleSet(ruleSet);
        return Response.ok("Rule Set "+ruleSet+" cleared.").build();
    }

    @GET
    @Path("/{ruleSet}/reload")
    @Produces(MediaType.TEXT_PLAIN)
<<<<<<< HEAD
    public Response reloadRuleSet(@PathParam("ruleSet") String ruleSet){
=======
    public Response reloadRuleSet(@PathParam("ruleSet") String ruleSet, @QueryParam("case") String caseStr){
>>>>>>> fa6aab0df23924251eee565f77ab3d4cd2f7942a
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
