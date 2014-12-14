package uk.co.revsys.oddball;

public class FilterException extends Exception{

    public FilterException(String transformer) {
        super("Transformer " + transformer + " could not be loaded");
    }

    public FilterException(String transformer, Throwable cause) {
        super("Transformer " + transformer + " could not be loaded", cause);
    }

}
