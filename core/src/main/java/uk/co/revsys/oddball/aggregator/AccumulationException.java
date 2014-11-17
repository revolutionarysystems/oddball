package uk.co.revsys.oddball.aggregator;

public class AccumulationException extends Exception{

    public AccumulationException(String message) {
        super("Accumulation failed " + message);
    }

    public AccumulationException(String message, Throwable cause) {
        super("Accumulation failed " + message, cause);
    }

}
