package uk.co.revsys.oddball.service.rest;

public class BadResponseException extends Exception{

    public BadResponseException(String message) {
        super("Bad Response - object count = " + message);
    }

    public BadResponseException(String message, Throwable cause) {
        super("Bad Response - object count = " + message, cause);
    }

}
