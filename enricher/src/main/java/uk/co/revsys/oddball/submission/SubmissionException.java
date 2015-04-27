package uk.co.revsys.oddball.submission;

public class SubmissionException extends Exception{

    public SubmissionException(String message) {
        super(message);
    }

    public SubmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubmissionException(Throwable cause) {
        super(cause);
    }

}
