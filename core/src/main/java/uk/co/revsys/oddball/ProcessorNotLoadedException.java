package uk.co.revsys.oddball;

public class ProcessorNotLoadedException extends Exception{

    public ProcessorNotLoadedException(String transformer) {
        super("Transformer " + transformer + " could not be loaded");
    }

    public ProcessorNotLoadedException(String transformer, Throwable cause) {
        super("Transformer " + transformer + " could not be loaded", cause);
    }

}
