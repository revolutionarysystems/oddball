/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball;

import com.fasterxml.jackson.core.JsonParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
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
import uk.co.revsys.oddball.aggregator.AggregatorMap;
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
import uk.co.revsys.oddball.rules.MongoDBFactory;
import uk.co.revsys.oddball.rules.MongoDBHelper;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.Rule;
import uk.co.revsys.oddball.rules.RuleNotLoadedException;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetImpl;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;
import uk.co.revsys.oddball.rules.RuleTypeMap;
import uk.co.revsys.oddball.tagger.Tagger;
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
    HashMap<String, String> processors = new HashMap<String, String>();

    public Oddball(ResourceRepository resourceRepository, String binSetName) throws BinSetNotLoadedException {
        this.resourceRepository = resourceRepository;
        binSet = loadBinSet(binSetName, resourceRepository);
    }

    public RuleSet ensureRuleSet(String ruleSetName) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet == null) {
            ruleSet = loadRuleSet(ruleSetName, resourceRepository);
            ruleSets.put(ruleSetName, ruleSet);
        }
        return ruleSet;
    }

    public Collection<String> assessCase(String ruleSetName, String inboundTransformer, Case aCase, int persistOption, String duplicateQuery, String avoidQuery, Map<String, String> options) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException, ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, IdentificationSchemeNotLoadedException, OwnerMissingException, JsonParseException, IOException, ParseException {
        Collection<String> results = new ArrayList<String>();
        if (inboundTransformer != null) {
            LOGGER.debug("Applying transformation:" + inboundTransformer);
            aCase.setContent(this.transformCase(aCase.getContent(), inboundTransformer));
        }
        Tagger tagger = new Tagger(ruleSetName, ensureRuleSet(ruleSetName));
        RuleSet saveRuleSet = null;
        if (options.containsKey("saveInto")) {
            saveRuleSet = ensureRuleSet(options.get("saveInto"));
        }
        String taggedCase = tagger.tagCase(aCase, options, persistOption, duplicateQuery, avoidQuery, saveRuleSet);
        if (taggedCase!=null){
            results.add(taggedCase);
            if (options.containsKey("processor")) {
                LOGGER.debug("Applying processor:" + options.get("processor"));
                results = applyProcessor(results, options, tagger.getRuleSet(), "{}", options.get("ownerDir"), JSONUtil.json2map(aCase.toString()));
            }
        } else {
            LOGGER.debug("Null result");
        }
        return results;
    }

    public Collection<String> assessCase(String ruleSetName, String inboundTransformer, String processor, Case aCase) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException, IOException, ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, IdentificationSchemeNotLoadedException, OwnerMissingException, JsonParseException, ParseException {
        System.out.println("Assessing case");
        aCase = new MapCase(aCase.getContent());
        HashMap<String, String> options = new HashMap<String, String>();
        String caseOwner = aCase.getOwner();
//        LOGGER.debug("Looking for owner");
//        LOGGER.debug(aCase.getJSONisedContent());
//        LOGGER.debug(aCase.getOwner());
        if (caseOwner != null) {
            options.put("owner", caseOwner);
        }
        if (processor != null) {
            options.put("processor", processor);
        }
        if (ruleSetName.contains("/")) {
            String ownerPrefix = ruleSetName.substring(0, ruleSetName.indexOf('/') + 1);
            for (String key : options.keySet()) {
                options.put(key, options.get(key).replace("{owner}", ownerPrefix).replace("{account}", ownerPrefix));
            }
            options.put("ownerDir", ownerPrefix.substring(0, ownerPrefix.length() - 1));
        }
        if (caseOwner != null && (!options.containsKey("ownerDir"))) {
            options.put("ownerDir", caseOwner);
        }
        String avoidQuery = "{'case.time':<time>,'case.series':'<series>'}";  // do not persist a signal that duplicates series and millisecond time
        return this.assessCase(ruleSetName, inboundTransformer, aCase, RuleSet.ALWAYSPERSIST, null, avoidQuery, options);
    }

    public Opinion assessCaseOpinion(String ruleSetName, String inboundTransformer, Case aCase, int persistOption, String duplicateQuery, String avoidQuery, Map<String, String> options) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException, IOException, OwnerMissingException {
        ensureRuleSet(ruleSetName);
        if (inboundTransformer != null) {
            LOGGER.debug("Applying transformation:" + inboundTransformer);
            aCase.setContent(this.transformCase(aCase.getContent(), inboundTransformer));
        }
        Tagger tagger = new Tagger(ruleSetName, ensureRuleSet(ruleSetName));
        RuleSet saveRuleSet = null;
        if (options.containsKey("saveInto")) {
            saveRuleSet = ensureRuleSet(options.get("saveInto"));
        }
        Opinion op = tagger.tagCaseOpinion(aCase, options, persistOption, duplicateQuery, avoidQuery, saveRuleSet);
        return op;
    }

    public Opinion assessCaseOpinion(String ruleSetName, String inboundTransformer, Case aCase) throws TransformerNotLoadedException, RuleSetNotLoadedException, InvalidCaseException, IOException, OwnerMissingException {
        return this.assessCaseOpinion(ruleSetName, inboundTransformer, aCase, RuleSet.ALWAYSPERSIST, null, null, new HashMap<String, String>());
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

    public void clearProcessors() {
        processors.clear();
    }

    private RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException {
        if (ruleSetName.contains(".rules")) {
            try {
        //        System.out.println("Looks like JSON");
                return RuleSetImpl.loadJSONRuleSet(ruleSetName, resourceRepository);
            } catch (RuleSetNotLoadedException e) {   // not a json file
        //        System.out.println("Failed to parse JSON");
                return RuleSetImpl.loadRuleSet(ruleSetName, resourceRepository);
            }
        } else {
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

    public String getDbStats(String ruleSetName) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.getPersist().getDbStats();
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

    public String showRules(String ruleSetName, Map<String, String> options) throws RuleSetNotLoadedException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        List<Rule> rules = ruleSet.getAllRules();
        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> ruleSetMap = new HashMap<String, Object>();
        ruleSetMap.put("ruleType", ruleSet.getRuleType());
        ruleSetMap.put("persistence", ruleSet.getRuleHost());
        Map<String, ArrayList<Map<String, Object>>> prefixes = new HashMap<String, ArrayList<Map<String, Object>>>();
        for (Rule rule : rules) {
            String prefix = "other";
            String label = rule.getLabel();
            if (label.contains(".")) {
                String[] labelParts = label.split("\\.");
                prefix = labelParts[0];
//                label = labelParts[1];
            }
            if (!prefixes.containsKey(prefix)) {
                prefixes.put(prefix, new ArrayList<Map<String, Object>>());
            }
            Map ruleMap = rule.asMap();
            ruleMap.put("label", ((String) ruleMap.get("label")).replace(prefix + ".", ""));
            prefixes.get(prefix).add(ruleMap);
        }
        ArrayList<Map<String, Object>> tagTypes = new ArrayList<Map<String, Object>>();
        for (String key : prefixes.keySet()) {
            Map<String, Object> tagType = new HashMap<String, Object>();
            tagType.put("tagType", key);
            tagType.put("default", ruleSet.getPrefixDefault(key + "."));
            tagType.put("rules", prefixes.get(key));
            tagTypes.add(tagType);
        }
        ruleSetMap.put("tagTypes", tagTypes);
        response.put("ruleSet", ruleSetMap);
        return JSONUtil.map2json(response);

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
            this.processors.put(processorName, processorString);
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

    public String getProcessor(String processorName, ResourceRepository resourceRepository) throws ProcessorNotLoadedException {
        if (processors.get(processorName) != null) {
            return (String) processors.get(processorName);
        } else {
            return loadProcessor(processorName, resourceRepository);
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
        } catch (RuleNotLoadedException e) {
            throw new FilterException("Rule not loaded applying filter:" + filterQuery);
        } catch (InvalidCaseException e) {
            throw new FilterException("Invalid Case applying filter:" + filterQuery);
        } catch (InstantiationException e) {
            throw new FilterException("Instantiation Exception applying filter:" + filterQuery);
        } catch (IllegalAccessException e) {
            throw new FilterException("Illegal Access Exception applying filter:" + filterQuery);
        } catch (IOException e) {
           throw new FilterException("IOException applying filter:" + filterQuery);
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
//        LOGGER.debug("init transformer:" + transformerName);
        JSONTransformer transformer = new JSONTransformer(new JEXLJSONPathEvaluator(evalFunctions));
        HashMap params = new HashMap();
        for (String caseStr : results) {
            transformed.add(transformer.transform(caseStr, transformStr, params));
        }
//        LOGGER.debug("signals to be transformed:" + Integer.toString(transformed.size()));
//        LOGGER.debug("done for transformer:" + transformerName);
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

    private Collection<String> incrementResults(Iterable<String> results, Collection<String> episodes, Map<String, String> options) throws AggregationException, InvalidTimePeriodException, IOException, ParseException {
        ArrayList<String> incrementedResults = new ArrayList<String>();
        Class aggregatorClass = new AggregatorMap().get(options.get("incrementor"));
        Map<String, Object> episodeMap = null;
        if (episodes!=null && episodes.iterator().hasNext()) {
            String episode = episodes.iterator().next();
            episodeMap = JSONUtil.json2map(episode);
        }
        try {
            for (String result : results) {
                Aggregator ag = (Aggregator) aggregatorClass.newInstance();
                if (episodeMap != null && episodeMap.containsKey("case")) { // case is decorated with other data
                    Map<String, Object> episodeCase = (Map<String, Object>) episodeMap.get("case");
                    ArrayList<Map> incremented = ag.incrementAggregation(result, episodeMap, options);
                    for (Object inc : incremented) {
//                        LOGGER.debug("EpisodeMap:"+episodeMap.toString());
//                        episodeMap.put("case", (Map<String, Object>) inc);
//                        LOGGER.debug("EpisodeMap:" + episodeMap.toString());
//                        LOGGER.debug("inc:" + inc.toString());
                        String incString = JSONUtil.map2json((Map) inc);
                        incrementedResults.add(incString);
                    }
                } else {
//                    LOGGER.debug("New Episode:");
                    ArrayList<Map> incremented = ag.incrementAggregation(result, episodeMap, options);
                    for (Object inc : incremented) {
//                        LOGGER.debug("Incremented:" + inc.toString());
                        String incString = JSONUtil.map2json((Map) inc);
                        incrementedResults.add(incString);
                    }
                }
            }
            return incrementedResults;
        } catch (InstantiationException e) {
            throw new AggregationException("Could not instantiate aggregator: " + options.get("incrementor"), e);
        } catch (IllegalAccessException e) {
            throw new AggregationException("Could not instantiate aggregator: " + options.get("incrementor"), e);
        } catch (NullPointerException e) {
            LOGGER.trace("NPE", e);
            throw new AggregationException("Problem with aggregator: " + options.get("incrementor"), e);
        }
    }

    private Collection<String> compareResults(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, TransformerNotLoadedException, JsonParseException {
        options.put("owner", owner);
        boolean compareRequired = true;   // default - we will do the compare on the batch of results
//        LOGGER.debug("id required:" + Boolean.toString(idRequired));
        if (options.containsKey("redo") && options.get("redo").equals("false")) {
            compareRequired = false;     // if the redo:false option is set, assume compare NOT required
//            LOGGER.debug("id required:"+ Boolean.toString(idRequired));
            for (String result : results) {
                if (!result.contains("comparison")) { // if one or more cases is missing the id, in which case it IS needed
                    compareRequired = true;
                }
            }
        }
//        LOGGER.debug("id required:"+ Boolean.toString(idRequired));
        ArrayList<String> comparedResults = new ArrayList<String>();
    
        if (compareRequired) {
            Collection<String> comparisonResults = comparisonResults(ruleSet, query, options);

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
            } catch (IOException e) {
                throw new ComparisonException("IOException: " + options.get("comparator"), e);
            } catch (InstantiationException e) {
                throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
            } catch (IllegalAccessException e) {
                throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
            }
        } else { // compare NOT required
            for (String result : results) {
                comparedResults.add(result);
            }
        }
        return comparedResults;
    }

    private Collection<String> identifyResults(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, TransformerNotLoadedException, IdentificationSchemeNotLoadedException, AggregationException, JsonParseException {
        options.put("owner", owner);
        boolean idRequired = true;   // default - we will do the id on the batch of results
//        LOGGER.debug("id required:" + Boolean.toString(idRequired));
        if (options.containsKey("redo") && options.get("redo").equals("false")) {
            idRequired = false;     // if the redo:false option is set, assume id NOT required
//            LOGGER.debug("id required:"+ Boolean.toString(idRequired));
            for (String result : results) {
                if (!result.contains("identification")) { // if one or more cases is missing the id, in which case it IS needed
                    idRequired = true;
                }
            }
        }
//        LOGGER.debug("id required:"+ Boolean.toString(idRequired));
        ArrayList<String> identifiedResults = new ArrayList<String>();
        if (idRequired) {
            Collection<String> comparisonResults = comparisonResults(ruleSet, query, options);

            Class identifierClass = new IdentifierMap().get(options.get("identifier"));
            options.put("periodDivision", "1Y");

            try {
                CaseIdentifier ider = (CaseIdentifier) identifierClass.newInstance();
                for (String result : results) {
                    Map identified = ider.identify(ruleSet, result, comparisonResults, options, this, resourceRepository);
                    //                LOGGER.debug(identified.toString());
                    String idString = JSONUtil.map2json((Map) identified);
                    //                LOGGER.debug(idString);
                    idString = idString.replace(":  }", ":{ }");
                    identifiedResults.add(idString);
                }
            } catch (IOException e) {
                throw new ComparisonException("IOException: " + options.get("comparator"), e);
            } catch (InstantiationException e) {
                throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
            } catch (IllegalAccessException e) {
                throw new ComparisonException("Could not instantiate aggregator: " + options.get("comparator"), e);
            }
        } else { // id NOT required
            for (String result : results) {
                identifiedResults.add(result);
            }
        }
        return identifiedResults;
    }

    public Collection<String> comparisonResults(RuleSet ruleSet, String query, Map<String, String> options) throws UnknownBinException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException, JsonParseException {
        Map<String, String> comparisonOptions = new HashMap<String, String>();
//        LOGGER.debug("comparisonResults");
//        LOGGER.debug(options.toString());
        comparisonOptions.putAll(options);
        if (options.containsKey("comparisonQuery")) {
            comparisonOptions.put("query", options.get("comparisonQuery"));
            query = options.get("comparisonQuery");
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

//        LOGGER.debug(comparisonOptions.toString());
        Collection<String> result = ruleSet.getPersist().findCasesForOwner(options.get("owner"), query, comparisonOptions);
        if (options.get("transformer") != null) {
            result = transformResults(result, getDefaultedTransformer("", options));
        }
        return result;
    }

    private Collection<String> tagResults(Iterable<String> results, Map<String, String> options) throws RuleSetNotLoadedException, InvalidCaseException, TransformerNotLoadedException, ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, IdentificationSchemeNotLoadedException, OwnerMissingException, JsonParseException, IOException, ParseException {
        ArrayList<String> taggedResults = new ArrayList<String>();
        String ruleSetName = options.get("tagger");
        String incomingXform = null;
        if (ruleSetName.contains(":")) {
            String[] nameSplit = ruleSetName.split(":");
            ruleSetName = nameSplit[0];
            incomingXform = nameSplit[1];
        }
        for (String result : results) {
            MapCase aCase = new MapCase(result);

            int persistOption = RuleSet.NEVERPERSIST;
            String duplicateQuery = null;
            String avoidQuery = null;
            if (options.get("persist") != null && options.get("persist").equals("true")) {
                persistOption = RuleSet.UPDATEPERSIST;
                if ((options.get("duplicateQuery") != null) && (!options.get("duplicateQuery").equals(""))) {
                    duplicateQuery = options.get("duplicateQuery");
                }
                if ((options.get("avoidQuery") != null) && (!options.get("avoidQuery").equals(""))) {
                    avoidQuery = options.get("avoidQuery");
                }
            }
            Collection<String> assessment = this.assessCase(ruleSetName, incomingXform, aCase, persistOption, duplicateQuery, avoidQuery, options);
            taggedResults.add(assessment.iterator().next());
        }
        return taggedResults;
    }

    private Collection<String> retrieveResults(Iterable<String> results, Map<String, String> options, String owner, Map<String, Object> caseMap) throws RuleSetNotLoadedException, InvalidCaseException, TransformerNotLoadedException, ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, AggregationException, ProcessorNotLoadedException, FilterException, IdentificationSchemeNotLoadedException, JsonParseException {
        ArrayList<String> retrievedResults = new ArrayList<String>();
        ArrayList<String> inputs = new ArrayList<String>();
        for (String result : results) {
            inputs.add(result);
        }
        if (!results.iterator().hasNext()) {
            inputs.add("{}");
        }
        String query = "{ }";
        for (String input : inputs) {
            if (options.containsKey("query")) {
                query = options.get("query").replace("'", "\"");
                OddUtil ou = new OddUtil();
                if (caseMap == null) {
                    caseMap = (Map<String, Object>) JSONUtil.json2map(input).get("case");
                }
                if (caseMap != null) {
                    query = ou.replacePlaceholders(query, caseMap);
                }
            }
//            LOGGER.debug(options.get("ruleSet"));
//            LOGGER.debug(query);
            Collection<String> retrieved = initialQuery(owner, options.get("ruleSet"), query, options);
//            LOGGER.debug(retrieved.toString());
            if (options.containsKey("results")){
//                LOGGER.debug(options.get("results"));
                if (options.get("results").equals("addLink") && !input.equals("{}")){
                    String first = "{\"_id\":\"null\"}";
                    if (retrieved.iterator().hasNext()) {
                        first = (String) retrieved.iterator().next();
                    }
                    String linkName = "link";
                    if (options.containsKey("resultName")) {
                        linkName = (String) options.get("resultName");
                    }
                    String linkedInput = addLink(input, first, linkName);
                    retrievedResults.add(linkedInput);
                } else if (options.get("results").equals("delete")){
                    for (String retrievedCase : retrieved){
                        deleteCase(options.get("ruleSet"), retrievedCase);
                    }
                } else if (options.get("results").equals("merge")){
                    for (String retrievedCase : retrieved){
                        String mergedInput = mergeCases(retrievedCase, input);
                        retrievedResults.add(mergedInput);
                    }
                } else {
                   retrievedResults.addAll(retrieved);
                }
            } else {
               retrievedResults.addAll(retrieved);
            }
        }
//        LOGGER.debug("retriever found");
//        LOGGER.debug(Integer.toString(retrievedResults.size()));
        return retrievedResults;
    }

    private Collection<String> applyProcessor(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner, Map<String, Object> caseMap) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, TransformerNotLoadedException, AggregationException, RuleSetNotLoadedException, InvalidCaseException, ProcessorNotLoadedException, FilterException, IdentificationSchemeNotLoadedException, OwnerMissingException, JsonParseException, IOException, ParseException {
        String processor = options.get("processor");
//        String processorChain = loadProcessor(processor, resourceRepository);
        String processorChain = getProcessor(processor, resourceRepository);
        options.put("processorChain", processorChain);
        return applyProcessorChain(results, options, ruleSet, query, owner, caseMap);
    }

    private Collection<String> applyProcessorChain(Iterable<String> results, Map<String, String> options, RuleSet ruleSet, String query, String owner, Map<String, Object> caseMap) throws ComparisonException, InvalidTimePeriodException, UnknownBinException, DaoException, TransformerNotLoadedException, AggregationException, RuleSetNotLoadedException, InvalidCaseException, FilterException, IdentificationSchemeNotLoadedException, ProcessorNotLoadedException, OwnerMissingException, JsonParseException, IOException, ParseException {
        ArrayList<String> processedResults = new ArrayList<String>();
        ArrayList<String> interimResults = new ArrayList<String>();
        Map<String, ArrayList<String>> storedResults = new HashMap<String, ArrayList<String>>();
        String processorChain = options.get("processorChain");
        String ownerDir = "";
        if (options.containsKey("ownerDir")) {
            ownerDir = (String) options.get("ownerDir");
        }
        if (!options.containsKey("owner")) {
            if (owner != null) {
                options.put("owner", owner);
            } else {
                options.put("owner", ownerDir);
            }
        }
        ArrayList<Object> chainSteps;
        Map chain = JSONUtil.json2map("{\"chain\":" + processorChain + "}");
        chainSteps = (ArrayList<Object>) chain.get("chain");
        for (Object step : chainSteps) {
            interimResults = new ArrayList<String>();
            Map<String, String> stepMap = (Map<String, String>) step;
            LOGGER.debug("Processor Step");
            LOGGER.debug(stepMap.toString());
            for (String key : stepMap.keySet()) {
                stepMap.put(key, stepMap.get(key).replace("{owner}", ownerDir + "/").replace("{account}", ownerDir + "/"));
            }
            if (stepMap.get("retriever") != null) {
                Map<String, String> subOptions = (Map<String, String>) options;
                subOptions.putAll(stepMap);
                interimResults.addAll(retrieveResults(results, subOptions, owner, caseMap));
            }
            if (stepMap.get("transformer") != null) {
                interimResults.addAll(transformResults(results, (String) stepMap.get("transformer")));
            }
            if (stepMap.get("aggregator") != null) {
                Map<String, String> subOptions = (Map<String, String>) stepMap;
                if (options.get("recent") != null && subOptions.get("recent") == null) {
                    subOptions.put("recent", options.get("recent"));
                }
                if (owner != null && subOptions.get("owner") == null) {
                    subOptions.put("owner", owner);
                }
                interimResults.addAll(aggregateResults(results, subOptions));
            }
            if (stepMap.get("incrementor") != null) {
                Map<String, String> subOptions = (Map<String, String>) stepMap;
                if (options.get("recent") != null && subOptions.get("recent") == null) {
                    subOptions.put("recent", options.get("recent"));
                }
                if (owner != null && subOptions.get("owner") == null) {
                    subOptions.put("owner", owner);
                }
                if (subOptions.get("episode") == null) {
                    subOptions.put("episode", "store:episode");
                }
                interimResults.addAll(incrementResults(results, storedResults.get(subOptions.get("episode")), subOptions));
            }
            if (stepMap.get("comparator") != null) {
                Map<String, String> subOptions = (Map<String, String>) stepMap;
                if (options.get("recent") != null) {
                    if (subOptions.get("recent") == null) {
                        subOptions.put("recent", options.get("recent"));
                    }
                }
                if (options.get("caseRecent") != null) {
                    if (subOptions.get("caseRecent") == null) {
                        subOptions.put("caseRecent", options.get("recent"));
                    }
                }
                if (options.get("query") != null) {
                    if (subOptions.get("query") == null && subOptions.get("comparisonQuery") == null) {
                        subOptions.put("query", options.get("query"));
                    }
                }
                if (options.get("ownerProperty") != null) {
                    if (subOptions.get("ownerProperty") == null) {
                        subOptions.put("ownerProperty", options.get("ownerProperty"));
                    }
                }
                if (options.get("forEach") != null) {
                    subOptions.put("forEach", options.get("forEach"));
                }
                if (options.get("forEachValue") != null) {
                    subOptions.put("forEachValue", options.get("forEachValue"));
                }
                RuleSet stepRuleSet = ruleSet;
                if (stepMap.get("comparisonRuleSet") != null) {
                    stepRuleSet = ensureRuleSet(stepMap.get("comparisonRuleSet"));
                    subOptions.put("transformer", stepMap.get("identityTransformer"));
                }

                try {
                    interimResults.addAll(compareResults(results, subOptions, stepRuleSet, query, owner));
                } catch (ComparisonException e) {
                    interimResults.addAll((Collection) results);
                    LOGGER.warn("Comparator " + stepMap.get("comparator") + " failed - process continues", e);
                }
            }
            if (stepMap.get("identifier") != null) {
                Map<String, String> subOptions = (Map<String, String>) stepMap;
                if (options.get("recent") != null && subOptions.get("recent") == null) {
                    subOptions.put("recent", options.get("recent"));
                }
                if (options.get("caseRecent") != null && subOptions.get("caseRecent") == null) {
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
                RuleSet stepRuleSet = ruleSet;
                if (stepMap.get("ruleSet") != null) {
                    stepRuleSet = ensureRuleSet(stepMap.get("ruleSet"));
                    subOptions.put("transformer", stepMap.get("identityTransformer"));
                }

                interimResults.addAll(identifyResults(results, subOptions, stepRuleSet, subOptions.get("query"), owner));
            }
            if (stepMap.get("tagger") != null) {
                interimResults.addAll(tagResults(results, (Map<String, String>) stepMap));
            }
            if (stepMap.get("retagger") != null) {
                stepMap.put("retag", "true");
                stepMap.put("tagger", stepMap.get("retagger"));
                try {
                    interimResults.addAll(tagResults(results, (Map<String, String>) stepMap));
                } catch (RuleSetNotLoadedException e) {
                    interimResults.addAll((Collection) results);
                    LOGGER.warn("Retag ruleSet " + stepMap.get("retagger") + " not loaded - process continues", e);
                }
            }
            if (stepMap.get("filter") != null) {
                interimResults.addAll(filterResults(results, (Map<String, String>) stepMap));
            }
            if (stepMap.get("processor") != null) {
                if (owner != null && stepMap.get("owner") == null) {
                    stepMap.put("owner", owner);
                }
                if (ownerDir != null && stepMap.get("ownerDir") == null) {
                    stepMap.put("ownerDir", ownerDir);
                }
                try {
                    interimResults.addAll(applyProcessor(results, (Map<String, String>) stepMap, ruleSet, query, owner, caseMap));
                } catch (ProcessorNotLoadedException ex) {  //log but don't fail
                    LOGGER.warn("Processor not loaded:" + stepMap.get("processor"), ex);
                }
            }
            if (stepMap.get("results") == null || stepMap.get("results").equals("retain") || stepMap.get("results").equals("addLink")|| stepMap.get("results").equals("merge")) {
                results = interimResults;
            } else if (stepMap.get("results").contains("store:")) {
                storedResults.put(stepMap.get("results"), interimResults);
                interimResults = (ArrayList<String>) results;
            } else { //revert
                interimResults = (ArrayList<String>) results;
            }
            //LOGGER.debug(step.toString());
        }
//        } catch (IOException ex) {
//            throw new InvalidCaseException("{\"chain\":" + processorChain + "}");
//        }
        processedResults.addAll(interimResults);
        return processedResults;
    }

    private String addLink(String baseResult, String linkResult, String resultName) throws JsonParseException {
        Map<String, Object> baseMap = JSONUtil.json2map(baseResult);
        Map<String, Object> linkMap = JSONUtil.json2map(linkResult);
//            baseMap.put(resultName, "test");
        String linkId = (String) linkMap.get("_id");
        baseMap.put(resultName, linkId);
        return JSONUtil.map2json(baseMap);
    }

    private void deleteCase(String ruleSetName, String caseString) throws JsonParseException, RuleSetNotLoadedException {
        Map<String, Object> caseMap = JSONUtil.json2map(caseString);
//            baseMap.put(resultName, "test");
        String caseId = (String) caseMap.get("_id");
        deleteCaseById(ruleSetName, caseString, null);
    }

    private String mergeCases(String caseA, String caseB) throws JsonParseException{
        Map<String, Object> caseMapA = JSONUtil.json2map(caseA);
//        LOGGER.debug(caseMapA.toString());
        Map<String, Object> caseMapB = JSONUtil.json2map(caseB);
//        LOGGER.debug(caseMapB.toString());
        caseMapB = new OddUtil().mergeMaps(caseMapA, caseMapB);
//        LOGGER.debug(caseMapB.toString());
        //caseMapB.putAll(caseMapA);
        return JSONUtil.map2json(caseMapB);
    }


    private String getDefaultedTransformer(String ruleSetName, Map<String, String> options) {
        String transformerStr = options.get("transformer");
        if (transformerStr != null) {
            if (transformerStr.equals("default")) {
                return ruleSetName + ".default.xform";
            }
            if (transformerStr.contains("{ruleSet}")) {
                return transformerStr.replace("{ruleSet}", ruleSetName);
            }
            String ownerDir = "";
            if (options.containsKey("ownerDir")) {
                ownerDir = (String) options.get("ownerDir");
            }
            if (transformerStr.indexOf("{owner}") == 0) {
                return transformerStr.replace("{owner}", ownerDir + "/").replace("{account}", ownerDir + "/");
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

    private ArrayList<String> dimensionCombinations(ArrayList<ArrayList<String>> distinctDimensionValues) {
        if (distinctDimensionValues.size() == 1) {
            return distinctDimensionValues.get(0);
        } else {
            ArrayList<String> firstDimension = distinctDimensionValues.remove(0);
            ArrayList<String> subResult = dimensionCombinations(distinctDimensionValues);
            ArrayList<String> result = new ArrayList<String>();
            for (String a : firstDimension) {
                for (String b : subResult) {
                    result.add(a + "," + b);
                }
            }
            return result;
        }
    }

    public Collection<String> findQueryCasesForEach(String ruleSetName, String query, Map<String, String> options) throws IOException, RuleSetNotLoadedException, DaoException, TransformerNotLoadedException, AggregationException, UnknownBinException, InvalidCaseException, InvalidTimePeriodException, ProcessorNotLoadedException, ComparisonException, FilterException, IdentificationSchemeNotLoadedException, OwnerMissingException, JsonParseException, ParseException {
        ArrayList<String> cases = new ArrayList<String>();
        String forEach = options.get("forEach");
        HashMap<String, String> distinctOptions = new HashMap<String, String>();
        distinctOptions.putAll(options);
        distinctOptions.remove("transformer");
        distinctOptions.remove("aggregator");
        distinctOptions.remove("selector");
        distinctOptions.remove("identifier");
        distinctOptions.remove("comparator");
        distinctOptions.remove("tagger");
        distinctOptions.remove("filter");
        distinctOptions.remove("forEach");
        distinctOptions.remove("count");
        distinctOptions.remove("processor");
        distinctOptions.remove("processorChain");
        String forEachProps[] = forEach.split(",");
        ArrayList<ArrayList<String>> distinctDimensionValues = new ArrayList<ArrayList<String>>();
        for (String forEachProp : forEachProps) {
            distinctOptions.put("distinct", forEachProp);
            ArrayList<String> allDistinct = new ArrayList<String>();
            allDistinct.addAll(findQueryCases(ruleSetName, query, distinctOptions));
            distinctDimensionValues.add(allDistinct);
        }
        ArrayList<String> combinations = dimensionCombinations(distinctDimensionValues);
        for (String combination : combinations) {
            options.put("forEachValue", combination.replace("\"", ""));
            cases.addAll(wrapSimpleValues(combination.replace("\",\"", ","), findQueryCases(ruleSetName, query, options)));
        }
//        }
        return cases;
    }

    private ArrayList<String> wrapSimpleValues(String forValue, Collection<String> results) {
        ArrayList<String> wrapped = new ArrayList<String>();
        for (String result : results) {
            if (!result.contains("{")) {
                result = "{\"for\":" + forValue + ",\"value\":" + result + "}";
            } else {
                result = "{\"for\":" + forValue + "," + result.substring(1);
            }
            wrapped.add(result);
        }
        return wrapped;
    }

    private Collection<String> initialQuery(String owner, String ruleSetNames, String query, Map<String, String> options) throws RuleSetNotLoadedException, UnknownBinException, DaoException, InvalidTimePeriodException, TransformerNotLoadedException {
        Collection<String> result = new ArrayList<String>();
        if (options.get("retriever").equals("caseRetriever")) {
            if (options.containsKey("ruleSet")) {
                ruleSetNames = options.get("ruleSet");
            }
            String path = "";
            if (ruleSetNames.contains("/")) {
                path = ruleSetNames.substring(0, ruleSetNames.lastIndexOf("/") + 1);
                ruleSetNames = ruleSetNames.substring(ruleSetNames.lastIndexOf("/") + 1);
            }
            String ruleSetNameArray[] = ruleSetNames.split("\\+");
            for (String ruleSetName : ruleSetNameArray) {
                RuleSet ruleSet = ensureRuleSet(path + ruleSetName);
                if (options.get("binLabel") != null) {
                    String binLabel = options.get("binLabel");
                    String binQuery = this.getBinQuery(binLabel, options);
                    options.put("binQuery", binQuery);
                }
                try {
                    Collection<String> interimResult = ruleSet.getPersist().findCasesForOwner(owner, query, options);
                    if (options.get("transformer") != null) {
                        interimResult = transformResults(interimResult, getDefaultedTransformer(path + ruleSetName, options));
                    }
//                    LOGGER.debug("Initial Query");
//                    LOGGER.debug(interimResult.toString());
                    result.addAll(interimResult);
                } catch (IOException ex) {
                    LOGGER.error("Can't find cases for query:" + query, ex);
                }
            }
        }
        LOGGER.debug(result.toString());
        return result;
    }

    public Collection<String> findQueryCases(String ruleSetName, String query, Map<String, String> options) throws IOException, RuleSetNotLoadedException, DaoException, TransformerNotLoadedException, AggregationException, UnknownBinException, InvalidCaseException, InvalidTimePeriodException, ProcessorNotLoadedException, ComparisonException, FilterException, IdentificationSchemeNotLoadedException, OwnerMissingException, JsonParseException, ParseException {
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
            result = applyProcessorChain(result, options, attemptRuleSet(comparisonRuleSet), query, owner, null);
        }
        if (options.get("processorChain") == null && options.get("processor") != null) {
            result = applyProcessor(result, options, attemptRuleSet(comparisonRuleSet), query, owner, null);
        }
        return result;
    }

    private RuleSet attemptRuleSet(String ruleSetName) {
        RuleSet ruleSet = null;
        try {
            ruleSet = ensureRuleSet(ruleSetName);
        } catch (RuleSetNotLoadedException ex) {
            // silent fail, return null
        }
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

    public Collection<String> deleteCaseById(String ruleSetName, String id, Map<String, String> options) throws RuleSetNotLoadedException{
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        Collection<String> result = new HashSet<String>();
        try {
            result = ruleSet.getPersist().deleteCaseById(owner, id);
        } catch (DaoException ex) {
            java.util.logging.Logger.getLogger(Oddball.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            StringBuilder resourceStringBuilder = new StringBuilder();
            for (String line : lines) {
                resourceStringBuilder.append(line);
            }
            String resourceString = resourceStringBuilder.toString();
            return resourceString;
        } catch (IOException ex) {
            throw new ResourceNotLoadedException(resourceName, ex);
        }
    }

    public void uploadResource(String resourceName, String resourceString) throws ResourceNotUploadedException {
        try {

            Resource resource = new Resource("", resourceName);
            InputStream stream = new ByteArrayInputStream(resourceString.getBytes(Charset.forName("UTF-8")));
            resourceRepository.write(resource, stream);
        } catch (IOException ex) {
            throw new ResourceNotUploadedException(resourceName, ex);
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

    public List<String> showDatabaseList(String filter) {
        String path = ".";
        if (filter.contains("/")) {
            filter = filter.substring(filter.lastIndexOf("/") + 1);
        }
        List<String> dbNames = MongoDBFactory.getDBNames();
        List<String> matchedNames = new ArrayList<String>();
        boolean dbFound = false;
        filter = filter.replace(".", "\\.").replace("*", ".*");
        Pattern p = Pattern.compile(filter);
        for (String dbName : dbNames) {
            if (p.matcher(dbName).matches()) {
//                String quoted = "\"" + dbName.replace("\"", "\\\"") + "\"";
//                matchedNames.add(quoted);
                matchedNames.add(dbName);
            }
        }
        List<String> dataSources = new ArrayList<String>();
        for (String name : matchedNames) {
            String ruleSetName;
            String[] fragments = name.split("-");
            int fragmentCount = fragments.length;
            int ownerFragments;
            String owner = "";
            if (name.contains("-rules-")) {
                ruleSetName = fragments[fragmentCount - 3] + ".rules";
                ownerFragments = fragmentCount - 3;
            } else {
                ruleSetName = fragments[fragmentCount - 2];
                ownerFragments = fragmentCount - 2;
            }
            for (int i = 0; i < ownerFragments; i++) {
                owner = owner + fragments[i] + "-";
            }
            if (ownerFragments > 0) {
                owner = owner.substring(0, owner.length() - 1);
            }
            Map<String, Object> dataSourceMap = new HashMap<String, Object>();
            dataSourceMap.put("databaseName", name);
            dataSourceMap.put("ruleSet", owner + "/" + ruleSetName);
            dataSources.add(JSONUtil.map2json(dataSourceMap));
        }
        return dataSources;
    }

    public static final String ALL = "_all";
    public static final String NONE = "";
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
