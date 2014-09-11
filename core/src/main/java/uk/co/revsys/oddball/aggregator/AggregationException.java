package uk.co.revsys.oddball.aggregator;

import uk.co.revsys.oddball.*;
import uk.co.revsys.oddball.bins.*;

public class AggregationException extends Exception{

    public AggregationException(String message) {
        super("Aggregation failed " + message);
    }

    public AggregationException(String message, Throwable cause) {
        super("Aggregation failed " + message, cause);
    }

}
