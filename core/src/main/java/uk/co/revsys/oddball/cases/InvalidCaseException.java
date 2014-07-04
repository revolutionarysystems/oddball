package uk.co.revsys.oddball.cases;

public class InvalidCaseException extends Exception{

    public InvalidCaseException(String message) {
        super(message);
    }

    public InvalidCaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
