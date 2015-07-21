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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.RuleSet;
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
//            LOGGER.debug(options.toString());
            List<Summary> comparisons = sa.summariseCases(comparisonCases, options, resourceRepository);
//            LOGGER.debug(Integer.toString(comparisons.size()));
            if (comparisons.size()<1){
                throw new ComparisonException("No cases for comparison");
            }
            Summary comparison = comparisons.get(0); // should be just the 1
            String comparisonCase = comparisonCases.iterator().next();
//            LOGGER.debug(comparison.asMap().toString());
            Map<String, Object> caseMap = JSONUtil.json2map(caseString);
//            LOGGER.debug(comparison.assess(caseMap).toString());
            caseMap.put("comparison", comparison.assess(caseMap));
            return caseMap;
        }
        catch (AggregationException ex){
            throw new ComparisonException(ex.getMessage());
        }
        
    }

    
    
    private SummaryDefinition summaryDefinition;     


    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
