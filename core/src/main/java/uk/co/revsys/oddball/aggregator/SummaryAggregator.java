/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.util.ArrayList;
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
public class SummaryAggregator implements Aggregator{
    
    
    
    @Override
    public ArrayList<Map> aggregateCases(Iterable<String> caseStrings, Map<String, String> options, ResourceRepository resourceRepository) throws AggregationException, InvalidTimePeriodException{
        ArrayList<Map> response = new ArrayList<Map>();
        ArrayList<Summary> summaries = summariseCases(caseStrings, options, resourceRepository);
        for (Summary summary: summaries){
            response.add(summary.asMap());
        }
        return response;
    }
        
        
    public ArrayList<Summary> summariseCases(Iterable<String> caseStrings, Map<String, String> options, ResourceRepository resourceRepository) throws AggregationException, InvalidTimePeriodException{
        
        String summaryDefinitionName = options.get("summaryDefinition");
        try {
            this.summaryDefinition = new SummaryDefinition(summaryDefinitionName, resourceRepository);
        }
        catch (SummaryDefinitionNotLoadedException e){
            throw new AggregationException("Summary Definition could not be loaded", e);
        }
        ArrayList<Summary> summaries = new ArrayList<Summary>();
        // set up time window parameters
        String align = "now";
        if (options.get("align")!=null){
            align = options.get("align");
        }
        boolean complete= false; // only report complete periods
        if (options.get("complete")!=null){
            complete = options.get("complete").equals("true");
        }
        long reportEnd = new Date().getTime();
        if (options.get("periodEnd")!=null){
            reportEnd = Long.parseLong((String) options.get("periodEnd"));
        } else {
            if (options.get("ago")!=null){
                reportEnd = new Date().getTime() - new OddUtil().parseTimePeriod((String) options.get("ago"), "m");
            }
        }
        long periodms = 60 * 60 * 1000; 
        if (options.get("periodDivision")!=null){
            periodms = new OddUtil().parseTimePeriod((String) options.get("periodDivision"), "m");
        }
        if (align.equals("clock")){
            reportEnd = periodms * Math.round(0.499999+reportEnd/(1.0*periodms));
        }
        long reportStart = reportEnd - periodms;
        if (options.get("periodStart")!=null){
            reportStart = Long.parseLong((String) options.get("periodStart"));
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
                long duration = new OddUtil().parseTimePeriod((String) options.get("recent"), "m");
                //long duration = Long.parseLong(options.get("recent"))*60*1000;
                if (align.equals("clock")){ //ensure at least the recent period is covered, when aligned to clock, this means 1 extra period
                    duration+=periodms;
                }
                reportStart = reportEnd - duration;
            }
        } else {
            if (options.get("caseRecent")!=null){
                if (options.get("periodStart")==null && options.get("periods")==null){
                    long duration = new OddUtil().parseTimePeriod((String) options.get("caseRecent"), "m");
                    //long duration = Long.parseLong(options.get("recent"))*60*1000;
                    if (align.equals("clock")){ //ensure at least the recent period is covered, when aligned to clock, this means 1 extra period
                        duration+=periodms;
                    }
                    reportStart = reportEnd - duration;
                }
            }
        }
        if (align.equals("clock")){
            reportStart = periodms * Math.round(-0.499999+reportStart/(1.0*periodms));
        }
        periods = (int) Math.round(0.4999+(reportEnd-reportStart)/(1.0*periodms));  //round up number of periods to ensure coverage
        int endIteration = 0;
        if (complete){   // except take first and last away if complete periods only wanted
            periods--;
            endIteration = 1;
        }
        // set up Summaries
        for (int i=periods-1 ; i>=endIteration; i--){
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
                Object time = new OddUtil().getDeepProperty(caseMap, "case.time");
                if (time==null){
                    time = new OddUtil().getDeepProperty(caseMap, "timestamp");
                }
                long caseTime = Long.parseLong(time.toString());
                for (Summary summary: summaries){
                // if case belongs in Summary, incorporate it
                    if (caseTime >= summary.getStartTime() && caseTime < summary.getEndTime()){
                        summary.incorporate(caseMap, caseTime);
                        break;
                    }
                }
            } catch (IOException e){
                throw new AggregationException("Case could not be parsed:"+caseString, e);
            }
                
                
        }
        return summaries;
    }

    private ArrayList<Summary> summaries;
    private SummaryDefinition summaryDefinition;     

    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
