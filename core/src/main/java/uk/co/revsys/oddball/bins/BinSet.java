/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.bins;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public interface BinSet {
    
    public void addBin(Bin rule);
    
    public Map<String, Bin> getBins();
    
    public String getBinType();

    public void setBinType(String ruleType);
    
    public String getName();

    public void setName(String name);

    public Collection listBinLabels();
    

}
