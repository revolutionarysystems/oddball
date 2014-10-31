package uk.co.revsys.oddball.rules;

public class InvalidTimePeriodException extends Exception{

    public InvalidTimePeriodException(String period) {
        super("Time Period " + period + " could not be understood");
    }

    public InvalidTimePeriodException(String period, Throwable cause) {
        super("Time Period " + period + " could not be understood", cause);
    }

}
