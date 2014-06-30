package uk.co.revsys.oddball.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class PropertyListMapFactoryBean extends AbstractFactoryBean<HashMap<String, List<String>>> implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    private String location;

    @Override
    public void setResourceLoader(ResourceLoader rl) {
        this.resourceLoader = rl;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public Class<?> getObjectType() {
        return HashMap.class;
    }

    @Override
    protected HashMap<String, List<String>> createInstance() throws Exception {
        HashMap<String, List<String>> propertyMap = new HashMap<String, List<String>>();
        Resource resource = resourceLoader.getResource(location);
        for (String property : IOUtils.readLines(resource.getInputStream())) {
            String[] tokens = property.split("=");
            String propertyName = tokens[0].trim();
            String propertyValueString = tokens[1].trim();
            List<String> propertyValue = new ArrayList<String>();
            for(String propertyValueToken: propertyValueString.split(",")){
                propertyValue.add(propertyValueToken.trim());
            }
            propertyMap.put(propertyName, propertyValue);
        }
        return propertyMap;
    }
}
