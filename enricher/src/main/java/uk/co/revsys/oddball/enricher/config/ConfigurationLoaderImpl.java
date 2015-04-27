package uk.co.revsys.oddball.enricher.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

public class ConfigurationLoaderImpl implements ConfigurationLoader {

    private ResourceRepository resourceRepository;
    
    private Map<String, JSONObject> configCache = new HashMap<String, JSONObject>();

    public ConfigurationLoaderImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public JSONObject loadConfiguration(String key) throws ConfigurationLoadException {
        if(configCache.containsKey(key)){
            return configCache.get(key);
        }
        try {
            Resource resource = new Resource(key);
            InputStream resourceStream = resourceRepository.read(resource);
            String config = IOUtils.toString(resourceStream);
            resourceStream.close();
            JSONObject json = new JSONObject(config);
            configCache.put(key, json);
            return json;
        } catch (IOException ex) {
            throw new ConfigurationLoadException(ex);
        }
    }
    
    public void clearCache(){
        configCache.clear();
    }

}
