package uk.co.revsys.oddball.client;

public class CaseQuery {

    private String ruleSet;
    private String ownerProperty;
    private String transformer;
    private String owner;
    private String series;
    private String selector;
    private String query;
    private String forEach;

    public CaseQuery(String ruleSet) {
        this.ruleSet = ruleSet;
    }

    public CaseQuery(String ruleSet, String ownerProperty, String transformer, String owner, String series, String selector) {
        this.ruleSet = ruleSet;
        this.ownerProperty = ownerProperty;
        this.transformer = transformer;
        this.owner = owner;
        this.series = series;
        this.selector = selector;
    }

    public CaseQuery(String ruleSet, String ownerProperty, String transformer, String owner, String series, String selector, String query) {
        this.ruleSet = ruleSet;
        this.ownerProperty = ownerProperty;
        this.transformer = transformer;
        this.owner = owner;
        this.series = series;
        this.selector = selector;
        this.query = query;
    }

    public CaseQuery(String ruleSet, String ownerProperty, String transformer, String owner, String series, String selector, String query, String forEach) {
        this.ruleSet = ruleSet;
        this.ownerProperty = ownerProperty;
        this.transformer = transformer;
        this.owner = owner;
        this.series = series;
        this.selector = selector;
        this.query = query;
        this.forEach = forEach;
    }

    public String getRuleSet() {
        return ruleSet;
    }

    public String getOwnerProperty() {
        return ownerProperty;
    }

    public String getTransformer() {
        return transformer;
    }

    public String getOwner() {
        return owner;
    }

    public String getSeries() {
        return series;
    }

    public String getSelector() {
        return selector;
    }

    public String getQuery() {
        return query;
    }

    public String getForEach() {
        return forEach;
    }

}
