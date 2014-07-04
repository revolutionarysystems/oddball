package uk.co.revsys.oddball.rules;

public class RuleNotLoadedException extends Exception{

    public RuleNotLoadedException(String rule) {
        super("Rule " + rule + " could not be loaded");
    }

    public RuleNotLoadedException(String rule, Throwable cause) {
        super("Rule " + rule + " could not be loaded", cause);
    }

}
