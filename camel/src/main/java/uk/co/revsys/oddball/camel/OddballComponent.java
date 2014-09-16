package uk.co.revsys.oddball.camel;

import uk.co.revsys.esb.component.HttpProxyComponent;
import java.util.Map;

import org.apache.camel.Processor;

public class OddballComponent extends HttpProxyComponent{

    @Override
    protected void populateMappings(Map<String, Class<? extends Processor>> mappings) {
        mappings.put("retrieve", RetrieveProcessor.class);
        mappings.put("retrieveForSeries", RetrieveForSeriesProcessor.class);
        mappings.put("retrieveQuery", RetrieveQueryProcessor.class);
        mappings.put("insert", InsertProcessor.class);
    }
    
}
