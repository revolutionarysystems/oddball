package uk.co.revsys.oddball.enricher.config;

import org.json.JSONObject;

public interface ConfigurationLoader {

    public JSONObject loadConfiguration(String key) throws ConfigurationLoadException;
    
}
