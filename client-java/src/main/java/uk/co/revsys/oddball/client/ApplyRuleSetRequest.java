package uk.co.revsys.oddball.client;

public class ApplyRuleSetRequest {

    private String ruleSet;
    private String body;
    private String inboundTransformer;
    private String processor;

    public ApplyRuleSetRequest(String ruleSet, String body) {
        this.ruleSet = ruleSet;
        this.body = body;
    }

    public ApplyRuleSetRequest(String ruleSet, String body, String inboundTransformer, String processor) {
        this.ruleSet = ruleSet;
        this.body = body;
        this.inboundTransformer = inboundTransformer;
        this.processor = processor;
    }

    public String getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getInboundTransformer() {
        return inboundTransformer;
    }

    public void setInboundTransformer(String inboundTransformer) {
        this.inboundTransformer = inboundTransformer;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }
    
}
