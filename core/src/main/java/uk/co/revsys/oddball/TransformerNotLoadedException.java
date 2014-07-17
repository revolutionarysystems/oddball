package uk.co.revsys.oddball;

import uk.co.revsys.oddball.bins.*;

public class TransformerNotLoadedException extends Exception{

    public TransformerNotLoadedException(String transformer) {
        super("Transformer " + transformer + " could not be loaded");
    }

    public TransformerNotLoadedException(String transformer, Throwable cause) {
        super("Transformer " + transformer + " could not be loaded", cause);
    }

}
