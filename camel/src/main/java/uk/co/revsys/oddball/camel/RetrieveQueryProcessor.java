package uk.co.revsys.oddball.camel;

import java.util.HashMap;
import java.util.Map;

public class RetrieveQueryProcessor extends AbstractOddballProcessor{

    private String owner;
    private String ruleSet;
    private String aggregator;
    private String selector;

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
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
    
    public RetrieveQueryProcessor(String baseUrl) {
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
    public String getUrlPath() {
        StringBuilder retrievePath = new StringBuilder("/"+getRuleSet()+"/query/");
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
