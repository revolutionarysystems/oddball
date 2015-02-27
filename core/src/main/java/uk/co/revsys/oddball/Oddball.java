/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.jsont.JSONTransformer;
import uk.co.revsys.jsont.jexl.CoreUtil;
import uk.co.revsys.jsont.jexl.JEXLJSONPathEvaluator;
import uk.co.revsys.jsont.jexl.MathUtil;
import uk.co.revsys.oddball.aggregator.AggregationException;
import uk.co.revsys.oddball.aggregator.Aggregator;
import uk.co.revsys.oddball.aggregator.CaseComparator;
import uk.co.revsys.oddball.aggregator.ComparatorMap;
import uk.co.revsys.oddball.aggregator.ComparisonException;
import uk.co.revsys.oddball.bins.BinSet;
import uk.co.revsys.oddball.bins.BinSetImpl;
import uk.co.revsys.oddball.bins.BinSetNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.oddball.identifier.CaseIdentifier;
import uk.co.revsys.oddball.identifier.IdentificationSchemeNotLoadedException;
import uk.co.revsys.oddball.identifier.IdentifierMap;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.MongoDBHelper;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.Rule;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetImpl;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;
import uk.co.revsys.oddball.util.InvalidTimePeriodException;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.oddball.util.OddUtil;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class Oddball {

    ResourceRepository resourceRepository;
    HashMap<String, RuleSet> ruleSets = new HashMap<String, RuleSet>();
    BinSet binSet;
    HashMap<String, BinSet> privateBinSets = new HashMap<String, BinSet>();
    HashMap<String, String> transformers = new HashMap<String, String>();

    public Oddball(ResourceRepository resourceRepository, String binSetName) throws BinSetNotLoadedException {
        this.resourceRepository = resourceRepository;
        binSet = loadBinSet(binSetName, resourceRepository);
    }

    private RuleSet ensureRuleSet(String ruleSetName) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet == null) {
            ruleSet = loadRuleSet(ruleSetName, resourceRepository);
            ruleSets.put(ruleSetName, ruleSet);
        }
        return ruleSet;
    }

    public Opinion assessCase(String ruleSetName, String inboundTransformer, Case aCase, int persistOption, String duplicateQuery, String avoidQuery, String ensureIndexes) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        if (inboundTransformer != null) {
            LOGGER.debug("Applying transformation:" + inboundTransformer);
            aCase.setContent(this.transformCase(aCase.getContent(), inboundTransformer));
        }
        return ruleSet.assessCase(aCase, null, ruleSetName, persistOption, duplicateQuery, avoidQuery, ensureIndexes);
    }

    public Opinion assessCase(String ruleSetName, String inboundTransformer, Case aCase) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException, IOException {
        return this.assessCase(ruleSetName, inboundTransformer, aCase, RuleSet.ALWAYSPERSIST, null, null, null);
    }

    public void clearRuleSet(String ruleSetName) {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet != null) {
            ruleSet.getPersist().dropCases();
            ruleSets.remove(ruleSetName);
        }
    }

    public void clearTransformers() {
        transformers.clear();
    }

    private RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException {
        if (ruleSetName.contains(".rules")){
            try{
                return RuleSetImpl.loadJSONRuleSet(ruleSetName, resourceRepository);
            } 
            catch (RuleSetNotLoadedException e){   // not a json file
                return RuleSetImpl.loadRuleSet(ruleSetName, resourceRepository);
            }
        }
        else {
            return RuleSetImpl.loadRuleSet(ruleSetName, resourceRepository);
        }
    }

    public RuleSet reloadRuleSet(String ruleSetName) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet == null) {
            return loadRuleSet(ruleSetName, resourceRepository);
        }
        ruleSet.reloadRules(resourceRepository);
        return ruleSet;
    }

    public RuleSet addExtraRule(String ruleSetName, String prefix, String label, String ruleString, String source) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        Rule duplicate = ruleSet.findExtraRule(prefix, ruleString);
        if (duplicate != null) {
            ruleSet.removeExtraRule(duplicate);
        }
        ruleSet.addExtraRule(ruleSet.createRule(prefix, label, ruleString, null, source, resourceRepository));
        return ruleSet;
    }

    public Collection<String> findRules(String ruleSetName, Map<String, String> options) throws RuleSetNotLoadedException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        List<Rule> rules = ruleSet.getAllRules();
        ArrayList<String> response = new ArrayList<String>();
        String prefix = "";
        String source = "";
        String label = "";
        if (options.get("prefix") != null) {
            prefix = options.get("prefix");
        }
        if (options.get("source") != null) {
            source = options.get("source");
        }
        if (options.get("label") != null) {
            label = options.get("label");
        }
        for (Rule rule : rules) {
            if (prefix.equals("ALL") || rule.getLabel().indexOf(prefix) == 0) {
                if (source.equals("ALL") || rule.getSource().indexOf(source) == 0) {
                    if (label.equals("") || rule.getLabel().contains(label)) {
                        response.add(rule.asJSON());
                    }
                }
            }
        }
        return response;

    }

    public Collection<String> saveRules(String ruleSetName, Map<String, String> options) throws RuleSetNotLoadedException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        List<Rule> rules = ruleSet.getAllRules();
        ArrayList<String> response = new ArrayList<String>();
        String prefix = "ALL";
        String source = "ALL";
        if (options.get("prefix") != null) {
            prefix = options.get("prefix");
        }
        if (options.get("source") != null) {
            source = options.get("source");
        }
        String fileName = ruleSetName;
        fileName = fileName + "." + prefix;
        fileName = fileName + "." + source;

        StringBuilder lines = new StringBuilder("");
        for (Rule rule : rules) {
            if (prefix.equals("ALL") || rule.getLabel().indexOf(prefix) == 0) {
                if (source.equals("ALL") || rule.getSource().indexOf(source) == 0) {
                    lines.append(rule.asRuleConfig());
                }
            }
        }
        InputStream is = new ByteArrayInputStream(lines.toString().getBytes("UTF-8"));
        Resource resource = new Resource("", fileName);

        try {
            resourceRepository.delete(resource);
        } catch (java.io.FileNotFoundException ex) {
        } catch (java.io.IOException ex) {
        }

        resourceRepository.write(resource, is);
        System.out.println("");

        for (Rule rule : rules) {
            if (prefix.equals("ALL") || rule.getLabel().indexOf(prefix) == 0) {
                if (source.equals("ALL") || rule.getSource().indexOf(source) == 0) {
                    response.add(rule.asJSON());
                }
            }
        }
        return response;

    }

    public final BinSet loadBinSet(String binSetName, ResourceRepository resourceRepository) throws BinSetNotLoadedException {
        return BinSetImpl.loadBinSet(binSetName, resourceRepository);
    }

    public BinSet reloadBinSet() throws BinSetNotLoadedException {
        binSet = BinSetImpl.loadBinSet(binSet.getName(), resourceRepository);
        privateBinSets.clear();
        return binSet;
    }

    public String loadTransformer(String transformerName, ResourceRepository resourceRepository) throws TransformerNotLoadedException {
        try {
            Resource resource = new Resource("", transformerName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder transformerStringBuilder = new StringBuilder();
            for (String line : lines) {
                transformerStringBuilder.append(line);
            }
            String transformerString = transformerStringBuilder.toString();
            this.transformers.put(transformerName, transformerString);
            return transformerString;
        } catch (IOException ex) {
            throw new TransformerNotLoadedException(transformerName, ex);
        }
    }

    public String loadProcessor(String processorName, ResourceRepository resourceRepository) throws ProcessorNotLoadedException {
        try {
            Resource resource = new Resource("", processorName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder processorStringBuilder = new StringBuilder();
            for (String line : lines) {
                processorStringBuilder.append(line);
            }
            String processorString = processorStringBuilder.toString();
            return processorString;
        } catch (IOException ex) {
            throw new ProcessorNotLoadedException(processorName, ex);
        }
    }

    public String getTransformer(String transformerName, ResourceRepository resourceRepository) throws TransformerNotLoadedException {
        if (transformers.get(transformerName) != null) {
            return (String) transformers.get(transformerName);
        } else {
            return loadTransformer(transformerName, resourceRepository);
        }
    }

    private Collection<String> filterResults(Iterable<String> results, Map<String, String> options) throws FilterException {
        String filterQuery = options.get("filter");
        ArrayList<String> filtered = new ArrayList<String>();
        Class ruleClass = new RuleTypeMap().get("mongo");
        try {
            Rule ruleInstance = (Rule) ruleClass.newInstance();
            ruleInstance.setRuleString(filterQuery, resourceRepository);
            for (String caseStr : results) {
                boolean hit = ruleInstance.testOneOffRule(new MapCase(caseStr), new MongoDBHelper("filter", true));
                if (hit) {
                    filtered.add(caseStr);
                }
            }
        } catch (Exception e) {
            throw new FilterException("failed applying filter:" + filterQuery);
        }
        return filtered;
    }

    public Collection<String> transformResults(Iterable<String> results, String transformerName) throws TransformerNotLoadedException {
        ArrayList<String> transformed = new ArrayList<String>();
        String transformStr = getTransformer(transformerName, resourceRepository);
        Map evalFunctions = new HashMap();
        evalFunctions.put("math", new MathUtil());
        evalFunctions.put("odd", new OddUtil());
        evalFunctions.put("util", new CoreUtil());
        JSONTransformer transformer = new JSONTransformer(new JEXLJSONPathEvaluator(evalFunctions));
        HashMap params = new HashMap();
        for (String caseStr : results) {
            transformed.add(transformer.transform(caseStr, transformStr, params));
        }
        return transformed;
    }

    private Collection<String> dedupResults(Iterable<String> results) throws TransformerNotLoadedException {
        ArrayList<String> deduped = new ArrayList<String>();
        for (String caseStr : results) {
            if (!deduped.contains(caseStr)) {
                deduped.add(caseStr);
            }
        }
        return deduped;
    }

    private String transformResult(String result, String transformerName) throws TransformerNotLoadedException {
        String transformStr = getTransformer(transformerName, resourceRepository);
        Map evalFunctions = new HashMap();
        evalFunctions.put("math", new MathUtil());
        evalFunctions.put("odd", new OddUtil());
        evalFunctions.put("util", new CoreUtil());
        JSONTransformer transformer = new JSONTransformer(new JEXLJSONPathEvaluator(evalFunctions));
        HashMap params = new HashMap();
        return transformer.transform(result, transformStr, params);
    }

    private String transformCase(String caseStr, String transformerName) throws TransformerNotLoadedException {
        String transformStr = getTransformer(transformerName, resourceRepository);
        Map evalFunctions = new HashMap();
        evalFunctions.put("math", new MathUtil());
        evalFunctions.put("odd", new OddUtil());
        evalFunctions.put("util", new CoreUtil());
        JSONTransformer transformer = new JSONTransformer(new JEXLJSONPathEvaluator(evalFunctions));
        HashMap params = new HashMap();
        return transformer.transform(caseStr, transformStr, params);
    }

    private Collection<String> aggregateResults(Iterable<String> results, Map<String, String> options) throws AggregationException, InvalidTimePeriodException {
        ArrayList<String> aggregatedResults = new ArrayList<String>();
        Class aggregatorClass = new AggregatorMap().get(options.get("aggregator"));
        try {
            Aggregator ag = (Aggregator) aggregatorClass.newInstance();
            ArrayList<Map> aggregated = ag.aggregateCases(results, options, resourceRepository);
            for (Object agg : aggregated) {
                String aggString = JSONUtil.map2json((Map) agg);
                aggString = aggString.replace(":  }", ":{ }");
                aggregatedResults.add(aggString);
            }
            return aggregatedResults;
        } catch (InstantiationException e) {
            throw new AggregationException("Could not instantiate aggregator: " + options.get("aggregator"), e);
        } catch (IllegalAccessException e) {
            throw new AggregationException("Could not instantiate aggregator: " + options.get("aggregator"), e);
        }
    }

    private Collection<String> compareResults(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, IOException, DaoException, TransformerNotLoadedException {
        options.put("owner", owner);
        Collection<String> comparisonResults = comparisonResults(ruleSet, query, options);

        ArrayList<String> comparedResults = new ArrayList<String>();
        Class comparatorClass = new ComparatorMap().get(options.get("comparator"));
        options.put("periodDivision", "1Y");

        try {
            CaseComparator comp = (CaseComparator) comparatorClass.newInstance();
            for (String result : results) {
                Map compared = comp.compare(result, comparisonResults, options, resourceRepository);
                String compString = JSONUtil.map2json((Map) compared);
                compString = compString.replace(":  }", ":{ }");
                comparedResults.add(compString);
            }
            return comparedResults;
        } catch (IOException e) {
            throw new ComparisonException("IOException: " + options.get("comparator"), e);
        } catch (InstantiationException e) {
            throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
        } catch (IllegalAccessException e) {
            throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
        }
    }

    private Collection<String> identifyResults(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, IOException, DaoException, TransformerNotLoadedException, IdentificationSchemeNotLoadedException, AggregationException {
        options.put("owner", owner);
        Collection<String> comparisonResults = comparisonResults(ruleSet, query, options);

        ArrayList<String> identifiedResults = new ArrayList<String>();
        Class identifierClass = new IdentifierMap().get(options.get("identifier"));
        options.put("periodDivision", "1Y");

        try {
            CaseIdentifier ider = (CaseIdentifier) identifierClass.newInstance();
            for (String result : results) {
                Map identified = ider.identify(ruleSet, result, comparisonResults, options, this, resourceRepository);
                String idString = JSONUtil.map2json((Map) identified);
                idString = idString.replace(":  }", ":{ }");
                identifiedResults.add(idString);
            }
            return identifiedResults;
        } catch (IOException e) {
            throw new ComparisonException("IOException: " + options.get("comparator"), e);
        } catch (InstantiationException e) {
            throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
        } catch (IllegalAccessException e) {
            throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
        }
    }

    public Collection<String> comparisonResults(RuleSet ruleSet, String query, Map<String, String> options) throws UnknownBinException, IOException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException {
        Map<String, String> comparisonOptions = new HashMap<String, String>();
        comparisonOptions.putAll(options);
        if (options.containsKey("comparisonQuery")) {
            comparisonOptions.put("query", options.get("comparisonQuery"));
        }
        if (options.containsKey("comparisonAgo")) {
            comparisonOptions.put("ago", options.get("comparisonAgo"));
        }
        if (options.containsKey("comparisonRecent")) {
            comparisonOptions.put("recent", options.get("comparisonRecent"));
        }
        if (options.get("comparisonBinLabel") != null) {
            String binLabel = options.get("comparisonBinLabel");
            String binQuery = this.getBinQuery(binLabel, comparisonOptions);
            comparisonOptions.put("binQuery", binQuery);
        }
        comparisonOptions.remove("selector");
        Collection<String> result = ruleSet.getPersist().findCasesForOwner(options.get("owner"), query, comparisonOptions);
        if (options.get("transformer") != null) {
            result = transformResults(result, getDefaultedTransformer("", options));
        }
        return result;
    }

    private Collection<String> tagResults(Iterable<String> results, Map<String, String> options) throws RuleSetNotLoadedException, InvalidCaseException, TransformerNotLoadedException, IOException {
        ArrayList<String> taggedResults = new ArrayList<String>();
        String ruleSetName = options.get("tagger");
        String incomingXform = null;
        if (ruleSetName.contains(":")) {
            String[] nameSplit = ruleSetName.split(":");
            ruleSetName = nameSplit[0];
            incomingXform = nameSplit[1];
        }
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        for (String result : results) {
            MapCase aCase = new MapCase(result);
            int persistOption = RuleSet.NEVERPERSIST;
            String duplicateQuery = "";
            String avoidQuery = "";
            if (options.get("persist") != null && options.get("persist").equals("true")) {
                persistOption = RuleSet.UPDATEPERSIST;
                if ((options.get("duplicateQuery") != null) && (!options.get("duplicateQuery").equals(""))) {
                    duplicateQuery = options.get("duplicateQuery");
                }
                if ((options.get("avoidQuery") != null) && (!options.get("avoidQuery").equals(""))) {
                    avoidQuery = options.get("avoidQuery");
                }
            }
//    public Opinion assessCase(String ruleSetName, String inboundTransformer, Case aCase, int persistOption, String duplicateQuery) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException {
            Opinion caseOp = this.assessCase(ruleSetName, incomingXform, aCase, persistOption, duplicateQuery, avoidQuery, null);
//            Opinion caseOp = ruleSet.assessCase(aCase, incomingXform, ruleSetName, persistOption, duplicateQuery);
            taggedResults.add(caseOp.getEnrichedCase(ruleSetName, aCase, true));
        }
        return taggedResults;
    }

    private Collection<String> applyProcessor(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, IOException, DaoException, TransformerNotLoadedException, AggregationException, RuleSetNotLoadedException, InvalidCaseException, ProcessorNotLoadedException, FilterException, IdentificationSchemeNotLoadedException {
        String processor = options.get("processor");
        String processorChain = loadProcessor(processor, resourceRepository);
        options.put("processorChain", processorChain);
        return applyProcessorChain(results, options, ruleSet, query, owner);
    }

    private Collection<String> applyProcessorChain(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, IOException, DaoException, TransformerNotLoadedException, AggregationException, RuleSetNotLoadedException, InvalidCaseException, FilterException, IdentificationSchemeNotLoadedException {
        ArrayList<String> processedResults = new ArrayList<String>();
        ArrayList<String> interimResults = new ArrayList<String>();
        String processorChain = options.get("processorChain");
        try {
            Map chain = JSONUtil.json2map("{\"chain\":" + processorChain + "}");
            ArrayList<Object> chainSteps = (ArrayList<Object>) chain.get("chain");
            for (Object step : chainSteps) {
                interimResults = new ArrayList<String>();
                Map<String, String> stepMap = (Map<String, String>) step;
                for (String key : stepMap.keySet()) {
                    stepMap.put(key, stepMap.get(key).replace("{owner}", owner + "/").replace("{account}", owner + "/"));
                }

                if (stepMap.get("retriever") != null) {
                    Map<String, String> subOptions = (Map<String, String>) options;
                    subOptions.putAll(stepMap);
                    interimResults.addAll(initialQuery(owner, options.get("ruleSet"), query, subOptions));
                }
                if (stepMap.get("transformer") != null) {
                    interimResults.addAll(transformResults(results, (String) stepMap.get("transformer")));
                }
                if (stepMap.get("aggregator") != null) {
                    Map<String, String> subOptions = (Map<String, String>) stepMap;
                    if (options.get("recent") != null) {
                        subOptions.put("recent", options.get("recent"));
                    }
                    interimResults.addAll(aggregateResults(results, subOptions));
                }
                if (stepMap.get("comparator") != null) {
                    Map<String, String> subOptions = (Map<String, String>) stepMap;
                    if (options.get("recent") != null) {
                        subOptions.put("recent", options.get("recent"));
                    }
                    if (options.get("caseRecent") != null) {
                        subOptions.put("caseRecent", options.get("recent"));
                    }
                    if (options.get("query") != null) {
                        subOptions.put("query", options.get("query"));
                    }
                    if (options.get("forEach") != null) {
                        subOptions.put("forEach", options.get("forEach"));
                    }
                    if (options.get("forEachValue") != null) {
                        subOptions.put("forEachValue", options.get("forEachValue"));
                    }
                    interimResults.addAll(compareResults(results, subOptions, ruleSet, query, owner));
                }
                if (stepMap.get("identifier") != null) {
                    Map<String, String> subOptions = (Map<String, String>) stepMap;
                    if (options.get("recent") != null) {
                        subOptions.put("recent", options.get("recent"));
                    }
                    if (options.get("caseRecent") != null) {
                        subOptions.put("caseRecent", options.get("caseRecent"));
                    }
                    if (options.get("query") != null && subOptions.get("query") == null) {
                        subOptions.put("query", options.get("query"));
                    }
                    if (options.get("forEach") != null) {
                        subOptions.put("forEach", options.get("forEach"));
                    }
                    if (options.get("forEachValue") != null) {
                        subOptions.put("forEachValue", options.get("forEachValue"));
                    }
                    if (stepMap.get("identityTransformer") != null) {
                        subOptions.put("transformer", stepMap.get("identityTransformer"));
                    }
                    interimResults.addAll(identifyResults(results, subOptions, ruleSet, subOptions.get("query"), owner));
                }
                if (stepMap.get("tagger") != null) {
                    interimResults.addAll(tagResults(results, (Map<String, String>) stepMap));
                }
                if (stepMap.get("filter") != null) {
                    interimResults.addAll(filterResults(results, (Map<String, String>) stepMap));
                }
                results = interimResults;
            }
        } catch (IOException ex) {
            throw new InvalidCaseException("{\"chain\":" + processorChain + "}");
        }
        processedResults.addAll(interimResults);
        return processedResults;
    }

    private String getDefaultedTransformer(String ruleSetName, Map<String, String> options) {
        String transformerStr = options.get("transformer");
        if (transformerStr != null) {
            if (transformerStr.equals("default")) {
                return ruleSetName + ".default.json";
            }
            if (transformerStr.contains("{ruleSet}")) {
                return transformerStr.replace("{ruleSet}", ruleSetName);
            }
            if (transformerStr.indexOf("{owner}") == 0) {
                return transformerStr.replace("{owner}", options.get("owner") + "/");
            }
            return transformerStr;
        } else {
            return transformerStr;
        }
    }

    private Collection<String> restoreHashes(Collection<String> inStrings) {
        Collection<String> result = new ArrayList<String>();
        for (String resultString : inStrings) {
            result.add(resultString.replace("(hash)", "#"));
        }
        return result;
    }

    public Collection<String> findQueryCasesForEach(String ruleSetName, String query, Map<String, String> options) throws IOException, RuleSetNotLoadedException, DaoException, TransformerNotLoadedException, AggregationException, UnknownBinException, InvalidCaseException, InvalidTimePeriodException, ProcessorNotLoadedException, ComparisonException, FilterException, IdentificationSchemeNotLoadedException {
        ArrayList<String> cases = new ArrayList<String>();
        String forEach = options.get("forEach");
        HashMap<String, String> distinctOptions = new HashMap<String, String>();
        distinctOptions.putAll(options);
        distinctOptions.put("distinct", forEach);
        distinctOptions.remove("transformer");
        distinctOptions.remove("aggregator");
        distinctOptions.remove("selector");
        distinctOptions.remove("identifier");
        distinctOptions.remove("comparator");
        distinctOptions.remove("tagger");
        distinctOptions.remove("filter");
        distinctOptions.remove("forEach");
        distinctOptions.remove("processor");
        distinctOptions.remove("processorChain");
        ArrayList<String> allDistinct = new ArrayList<String>();
        allDistinct.addAll(findQueryCases(ruleSetName, query, distinctOptions));
        for (String distinctValue : allDistinct) {
            options.put("forEachValue", distinctValue.replace("\"", ""));
            cases.addAll(findQueryCases(ruleSetName, query, options));
        }
        return cases;
    }

    private Collection<String> initialQuery(String owner, String ruleSetNames, String query, Map<String, String> options) throws RuleSetNotLoadedException, UnknownBinException, IOException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException {
        Collection<String> result = new ArrayList<String>();
        if (options.get("retriever").equals("caseRetriever")) {
            String path = "";
            if (ruleSetNames.contains("/")) {
                path = ruleSetNames.substring(0, ruleSetNames.lastIndexOf("/") + 1);
                ruleSetNames = ruleSetNames.substring(ruleSetNames.lastIndexOf("/") + 1);
            }
            String ruleSets[] = ruleSetNames.split("\\+");
            for (String ruleSetName : ruleSets) {
                RuleSet ruleSet = ensureRuleSet(path + ruleSetName);
                if (options.get("binLabel") != null) {
                    String binLabel = options.get("binLabel");
                    String binQuery = this.getBinQuery(binLabel, options);
                    options.put("binQuery", binQuery);
                }
                Collection<String> interimResult = ruleSet.getPersist().findCasesForOwner(owner, query, options);
                if (options.get("transformer") != null) {
                    interimResult = transformResults(interimResult, getDefaultedTransformer(path + ruleSetName, options));
                }
                result.addAll(interimResult);
            }
        }
        return result;
    }

    public Collection<String> findQueryCases(String ruleSetName, String query, Map<String, String> options) throws IOException, RuleSetNotLoadedException, DaoException, TransformerNotLoadedException, AggregationException, UnknownBinException, InvalidCaseException, InvalidTimePeriodException, ProcessorNotLoadedException, ComparisonException, FilterException, IdentificationSchemeNotLoadedException {
        String owner = Oddball.NONE;
        if (options.get("owner") != null) {
            owner = options.get("owner");
        }
        if (options.get("retriever") == null) {
            options.put("retriever", "caseRetriever");
        }
        Collection<String> result = initialQuery(owner, ruleSetName, query, options);
        String comparisonRuleSet = ruleSetName;
        if (options.get("comparisonRuleSet") != null) {
            comparisonRuleSet = options.get("comparisonRuleSet");
        }
        result = restoreHashes(result);
        if (options.get("distinct") != null) {
            result = dedupResults(result);
        }
        if (options.get("aggregator") != null) {
            result = aggregateResults(result, options);
        }
        if (options.get("comparator") != null) {
            result = compareResults(result, options, attemptRuleSet(comparisonRuleSet), query, owner);
        }
        if (options.get("identifier") != null) {
            result = identifyResults(result, options, attemptRuleSet(comparisonRuleSet), query, owner);
        }
        if (options.get("tagger") != null) {
            result = tagResults(result, options);
        }
        if (options.get("filter") != null) {
            result = filterResults(result, options);
        }
        if (options.get("processorChain") != null) {
            result = applyProcessorChain(result, options, attemptRuleSet(comparisonRuleSet), query, owner);
        }
        if (options.get("processorChain") == null && options.get("processor") != null) {
            result = applyProcessor(result, options, attemptRuleSet(comparisonRuleSet), query, owner);
        }
        return result;
    }

    private RuleSet attemptRuleSet(String ruleSetName) {
        RuleSet ruleSet = null;
        try {
            ruleSet = ensureRuleSet(ruleSetName);
        } catch (RuleSetNotLoadedException ex) {
        };
        return ruleSet;
    }

    public void deleteQueryCases(String ruleSetName, String query, Map<String, String> options) throws IOException, RuleSetNotLoadedException, DaoException, TransformerNotLoadedException, AggregationException, UnknownBinException, InvalidCaseException, InvalidTimePeriodException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.NONE;
        if (options.get("owner") != null) {
            owner = options.get("owner");
        }
        if (options.get("binLabel") != null) {
            String binLabel = options.get("binLabel");
            String binQuery = this.getBinQuery(binLabel, options);
            options.put("binQuery", binQuery);
        }
        ruleSet.getPersist().deleteCasesForOwner(owner, query, options);
    }

    public Collection<String> findCaseById(String ruleSetName, String id, Map<String, String> options) throws RuleSetNotLoadedException, DaoException, TransformerNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        Collection<String> result = ruleSet.getPersist().findCaseById(owner, id);
        if (options.get("transformer") != null) {
            return transformResults(result, getDefaultedTransformer(ruleSetName, options));
        } else {
            return result;
        }
    }

    public Collection<String> deleteCaseById(String ruleSetName, String id, Map<String, String> options) throws RuleSetNotLoadedException, DaoException, TransformerNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        Collection<String> result = ruleSet.getPersist().deleteCaseById(owner, id);
        return result;
    }

    private String getBinQuery(String binLabel, Map<String, String> options) throws UnknownBinException {
        String binQuery;
        String owner = Oddball.ALL;
        if (options.get("owner") != null) {
            owner = options.get("owner");
        }
        BinSet ownerBinSet = loadPrivateBinSet(owner);
        if (ownerBinSet != null && ownerBinSet.getBins().get(binLabel) != null) {
            binQuery = ownerBinSet.getBins().get(binLabel).getBinString();
        } else {
            binQuery = binSet.getBins().get(binLabel).getBinString();
        }
        if (binQuery == null) {
            throw new UnknownBinException(binLabel);
        }
        return binQuery;
    }

    public Collection<String> listBinLabels(String owner) throws BinSetNotLoadedException {
        Collection<String> binLabels = new ArrayList<String>();
        BinSet ownerBinSet = loadPrivateBinSet(owner);
        if (ownerBinSet != null) {
            binLabels.addAll(ownerBinSet.listBinLabels());
        }
        binLabels.addAll(binSet.listBinLabels());
        return binLabels;
    }

    private BinSet loadPrivateBinSet(String owner) {
        BinSet ownerBinSet = this.privateBinSets.get(owner);
        if (ownerBinSet == null) {
            try {
                ownerBinSet = loadBinSet(owner, resourceRepository);
                privateBinSets.put(owner, ownerBinSet);
            } catch (BinSetNotLoadedException e) {
                // owner bin set optional
            }
        }
        return ownerBinSet;
    }

    public String showResource(String resourceName) throws ResourceNotLoadedException {
        try {

            Resource resource = new Resource("", resourceName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder processorStringBuilder = new StringBuilder();
            for (String line : lines) {
                processorStringBuilder.append(line);
            }
            String processorString = processorStringBuilder.toString();
            return processorString;
        } catch (IOException ex) {
            throw new ResourceNotLoadedException(resourceName, ex);
        }
    }

    public List<String> showResourceList(String filter) throws ResourceNotLoadedException {
        String path = ".";
        if (filter.contains("/")) {
            path = path + "/" + filter.substring(0, filter.lastIndexOf("/"));
            filter = filter.substring(filter.lastIndexOf("/") + 1);
        }
        try {
            List<Resource> resources = resourceRepository.listResources(path);
            List<String> matchedResources = new ArrayList<String>();
            boolean rulesetFound = false;
            filter = filter.replace(".", "\\.").replace("*", ".*");
            Pattern p = Pattern.compile(filter);
            for (Resource resource : resources) {
                if (p.matcher(resource.getName()).matches()) {
                    String quoted = "\"" + resource.getName().replace("\"", "\\\"") + "\"";
                    matchedResources.add(quoted);
                }
            }
            return matchedResources;
        } catch (IOException ex) {
            throw new ResourceNotLoadedException(filter, ex);
        }
    }

    public static final String ALL = "_all";
    public static final String NONE = "";
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
