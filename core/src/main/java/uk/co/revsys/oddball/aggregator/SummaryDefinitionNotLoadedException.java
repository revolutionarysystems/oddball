package uk.co.revsys.oddball.aggregator;

public class SummaryDefinitionNotLoadedException extends Exception{

    public SummaryDefinitionNotLoadedException(String message) {
        super("Summary Definition Not Loaded " + message);
    }

    public SummaryDefinitionNotLoadedException(String message, Throwable cause) {
        super("Summary Definition Not Loaded " + message, cause);
    }

}
