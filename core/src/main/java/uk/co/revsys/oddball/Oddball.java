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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.jsont.JSONTransformer;
import uk.co.revsys.oddball.bins.BinSet;
import uk.co.revsys.oddball.bins.BinSetImpl;
import uk.co.revsys.oddball.bins.BinSetNotLoadedException;
import uk.co.revsys.oddball.bins.UnknownBinException;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.InvalidCaseException;
import uk.co.revsys.oddball.rules.DaoException;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.Rule;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.rules.RuleSetImpl;
import uk.co.revsys.oddball.rules.RuleSetNotLoadedException;
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

    public Opinion assessCase(String ruleSetName, Case aCase) throws RuleSetNotLoadedException, InvalidCaseException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.assessCase(aCase, null, ruleSetName);

    }

    public void clearRuleSet(String ruleSetName) {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet != null) {
            ruleSets.remove(ruleSetName);
        }
    }

    public void clearTransformers() {
        transformers.clear();
    }

    private RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException {
        return RuleSetImpl.loadRuleSet(ruleSetName, resourceRepository);
    }

    public RuleSet reloadRuleSet(String ruleSetName) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet==null){
            return loadRuleSet(ruleSetName, resourceRepository);
        }
        ruleSet.reloadRules(resourceRepository);
        return ruleSet;
    }

    public RuleSet addExtraRule(String ruleSetName, String prefix, String label, String ruleString, String source) throws RuleSetNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        Rule duplicate = ruleSet.findExtraRule(prefix, ruleString);
        if (duplicate!=null){
            ruleSet.removeExtraRule(duplicate);
        }
        ruleSet.addExtraRule(ruleSet.createRule(prefix, label, ruleString, source, resourceRepository));
        return ruleSet;
    }
    
    public Iterable<String> findRules(String ruleSetName, Map<String, String> options) throws RuleSetNotLoadedException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        Set<Rule> rules = ruleSet.getAllRules();
        ArrayList<String> response = new ArrayList<String>();
        String prefix = "";
        String source = "";
        if (options.get("prefix")!=null){
            prefix = options.get("prefix");
        }
        if (options.get("source")!=null){
            source = options.get("source");
        }
        for (Rule rule : rules){
            if (prefix.equals("ALL") || rule.getLabel().indexOf(prefix)==0){
                if (source.equals("ALL") || rule.getSource().indexOf(source)==0){
                    response.add(rule.asJSON());
                }
            }
        }
        return response;
        
    }

    public Iterable<String> saveRules(String ruleSetName, Map<String, String> options) throws RuleSetNotLoadedException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        Set<Rule> rules = ruleSet.getAllRules();
        ArrayList<String> response = new ArrayList<String>();
        String prefix = "ALL";
        String source = "ALL";
        if (options.get("prefix")!=null){
            prefix = options.get("prefix");
        }
        if (options.get("source")!=null){
            source = options.get("source");
        }
        String fileName = ruleSetName;
        fileName = fileName + "."+prefix;
        fileName = fileName + "."+source;
        
        StringBuilder lines = new StringBuilder("");
        for (Rule rule : rules){
            if (prefix.equals("ALL") || rule.getLabel().indexOf(prefix)==0){
                if (source.equals("ALL") || rule.getSource().indexOf(source)==0){
                    lines.append(rule.asRuleConfig());
                }
            }
        }
        System.out.println("Rule lines");
        System.out.println(lines);
        
        InputStream is = new ByteArrayInputStream(lines.toString().getBytes("UTF-8"));
        Resource resource = new Resource("", fileName);

        try {
            resourceRepository.delete(resource);
        } 
        catch (java.io.FileNotFoundException ex){}
        catch (java.io.IOException ex){}
        
        resourceRepository.write(resource, is);
        System.out.println("");
        
        for (Rule rule : rules){
            if (prefix.equals("ALL") || rule.getLabel().indexOf(prefix)==0){
                if (source.equals("ALL") || rule.getSource().indexOf(source)==0){
                    response.add(rule.asJSON());
                }
            }
        }
        return response;
        
    }

    
    
        
public BinSet loadBinSet(String binSetName, ResourceRepository resourceRepository) throws BinSetNotLoadedException {
        return BinSetImpl.loadBinSet(binSetName, resourceRepository);
    }

    public BinSet reloadBinSet() throws BinSetNotLoadedException {
        binSet = BinSetImpl.loadBinSet(binSet.getName(), resourceRepository);
        privateBinSets.clear();
        return binSet;
    }

    public String loadTransformer(String transformerName, ResourceRepository resourceRepository) throws TransformerNotLoadedException{
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
        }
        catch (IOException ex){
            throw new TransformerNotLoadedException(transformerName, ex);
        }
    }
    
    public String getTransformer(String transformerName, ResourceRepository resourceRepository) throws TransformerNotLoadedException{
        if (transformers.get(transformerName)!=null){
            return (String) transformers.get(transformerName);
        } else {
            return loadTransformer(transformerName, resourceRepository);
        }
    }

    private Iterable<String> transformResults(Iterable<String> results, String transformerName) throws TransformerNotLoadedException{
        ArrayList<String> transformed = new ArrayList<String>();
        String transformStr = getTransformer(transformerName, resourceRepository);
        JSONTransformer transformer = new JSONTransformer();
        HashMap params = new HashMap();
        for (String caseStr : results){
            transformed.add(transformer.transform(caseStr, transformStr, params));
        }
        return transformed;
    }
    
    private String transformResult(String result, String transformerName) throws TransformerNotLoadedException{
        String transformStr = getTransformer(transformerName, resourceRepository);
        JSONTransformer transformer = new JSONTransformer();
        HashMap params = new HashMap();
        return transformer.transform(result, transformStr, params);
    }
    
    public Iterable<String> findCases(String ruleSetName, Map<String, String> options) throws RuleSetNotLoadedException, DaoException, TransformerNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        if (options.get("owner")!=null){
            owner = options.get("owner");
        }
        Iterable<String> result = ruleSet.getPersist().findCasesForOwner(owner);
        if (options.get("transformer")!=null){
            return transformResults(result, options.get("transformer"));
        } else {
            return result;
        }
    }

    public Iterable<String> findQueryCases(String ruleSetName, String query, Map<String, String> options) throws RuleSetNotLoadedException, DaoException, TransformerNotLoadedException{
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        if (options.get("owner")!=null){
            owner = options.get("owner");
        }
        Iterable<String> result = ruleSet.getPersist().findCasesForOwner(owner, query);
        if (options.get("transformer")!=null){
            return transformResults(result, options.get("transformer"));
        } else {
            return result;
        }
    }

    public String findLatestQueryCase(String ruleSetName, String query, Map<String, String> options) throws RuleSetNotLoadedException, DaoException, TransformerNotLoadedException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        if (options.get("owner")!=null){
            owner = options.get("owner");
        }
        String result = ruleSet.getPersist().findLatestCaseForOwner(owner, query);
        if (options.get("transformer")!=null){
            return transformResult(result, options.get("transformer"));
        } else {
            return result;
        }
    }

    public Iterable<String> findDistinct(String ruleSetName, String field, String recent, Map<String, String> options) throws RuleSetNotLoadedException, DaoException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String owner = Oddball.ALL;
        if (options.get("owner")!=null){
            owner = options.get("owner");
        }
        return ruleSet.getPersist().findDistinctQuery(owner, "{}", field, recent);
    }

    public Iterable<String> findCasesInBin(String ruleSetName, String binLabel, Map<String, String> options) throws UnknownBinException, RuleSetNotLoadedException, DaoException, BinSetNotLoadedException, TransformerNotLoadedException {
        String binQuery = null;
        String owner = Oddball.ALL;
        if (options.get("owner")!=null){
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
        return findQueryCases(ruleSetName, binQuery, options);
    }

    public Iterable<String> findDistinctPropertyInBin(String ruleSetName, String binLabel, Map<String, String> options) throws UnknownBinException, RuleSetNotLoadedException, DaoException, BinSetNotLoadedException, TransformerNotLoadedException, IOException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        String binQuery = null;
        String owner = Oddball.ALL;
        String property = "_id";
        String recent = "60";
        if (options.get("owner")!=null){
            owner = options.get("owner");
        }
        if (options.get("property")!=null){
            property = options.get("property");
        }
        if (options.get("recent")!=null){
            recent = options.get("recent");
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
        return ruleSet.getPersist().findDistinctQuery(owner, binQuery, property, recent);
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

    private BinSet loadPrivateBinSet(String owner){
        BinSet ownerBinSet = this.privateBinSets.get(owner);
        if (ownerBinSet == null) {
            try {
                ownerBinSet = loadBinSet(owner, resourceRepository);
                privateBinSets.put(owner, ownerBinSet);
            }
            catch(BinSetNotLoadedException e){
                // owner bin set optional
            }
        }
        return ownerBinSet;
    }

    public static final String ALL = "_all";
    static final Logger LOGGER = LoggerFactory.getLogger("oddball");

}
