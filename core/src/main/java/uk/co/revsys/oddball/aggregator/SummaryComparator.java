/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.oddball.util.OddUtil;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class SummaryComparator implements CaseComparator{
    
    
    public Map compare(String caseString, Iterable<String> comparisonCases, Map<String, String> options, ResourceRepository resourceRepository) throws ComparisonException, InvalidTimePeriodException, IOException{
        SummaryAggregator sa = new SummaryAggregator();
        try {
//            LOGGER.debug("compareToSummary");
//            LOGGER.debug(caseString);
            Collection<Summary> comparisons = sa.summariseCases(comparisonCases, options, resourceRepository);
//            LOGGER.debug(Integer.toString(comparisons.size()));
//            LOGGER.debug(comparisons.toString());
            Summary comparison = sa.summariseCases(comparisonCases, options, resourceRepository).get(0); // should be just the 1
//            LOGGER.debug(comparison.asMap().toString());
            Map<String, Object> caseMap = JSONUtil.json2map(caseString);
            caseMap.put("comparison", comparison.assess(caseMap));
//            return comparison.assess(caseMap);
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

    private SummaryDefinition summaryDefinition;     


    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
