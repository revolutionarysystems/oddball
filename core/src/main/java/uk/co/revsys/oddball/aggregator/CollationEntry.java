/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class CollationEntry {

    public CollationEntry() {
        this.count = 0;
        this.subCollations = new HashMap<String, CollationEntry>();
    }

    @Override
    public String toString() {
        return "CollationEntry{" + "count=" + getCount() + ", subCollations=" + subCollations + '}';
    }
    
    private int count;
    private Map<String, CollationEntry> subCollations;

    public void incrementCount(){
        this.count++;
    }

    /**
     * @return the subCollations
     */
    public Map<String, CollationEntry> getSubCollations() {
        return subCollations;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
    
    
}
