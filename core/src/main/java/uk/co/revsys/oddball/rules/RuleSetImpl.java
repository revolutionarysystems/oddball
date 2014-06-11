/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import uk.co.revsys.oddball.cases.Case;

/**
 *
 * @author Andrew
 */
public class RuleSetImpl implements RuleSet{
    

    public RuleSetImpl() {
        setPersist(new MongoDBHelper("oddball-persist"));
    }
    
    public RuleSetImpl(String name) {
        setPersist(new MongoDBHelper(name+"-persist"));
        this.name = name;
    }

    Set<Rule> rules = new HashSet<Rule>();

    private String name;
    private String ruleType;
   private MongoDBHelper persist;


    @Override
    public void addRule(Rule rule) {
        rules.add(rule);
    }

    @Override
    public Opinion assessCase(Case aCase, String key, String ruleSetStr) throws IOException{
        
        Opinion op = new OpinionImpl();

        
        for (Rule rule: rules){
            Assessment as = rule.apply(aCase, this, key);
            op.incorporate(as);
        }
        if (op.getTags().isEmpty()){
            op.getTags().add("*odDball*");
        }
        return op;
    }

   
    public Set<Rule> getRules() {
        return rules;
    }

    public String getName() {
        return name;
    }
    
    /**
     * @return the ruleType
     */
    public String getRuleType() {
        return ruleType;
    }

    /**
     * @param ruleType the ruleType to set
     */
    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
    
    public MongoDBHelper getPersist() {
        return persist;
    }

    public void setPersist(MongoDBHelper persist) {
        this.persist = persist;
    }

}
