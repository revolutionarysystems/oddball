package uk.co.revsys.oddball.aggregator;

import uk.co.revsys.oddball.*;
import uk.co.revsys.oddball.bins.*;

public class SummaryDefinitionNotLoadedException extends Exception{

    public SummaryDefinitionNotLoadedException(String message) {
        super("Summary Definition Not Loaded " + message);
    }

    public SummaryDefinitionNotLoadedException(String message, Throwable cause) {
        super("Summary Definition Not Loaded " + message, cause);
    }

}
