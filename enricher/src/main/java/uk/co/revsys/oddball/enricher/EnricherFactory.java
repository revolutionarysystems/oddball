package uk.co.revsys.oddball.enricher;

import org.json.JSONObject;
import uk.co.revsys.enricher.Enricher;
import uk.co.revsys.enricher.config.ConfigurationParseException;
import uk.co.revsys.enricher.config.ConfigurationParser;
import uk.co.revsys.oddball.enricher.config.ConfigurationLoadException;
import uk.co.revsys.oddball.enricher.config.ConfigurationLoader;

public class EnricherFactory{

    private ConfigurationLoader configLoader;
    private ConfigurationParser configParser;
    
    public Enricher getEnricher(String key) throws ConfigurationLoadException, ConfigurationParseException{
        JSONObject config = configLoader.loadConfiguration(key);
        return configParser.parse(config);
    }

}
