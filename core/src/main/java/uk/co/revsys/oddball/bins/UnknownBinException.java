package uk.co.revsys.oddball.bins;

public class UnknownBinException extends Exception{

    public UnknownBinException(String bin) {
        super("Unknown bin: " + bin);
    }

}
