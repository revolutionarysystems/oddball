package uk.co.revsys.oddball;

public class ResourceNotUploadedException extends Exception{

    public ResourceNotUploadedException(String resource) {
        super("Resource " + resource + " could not be uploaded");
    }

    public ResourceNotUploadedException(String resource, Throwable cause) {
        super("Resource " + resource + " could not be uploaded", cause);
    }

}
