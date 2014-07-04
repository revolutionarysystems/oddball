/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import uk.co.revsys.oddball.bins.Bin;
import uk.co.revsys.oddball.bins.BinImpl;
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
import uk.co.revsys.resource.repository.model.RepositoryItem;
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
        LOGGER.debug("assessing: case = " + aCase.getJSONisedContent());
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.assessCase(aCase, null, ruleSetName);

    }

    public void clearRuleSet(String ruleSetName) {
        RuleSet ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet != null) {
            ruleSets.remove(ruleSetName);
        }
    }

    private RuleSet loadRuleSet(String ruleSetName, ResourceRepository resourceRepository) throws RuleSetNotLoadedException {
        return RuleSetImpl.loadRuleSet(ruleSetName, resourceRepository);
    }

    public BinSet loadBinSet(String binSetName, ResourceRepository resourceRepository) throws BinSetNotLoadedException {
        return BinSetImpl.loadBinSet(binSetName, resourceRepository);

    }

    public BinSet reloadBinSet() throws BinSetNotLoadedException {
        binSet = BinSetImpl.loadBinSet(binSet.getName(), resourceRepository);
        privateBinSets.clear();
        return binSet;
    }

    public Iterable<String> findAllCases(String ruleSetName) throws RuleSetNotLoadedException, DaoException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.getPersist().findCasesForOwner(Oddball.ALL);
    }

    public Iterable<String> findCasesForOwner(String ruleSetName, String owner) throws RuleSetNotLoadedException, DaoException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.getPersist().findCasesForOwner(owner);
    }

    public Iterable<String> findAllQueryCases(String ruleSetName, String query) throws RuleSetNotLoadedException, DaoException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.getPersist().findCasesForOwner(Oddball.ALL, query);
    }

    public Iterable<String> findQueryCasesForOwner(String ruleSetName, String owner, String query) throws RuleSetNotLoadedException, DaoException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.getPersist().findCasesForOwner(owner, query);
    }

    public Iterable<String> findDistinct(String ruleSetName, String owner, String field, String recent) throws RuleSetNotLoadedException, DaoException {
        RuleSet ruleSet = ensureRuleSet(ruleSetName);
        return ruleSet.getPersist().findDistinct(owner, field, recent);
    }

    public Iterable<String> findAllCasesInBin(String ruleSetName, String binLabel) throws RuleSetNotLoadedException, DaoException {
        String binQuery = binSet.getBins().get(binLabel).getBinString();
        return findAllQueryCases(ruleSetName, binQuery);
    }

    public Iterable<String> findCasesInBinForOwner(String ruleSetName, String owner, String binLabel) throws UnknownBinException, RuleSetNotLoadedException, DaoException, BinSetNotLoadedException {
        String binQuery = null;
        BinSet ownerBinSet = loadPrivateBinSet(owner);
        if (ownerBinSet != null && ownerBinSet.getBins().get(binLabel) != null) {
            binQuery = ownerBinSet.getBins().get(binLabel).getBinString();
        } else {
            binQuery = binSet.getBins().get(binLabel).getBinString();
        }
        if (binQuery == null) {
            throw new UnknownBinException(binLabel);
        }
        return findQueryCasesForOwner(ruleSetName, owner, binQuery);
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

    private BinSet loadPrivateBinSet(String owner) throws BinSetNotLoadedException {
        BinSet ownerBinSet = this.privateBinSets.get(owner);
        if (ownerBinSet == null) {
            ownerBinSet = loadBinSet(owner, resourceRepository);
            privateBinSets.put(owner, ownerBinSet);
        }
        return ownerBinSet;
    }

    public static final String ALL = "_all";
    static final Logger LOGGER = Logger.getLogger("oddball");

}
