package uk.co.revsys.oddball.identifier;

import uk.co.revsys.oddball.aggregator.*;

public class IdentificationSchemeNotLoadedException extends Exception{

    public IdentificationSchemeNotLoadedException(String message) {
        super("Identification Scheme Not Loaded " + message);
    }

    public IdentificationSchemeNotLoadedException(String message, Throwable cause) {
        super("Identification Scheme Not Loaded " + message, cause);
    }

}
