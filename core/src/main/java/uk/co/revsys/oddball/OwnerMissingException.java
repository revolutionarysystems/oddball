package uk.co.revsys.oddball;

public class OwnerMissingException extends Exception{

    public OwnerMissingException(String ruleSetName) {
        super("Owner property not available assessing "+ruleSetName);
    }

    public OwnerMissingException(String ruleSetName, Throwable cause) {
        super("Owner property not available assessing "+ruleSetName, cause);
    }

}
