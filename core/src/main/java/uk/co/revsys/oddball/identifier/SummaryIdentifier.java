/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.aggregator.*;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class SummaryIdentifier implements CaseIdentifier{
    
    
    @Override
    public Map<String, Object> identify(RuleSet ruleSet, String caseString, Iterable<String> comparisonCases, Map<String, String> options, Oddball ob, ResourceRepository resourceRepository) throws ComparisonException, IOException, IdentificationSchemeNotLoadedException, UnknownBinException, IOException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException, AggregationException{
        SummaryAggregator sa = new SummaryAggregator();
        IdentificationScheme ids = new IdentificationScheme(options.get("identificationScheme"), resourceRepository);
        String identificationPeriod = options.get("identificationPeriod");
        Map<String, Object> identification = new HashMap<String, Object>();
        try {
            List<Summary> comparisons = sa.summariseCases(comparisonCases, options, resourceRepository);
            if (comparisons.size()==0){
                throw new InvalidTimePeriodException("No valid time periods for Summary Identifier");
            }
            Summary comparison = comparisons.get(0); // should be just the 
            Map<String, Object> comparisonMap = comparison.asMap();
            Double totalInfo = 0.0;
            for (String key : comparisonMap.keySet()){
                try {
                    Map<String, Object> distribution = (Map<String, Object>) comparisonMap.get(key);
                    Double info = (Double) distribution.get("information");
                    totalInfo += info;
                }
                catch (ClassCastException e){
                    // ignore this key
                }
                catch (NullPointerException e){
                    // ignore this key
                }
            }
            Map<String, Object> caseMap = JSONUtil.json2map(caseString);
            Map<String, Map<String, Object>> assessment = comparison.assess(caseMap);
            Map<String, Map<String, Object>> outcomes = new HashMap<String, Map<String, Object>>();
            Map<String, Object> primaryOutcome = null;
            Map<String, Object> combinedOutcome = new HashMap<String, Object>();
            for (Map<String, Object> indicator : (ArrayList<Map<String, Object>>) ids.getScheme()){
                Map<String, Object> outcome = tryIndicator(ruleSet, sa, caseMap, indicator, identificationPeriod, assessment, options, ob, resourceRepository);
                if (outcome!=null){
                    if (outcome.get("rank").equals("primary")){
                        primaryOutcome = outcome;
                    }
                    outcomes.put((String)indicator.get("name"),outcome);
                }
            }
            if (primaryOutcome !=null){
                primaryOutcome.put("relInfoReduction", 1);
                ArrayList<String> combiningQueries = new ArrayList<String> ();
                for (Map<String, Object> outcome : (Collection<Map<String, Object>>) outcomes.values()){
                    if (!outcome.get("rank").equals("primary")){
                        outcome.put("relInfoReduction", ((Double)outcome.get("infoReduction"))/((Double)primaryOutcome.get("infoReduction")));
                        String comparePrimary = "";
                        if ((Integer)outcome.get("count")==1 && (Integer)primaryOutcome.get("count")==1){
                            comparePrimary = "SingleEQPrimary";
                            combiningQueries.add((String)outcome.get("query"));                        
                        }
                        if ((Integer)outcome.get("count")==1 && (Integer)primaryOutcome.get("count")>1){
                            comparePrimary = "SingleLTPrimary";
                            combiningQueries.add((String)outcome.get("query"));                        
                        }
                        if ((Integer)outcome.get("count")>1 && (Integer)primaryOutcome.get("count")==1){
                            comparePrimary = "MultipleGTSinglePrimary";
                            if ((Double) outcome.get("power")> 0.6){
                                combiningQueries.add((String)outcome.get("query"));                        
                            }
                        }
                        if ((Integer)outcome.get("count")>1 && (Integer)primaryOutcome.get("count")>1){
                            if ((Integer)outcome.get("count") == (Integer)primaryOutcome.get("count")){
                                comparePrimary = "MultipleEQPrimary";
                                combiningQueries.add((String)outcome.get("query"));                        
                            }
                            if ((Integer)outcome.get("count")> (Integer)primaryOutcome.get("count")){
                                comparePrimary = "MultipleGTPrimary";
                                if ((Double) outcome.get("power")> 0.6){
                                    combiningQueries.add((String)outcome.get("query"));                        
                                }
                            }
                            if ((Integer)outcome.get("count")< (Integer)primaryOutcome.get("count")){
                                comparePrimary = "MultipleLTPrimary";
                                if ((Double) outcome.get("power")> 0.6){
                                    combiningQueries.add((String)outcome.get("query"));                        
                                }
                            }
                        }
                        outcome.put("compareToPrimary",comparePrimary);
                    } else {
                        outcome.put("compareToPrimary","isPrimary");
                    }
                }
                StringBuilder combinedQuerySB = new StringBuilder("{\"$or\":[");
                for (String query : combiningQueries){
                    combinedQuerySB.append(query);
                    combinedQuerySB.append(",");
                }
                combinedQuerySB.append((String)primaryOutcome.get("query"));
                combinedQuerySB.append("]}");
                combinedOutcome.put("query", combinedQuerySB.toString());
                outcomes.put("combined", combinedOutcome);

            }            
//            identification.put("identification", outcomes);
            if (options.containsKey("identificationLabel")){
                caseMap.put(options.get("identificationLabel"), outcomes);
            } else {
                caseMap.put("identification", outcomes);
            }
//            return identification;
            return caseMap;
        }
        catch (AggregationException ex){
            throw new ComparisonException(ex.getMessage());
        }
//        String summaryDefinitionName = options.get("summaryDefinition");
//        try {
//            this.summaryDefinition = new SummaryDefinition(summaryDefinitionName, resourceRepository);
//        }
//        catch (SummaryDefinitionNotLoadedException e){
//            throw new AggregationException("Summary Definition could not be loaded", e);
//        }
        
        
    }

    private Map<String, Object> tryIndicator(RuleSet ruleSet, SummaryAggregator sa, Map<String, Object> caseMap, Map<String, Object> indicator, String identificationPeriod, Map<String, Map<String, Object>> assessment, Map<String, String> options,  Oddball ob, ResourceRepository resourceRepository) throws UnknownBinException, IOException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException, AggregationException{
        int candidateCount = assessment.size();
//        LOGGER.debug("Trying identification:"+indicator.toString());
        ArrayList<String> includeFields = (ArrayList<String>) indicator.get("include-fields");
        ArrayList<String> queryFields = (ArrayList<String>) indicator.get("query-fields");
        int totalPowers = 0;
        //int totalInfo = 0;
        double relInfo = 0;
        ArrayList<String> nonUnique = new ArrayList();
        ArrayList<String> prevNonUnique = new ArrayList();
        String applicableField = "";
        String queryField = "";

        Double totalInfo1 = 0.0;
        int infoFactors1 = 0;
        for (String field : assessment.keySet()){
            try {
                Map<String, Object> fieldAssessment = assessment.get(field);
                if (fieldAssessment.containsKey("proportion")){
                    if ((Float)fieldAssessment.get("proportion")<1.0){
                        nonUnique.add(field);
                    } else {
                        totalPowers++;
                    }
                    
                }
                if ((includeFields.contains(field)) && ((Double)fieldAssessment.get("info")>0.0)){
                    relInfo = (Double)fieldAssessment.get("relInfo");
                    applicableField = field;
                    queryField = queryFields.get(0);
                }
                if (fieldAssessment.containsKey("info")){
                    totalInfo1+= (Double)fieldAssessment.get("info");
                    infoFactors1++;
                }
            }
            catch (NullPointerException e){
                LOGGER.warn("Identity indicator failed:"+indicator.get("name"), e);
            }
                    
                    
        }
        if (!applicableField.equals("")){
            String query = "{\""+queryField+"\":\""+assessment.get(applicableField).get("value")+"\"}";

            //options
            Map<String, String> subOptions = new HashMap<String, String>();
            for (String key : options.keySet()){
                subOptions.put(key, options.get(key));
            }
            subOptions.put("query", query);
            subOptions.put("caseRecent", identificationPeriod);
            if (subOptions.get("identityTransformer")!=null){
                subOptions.put("transformer", (String) subOptions.get("identityTransformer"));
            }

            Collection<String> comparisonCases = ob.comparisonResults(ruleSet, query, subOptions);
            ArrayList<Summary> comparisons=sa.summariseCases(comparisonCases, subOptions, resourceRepository);


            if (comparisons.size()==0){
                LOGGER.warn("Identity indicator failed:"+indicator.get("name"));
                return null;
            } else {
                Summary comparison = comparisons.get(0); // should be just the 1
                assessment = comparison.assess(caseMap);
        //        LOGGER.debug("Extended ID");
        //        LOGGER.debug((String)indicator.get("name"));
        //        LOGGER.debug(comparison.asMap().toString());
                for (String field : nonUnique){
                    prevNonUnique.add(field);
                }

                nonUnique.clear();
                int maxCount=0;

                Double totalInfo2 = 0.0;
                int infoFactors2 = 0;
                for (String field : assessment.keySet()){
                    try {
                        Map<String, Object> fieldAssessment = assessment.get(field);

                        if (fieldAssessment.containsKey("proportion") && (Float)fieldAssessment.get("proportion")<1.0){
                            nonUnique.add(field);
                        }
                        if ((includeFields.contains(field)) && ((Integer)fieldAssessment.get("count")>maxCount)){
                            maxCount = (Integer)fieldAssessment.get("count");
                        }
                        if (fieldAssessment.containsKey("info")){
                            totalInfo2+= (Double)fieldAssessment.get("info");
                            infoFactors2++;
                        }
                    }
                    catch (NullPointerException e){
                        LOGGER.warn("Identity indicator failed:"+field, e);
                    }
                }

                for (String field2 : nonUnique){
                    prevNonUnique.remove(field2);
                }
        //        System.out.println(prevNonUnique);
                totalPowers+=prevNonUnique.size();
        //        System.out.println("Total Powers");
        //        System.out.println(totalPowers);

                HashMap<String, Object> outcome = new HashMap<String, Object> ();
                outcome.put("indicator", indicator.get("name"));
                outcome.put("count", maxCount);

                outcome.put("query", query);
                outcome.put("power", 1.0*totalPowers/candidateCount);
                outcome.put("infoReduction", (totalInfo1/infoFactors1)-(totalInfo2/infoFactors2));
                outcome.put("strength", relInfo);
                outcome.put("rank", indicator.get("rank"));
                outcome.put("support", comparison.asMap());
                return outcome;
            }
        } else {
                LOGGER.warn("Identity indicator failed:"+indicator.get("name"));
                return null;
        }
    }
    
    private SummaryDefinition summaryDefinition;     

    private IdentificationScheme identificationScheme;     


    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
