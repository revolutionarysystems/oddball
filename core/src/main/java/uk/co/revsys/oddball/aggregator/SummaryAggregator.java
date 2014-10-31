/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class SummaryAggregator implements Aggregator{
    
    
    
    @Override
    public ArrayList<Map> aggregateCases(Iterable<String> caseStrings, Map<String, String> options, ResourceRepository resourceRepository) throws AggregationException{
        ArrayList<Map> response = new ArrayList<Map>();
        String summaryDefinitionName = options.get("summaryDefinition");
        try {
            this.summaryDefinition = new SummaryDefinition(summaryDefinitionName, resourceRepository);
        }
        catch (SummaryDefinitionNotLoadedException e){
            throw new AggregationException("Summary Definition could not be loaded", e);
        }
        summaries = new ArrayList<Summary>();
        // set up time window parameters
        String align = "now";
        if (options.get("align")!=null){
            align = options.get("align");
        }
        long reportEnd = new Date().getTime();
        if (options.get("periodEnd")!=null){
            reportEnd = Long.parseLong(options.get("periodEnd"));
        }
        long periodMin = 60; // default unit is minute
        if (options.get("periodDivision")!=null){
            periodMin = Long.parseLong(options.get("periodDivision"));
        }
        long periodms=periodMin * 60 * 1000;
        if (align.equals("clock")){
            reportEnd = periodms * Math.round(0.499999+reportEnd/(1.0*periodms));
        }
        long reportStart = reportEnd - periodms;
        if (options.get("periodStart")!=null){
            reportStart = Long.parseLong(options.get("periodStart"));
        }
        int periods = (int) Math.round(0.4999+(reportEnd-reportStart)/(1.0*periodms));  //round up number of periods to ensure coverage
        if (options.get("periods")!=null){
            periods = (int) Long.parseLong(options.get("periods"));
            if (options.get("periodStart")==null){
                reportStart = reportEnd - periods * periodms;
            }
        }
        if (options.get("recent")!=null){
            if (options.get("periodStart")==null && options.get("periods")==null){
                long duration = Long.parseLong(options.get("recent"))*60*1000;
                if (align.equals("clock")){ //ensure at least the recent period is covered, when aligned to clock, this means 1 extra period
                    duration+=periodms;
                }
                reportStart = reportEnd - duration;
            }
        }
        if (align.equals("clock")){
            reportStart = periodms * Math.round(-0.499999+reportStart/(1.0*periodms));
        }
        periods = (int) Math.round(0.4999+(reportEnd-reportStart)/(1.0*periodms));  //round up number of periods to ensure coverage
        // set up Summaries
        for (int i=periods-1 ; i>=0; i--){
            try {
                summaries.add(new Summary(options.get("owner"), reportStart+i*periodms, periodms, summaryDefinition));
            } catch (AccumulationException e){
                throw new AggregationException("Problem in Accumulation", e);
            }
        }
        // iterate through cases
        for (String caseString:caseStrings){
            try {
                Map<String, Object> caseMap = JSONUtil.json2map(caseString);
                long caseTime = Long.parseLong((String)caseMap.get("timestamp")); //generalise
                for (Summary summary: summaries){
                // if case belongs in Summary, incorporate it
                    if (caseTime >= summary.getStartTime() && caseTime < summary.getEndTime()){
                        summary.incorporate(caseMap);
                        break;
                    }
                }
            } catch (IOException e){
                throw new AggregationException("Case could not be parsed", e);
            }
                
                
        }
        // pull back summary representations
        for (Summary summary: summaries){
            response.add(summary.asMap());
        }
        return response;
        
    }

    private ArrayList<Summary> summaries;
    private SummaryDefinition summaryDefinition;     
}
