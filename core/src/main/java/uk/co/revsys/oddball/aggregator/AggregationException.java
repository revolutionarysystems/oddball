package uk.co.revsys.oddball.aggregator;

public class AggregationException extends Exception{

    public AggregationException(String message) {
        super("Aggregation failed " + message);
    }

    public AggregationException(String message, Throwable cause) {
        super("Aggregation failed " + message, cause);
    }

}
