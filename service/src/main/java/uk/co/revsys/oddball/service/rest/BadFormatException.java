package uk.co.revsys.oddball.service.rest;

public class BadFormatException extends Exception{

    public BadFormatException(String message) {
        super("Bad Format specifier " + message);
    }

    public BadFormatException(String message, Throwable cause) {
        super("Bad Format specifier " + message, cause);
    }

}
