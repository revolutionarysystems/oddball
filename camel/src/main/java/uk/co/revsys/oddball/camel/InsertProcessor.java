package uk.co.revsys.oddball.camel;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;

public class InsertProcessor extends AbstractOddballProcessor{

    private String owner;
    private String ruleSet;

    public String getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }

    public InsertProcessor(String baseUrl) {
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
        StringBuilder retrievePath = new StringBuilder("/"+getRuleSet()+"/");
        return retrievePath.toString();
    }
    
    @Override
    public Map<String, String> getPostParameters() {
        Map<String, String> postParameters = new HashMap<String, String>();
        if (postParameters.get("account")==null && owner!=null ){
            postParameters.put("account", owner);
        }
        
        return postParameters;
    }

}
