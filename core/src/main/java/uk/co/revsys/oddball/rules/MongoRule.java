/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.util.OddballException;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class MongoRule implements Rule {

    public MongoRule() {
    }
    
    public MongoRule(String ruleString, String label) {
        this.ruleString = ruleString;
        this.label = label;
    }
    
    private String ruleString;
    
    private String label;

    @Override
    public Assessment apply(Case aCase, RuleSet ruleSet, String key) {
        String content = ((StringCase)aCase).getContent();
        if (!ruleString.contains(".json") && testRule(key, ruleString, ((MongoRuleSet)ruleSet).getHelper())){
            return new Assessment(content, ruleString, label);
        } else {
            return new Assessment(content, ruleString, null);
        }
    }

    private boolean testRule(String id, String ruleString, MongoDBHelper helper){
        boolean hit = helper.testCase(ruleString, id);
        return hit;
    }
    
    /**
     * @return the ruleString
     */
    public String getRuleString() {
        return ruleString;
    }

    /**
     * @param ruleString the ruleString to set
     */
    public void setRuleString(String ruleString, ResourceRepository resourceRepository)throws OddballException {
        if (ruleString.contains(".json") && (resourceRepository!=null)){
            try {
                Resource resource = new Resource("", ruleString);
                InputStream inputStream = resourceRepository.read(resource);
                List<String> rulesLines = IOUtils.readLines(inputStream);
                StringBuffer ruleStringBuffer = new StringBuffer();
                for (String line: rulesLines){
                    ruleStringBuffer.append(line);
                }
                this.ruleString = ruleStringBuffer.toString();
                        }
            catch (java.io.FileNotFoundException e){
                throw new OddballException("No Rule named "+ruleString+" in repository");
            }
            catch (Exception e){
                e.printStackTrace();
                throw new OddballException("Rules could not be loaded");
            }

        } else {
            this.ruleString = ruleString;
        }
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
    

    @Override
    public String toString(){
        return "Rule-"+label+":"+ruleString;
    }
}
