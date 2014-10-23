package uk.co.revsys.oddball.aggregator;

import uk.co.revsys.oddball.*;
import uk.co.revsys.oddball.bins.*;

public class AccumulationException extends Exception{

    public AccumulationException(String message) {
        super("Accumulation failed " + message);
    }

    public AccumulationException(String message, Throwable cause) {
        super("Accumulation failed " + message, cause);
    }

}
