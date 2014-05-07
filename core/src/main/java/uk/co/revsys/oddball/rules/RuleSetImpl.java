/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import java.util.HashSet;
import java.util.Set;
import uk.co.revsys.oddball.cases.Case;

/**
 *
 * @author Andrew
 */
public class RuleSetImpl implements RuleSet{
    
    public RuleSetImpl(String name) {
        this.name = name;
    }

    Set<Rule> rules = new HashSet<Rule>();

    String name;

    @Override
    public void addRule(Rule rule) {
        rules.add(rule);
    }

    @Override
    public Opinion assessCase(Case aCase) {
        Opinion op = new OpinionImpl();
        for (Rule rule: rules){
            Assessment as = rule.apply(aCase);
            op.incorporate(as);
        }
        return op;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public String getName() {
        return name;
    }
    
    
}
