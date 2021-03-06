/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.identifier;

import uk.co.revsys.oddball.aggregator.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.TransformerNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public interface CaseIdentifier {

        Map identify(RuleSet ruleSet, String caseString, Iterable<String> comparisonCases, Map<String, String> options, Oddball ob, ResourceRepository resourceRepository) throws ComparisonException, InvalidTimePeriodException, IOException, IdentificationSchemeNotLoadedException, UnknownBinException, IOException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException, AggregationException;
    
}
