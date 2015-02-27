package uk.co.revsys.oddball;

public class ResourceNotLoadedException extends Exception{

    public ResourceNotLoadedException(String resource) {
        super("Resource " + resource + " could not be loaded");
    }

    public ResourceNotLoadedException(String resource, Throwable cause) {
        super("Resource " + resource + " could not be loaded", cause);
    }

}
