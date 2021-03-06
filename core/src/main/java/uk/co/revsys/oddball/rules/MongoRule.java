/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class MongoRule extends RuleImpl {

    public MongoRule() {
    }

    public MongoRule(String ruleString, String label) {
        super(ruleString, label);
    }

    @Override
    public Assessment apply(Case aCase, RuleSet ruleSet, String key) {
        String content = ((MapCase) aCase).getContent();
        if (!ruleString.contains(".json") && testRule(key, ruleString, ((MongoRuleSet) ruleSet).getAssess())) {
            return new Assessment(content, ruleString, label);
        } else {
            return new Assessment(content, ruleString, null);
        }
    }

    private boolean testRule(String id, String ruleString, MongoDBHelper helper) {
        boolean hit = helper.testCase(ruleString, id);
        return hit;
    }

    @Override
    public boolean testOneOffRule(Case aCase, MongoDBHelper helper) throws IOException{
        String caseId = helper.insertCase(aCase.getContent());
        boolean hit = helper.testCase(ruleString, caseId);
        return hit;
    }
    
    

    
    /**
     * @param ruleString the ruleString to set
     * @param resourceRepository
     */
    @Override
    public void setRuleString(String ruleString, ResourceRepository resourceRepository) throws RuleNotLoadedException {
        if (ruleString.contains(".json") && (resourceRepository != null)) {
            InputStream inputStream = null;
            try {
                Resource resource = new Resource("", ruleString);
                inputStream = resourceRepository.read(resource);
                List<String> rulesLines = IOUtils.readLines(inputStream);
                StringBuilder ruleStringBuffer = new StringBuilder();
                for (String line : rulesLines) {
                    ruleStringBuffer.append(line);
                }   
                this.ruleString = ruleStringBuffer.toString();
            } catch (IOException ex) {
                throw new RuleNotLoadedException(ruleString, ex);
            } finally {
                try {
                    if (inputStream!=null){
                        inputStream.close();
                    }
                } catch (IOException ex) {
                }
            }
        } else {
            this.ruleString = ruleString;
        }
    }

}
