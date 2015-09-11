/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.util.Map;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public interface CaseComparator {

    Map compare(String caseString, Iterable<String> comparisonStrings, Map<String, String> options, ResourceRepository resourceRepository) throws ComparisonException, InvalidTimePeriodException, IOException;
    
}
