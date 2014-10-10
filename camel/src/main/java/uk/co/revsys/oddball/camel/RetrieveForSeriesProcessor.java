package uk.co.revsys.oddball.camel;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;

public class RetrieveForSeriesProcessor extends AbstractOddballProcessor{

    private String owner;
    private String ruleSet;
    private String aggregator;
    private String series;
    private String selector;

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getTransformer() {
        return transformer;
    }

    public void setTransformer(String transformer) {
        this.transformer = transformer;
    }
    private String transformer;

    public String getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }
    
    public RetrieveForSeriesProcessor(String baseUrl) {
        super(baseUrl);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getHttpMethod() {
        return GET;
    }

    @Override
    public String getUrlPath(Exchange exchange) {
        StringBuilder retrievePath = new StringBuilder("/"+getRuleSet()+"/series/"+getSeries()+"/");
        return retrievePath.toString();
    }
    
    @Override
    public Map<String, String> getPostParameters() {
        Map<String, String> postParameters = new HashMap<String, String>();
        postParameters.put("account", owner);
        if (aggregator!=null){
            postParameters.put("aggregator", aggregator);
        }
        if (selector!=null){
            postParameters.put("selector", selector);
        }
        if (transformer!=null){
            postParameters.put("transformer", transformer);
        }
        
        return postParameters;
    }

}
