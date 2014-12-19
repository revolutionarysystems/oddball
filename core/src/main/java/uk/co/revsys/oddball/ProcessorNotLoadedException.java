package uk.co.revsys.oddball;

public class ProcessorNotLoadedException extends Exception{

    public ProcessorNotLoadedException(String processor) {
        super("Processor " + processor + " could not be loaded");
    }

    public ProcessorNotLoadedException(String processor, Throwable cause) {
        super("Processor " + processor + " could not be loaded", cause);
    }

}
