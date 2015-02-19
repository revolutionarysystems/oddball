/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class SummaryDefinition {
        public SummaryDefinition(String summaryDefinitionName, ResourceRepository resourceRepository)  throws SummaryDefinitionNotLoadedException{
                this.name = summaryDefinitionName;
                definition = loadSummaryDefinition(this.name, resourceRepository);
        }

    private Map<String, Object> loadSummaryDefinition(String summaryDefinitionName, ResourceRepository resourceRepository) throws SummaryDefinitionNotLoadedException {
        try {
            Resource resource = new Resource("", summaryDefinitionName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder summaryDefinitionStringBuilder = new StringBuilder();
            for (String line : lines) {
                summaryDefinitionStringBuilder.append(line);
            }
            String summaryDefinitionString = summaryDefinitionStringBuilder.toString();
            Map<String, Object> summaryDefinitionMap = JSONUtil.json2map(summaryDefinitionString);
            //this.transformers.put(transformerName, transformerString);
            return summaryDefinitionMap;
        } catch (IOException ex) {
            throw new SummaryDefinitionNotLoadedException(summaryDefinitionName, ex);
        }
    }

        
    private final String name;
    private final Map<String, Object> definition;

    public Map<String, Object> getDefinition() {
        return definition;
    }
    
}
