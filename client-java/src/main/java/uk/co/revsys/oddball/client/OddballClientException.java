package uk.co.revsys.oddball.client;

public class OddballClientException extends Exception{

    public OddballClientException(String message) {
        super(message);
    }

    public OddballClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public OddballClientException(Throwable cause) {
        super(cause);
    }

}
