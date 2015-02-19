/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.aggregator.*;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class IdentificationScheme {
        public IdentificationScheme(String identificationSchemeName, ResourceRepository resourceRepository)  throws IdentificationSchemeNotLoadedException{
                this.name = identificationSchemeName;
                scheme = loadIdentificationScheme(this.name, resourceRepository);
        }

    private ArrayList<Map<String, Object>> loadIdentificationScheme(String identificationSchemeName, ResourceRepository resourceRepository) throws IdentificationSchemeNotLoadedException {
        try {
            Resource resource = new Resource("", identificationSchemeName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder summaryDefinitionStringBuilder = new StringBuilder();
            for (String line : lines) {
                summaryDefinitionStringBuilder.append(line);
            }
            String summaryDefinitionString = summaryDefinitionStringBuilder.toString();
            ArrayList<Map<String, Object>> identificationSchemeSet = (ArrayList<Map<String, Object>>)JSONUtil.json2map(summaryDefinitionString).get("identificationScheme");
            //this.transformers.put(transformerName, transformerString);
            return identificationSchemeSet;
        } catch (IOException ex) {
            throw new IdentificationSchemeNotLoadedException(identificationSchemeName, ex);
        }
    }

        
    private final String name;
    private final ArrayList<Map<String, Object>> scheme;

    public ArrayList<Map<String, Object>> getScheme() {
        return scheme;
    }
    
}
