/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public interface Aggregator {

    public ArrayList<Map> aggregateCases(Iterable<String> caseStrings, Map<String, String> options, ResourceRepository resourceRepository) throws AggregationException, InvalidTimePeriodException;
    public ArrayList<Map> incrementAggregation(String itemString, Map<String, Object> aggregationMap, Map<String, String> options) throws IOException, ParseException ;
    
}
