package uk.co.revsys.oddball.aggregator;

import uk.co.revsys.oddball.*;
import uk.co.revsys.oddball.bins.*;

public class EventNotCreatedException extends Exception{

    public EventNotCreatedException(String eventString) {
        super("Event could not be created from " + eventString);
    }

    public EventNotCreatedException(String eventString, Throwable cause) {
        super("Event could not be created from " + eventString, cause);
    }

}
