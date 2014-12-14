package uk.co.revsys.oddball.aggregator;

public class ComparisonException extends Exception{

    public ComparisonException(String message) {
        super("Comparison failed " + message);
    }

    public ComparisonException(String message, Throwable cause) {
        super("Comparison failed " + message, cause);
    }

}
