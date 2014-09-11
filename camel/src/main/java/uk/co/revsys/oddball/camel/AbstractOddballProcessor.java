package uk.co.revsys.oddball.camel;

import uk.co.revsys.esb.component.HttpProxyProcessor;

public abstract class AbstractOddballProcessor extends HttpProxyProcessor {

    private String path;

    public AbstractOddballProcessor(String baseUrl) {
        super(baseUrl);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
