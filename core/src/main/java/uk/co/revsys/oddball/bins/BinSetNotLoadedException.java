package uk.co.revsys.oddball.bins;

public class BinSetNotLoadedException extends Exception{

    public BinSetNotLoadedException(String binSet) {
        super("BinSet " + binSet + " could not be loaded");
    }

    public BinSetNotLoadedException(String binSet, Throwable cause) {
        super("BinSet " + binSet + " could not be loaded", cause);
    }

}
