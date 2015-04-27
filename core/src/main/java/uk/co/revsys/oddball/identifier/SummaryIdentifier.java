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
    public Map<String, Object> identify(RuleSet ruleSet, String caseString, Iterable<String> comparisonCases, Map<String, String> options, Oddball ob, ResourceRepository resourceRepository) throws ComparisonException, InvalidTimePeriodException, IOException, IdentificationSchemeNotLoadedException, UnknownBinException, IOException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException, AggregationException{
        SummaryAggregator sa = new SummaryAggregator();
        IdentificationScheme ids = new IdentificationScheme(options.get("identificationScheme"), resourceRepository);
        String identificationPeriod = options.get("identificationPeriod");
        Map<String, Object> identification = new HashMap<String, Object>();
        try {
//            Collection<Summary> comparisons = sa.summariseCases(comparisonCases, options, resourceRepository);
            Summary comparison = sa.summariseCases(comparisonCases, options, resourceRepository).get(0); // should be just the 1
            Map<String, Object> caseMap = JSONUtil.json2map(caseString);
            Map<String, Map<String, Object>> assessment = comparison.assess(caseMap);
            Map<String, Map<String, Object>> outcomes = new HashMap<String, Map<String, Object>>();
            Map<String, Object> primaryOutcome = null;
            for (Map<String, Object> indicator : (ArrayList<Map<String, Object>>) ids.getScheme()){
                Map<String, Object> outcome = tryIndicator(ruleSet, sa, caseMap, indicator, identificationPeriod, assessment, options, ob, resourceRepository);
                if (outcome.get("rank").equals("primary")){
                    primaryOutcome = outcome;
                }
                outcomes.put((String)indicator.get("name"),outcome);
            }
            if (primaryOutcome !=null){
                for (Map<String, Object> outcome : (Collection<Map<String, Object>>) outcomes.values()){
                    if (!outcome.get("rank").equals("primary")){
                        String comparePrimary = "";
                        if ((Integer)outcome.get("count")==1 && (Integer)primaryOutcome.get("count")==1){
                            comparePrimary = "SingleEQPrimary";
                        }
                        if ((Integer)outcome.get("count")==1 && (Integer)primaryOutcome.get("count")>1){
                            comparePrimary = "SingleLTPrimary";
                        }
                        if ((Integer)outcome.get("count")>1 && (Integer)primaryOutcome.get("count")==1){
                            comparePrimary = "MultipleGTSinglePrimary";
                        }
                        if ((Integer)outcome.get("count")>1 && (Integer)primaryOutcome.get("count")>1){
                            if ((Integer)outcome.get("count") == (Integer)primaryOutcome.get("count")){
                                comparePrimary = "MultipleEQPrimary";
                            }
                            if ((Integer)outcome.get("count")> (Integer)primaryOutcome.get("count")){
                                comparePrimary = "MultipleGTPrimary";
                            }
                            if ((Integer)outcome.get("count")< (Integer)primaryOutcome.get("count")){
                                comparePrimary = "MultipleLTPrimary";
                            }
                        }
                        outcome.put("compareToPrimary",comparePrimary);
                    } else {
                        outcome.put("compareToPrimary","isPrimary");
                    }
                }
            }            
//            identification.put("identification", outcomes);
            caseMap.put("identification", outcomes);
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
        ArrayList<String> includeFields = (ArrayList<String>) indicator.get("include-fields");
        ArrayList<String> queryFields = (ArrayList<String>) indicator.get("query-fields");
        int totalPowers = 0;
        double relInfo = 0;
        ArrayList<String> nonUnique = new ArrayList();
        ArrayList<String> prevNonUnique = new ArrayList();
        String applicableField = "";
        String queryField = "";

//        System.out.println("Assessment");
//        System.out.println(assessment);

        for (String field : assessment.keySet()){
            Map<String, Object> fieldAssessment = assessment.get(field);
//            System.out.println("field:"+field);
//            System.out.println(fieldAssessment.get("proportion"));
            if ((Float)fieldAssessment.get("proportion")<1.0){
                nonUnique.add(field);
            } else {
                totalPowers++;
            }
            if ((includeFields.contains(field)) && ((Double)fieldAssessment.get("info")>0.0)){
                relInfo = (Double)fieldAssessment.get("relInfo");
                applicableField = field;
                queryField = queryFields.get(0);
            }
        }
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
        
        for (String field : assessment.keySet()){
            Map<String, Object> fieldAssessment = assessment.get(field);
            if ((Float)fieldAssessment.get("proportion")<1.0){
                nonUnique.add(field);
            }
            if ((includeFields.contains(field)) && ((Integer)fieldAssessment.get("count")>maxCount)){
                maxCount = (Integer)fieldAssessment.get("count");
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
        outcome.put("strength", relInfo);
        outcome.put("rank", indicator.get("rank"));
        outcome.put("support", comparison.asMap());
        return outcome;
    }
    
    private SummaryDefinition summaryDefinition;     

    private IdentificationScheme identificationScheme;     


    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
