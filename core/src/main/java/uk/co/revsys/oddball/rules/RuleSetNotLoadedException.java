package uk.co.revsys.oddball.rules;

public class RuleSetNotLoadedException extends Exception{

    public RuleSetNotLoadedException(String ruleSet) {
        super("Rule Set " + ruleSet + " could not be loaded");
    }

    public RuleSetNotLoadedException(String ruleSet, Throwable cause) {
        super("Rule Set " + ruleSet + " could not be loaded", cause);
    }

}
