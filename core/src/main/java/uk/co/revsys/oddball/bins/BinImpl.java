/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.bins;

import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public class BinImpl implements Bin {

    public BinImpl() {
    }
    
    public BinImpl(String binString, String label) {
        this.binString = binString;
        this.label = label;
    }
    
    private String binString;
    
    private String label;

    /**
     * @return the binString
     */
    @Override
    public String getBinString() {
        return binString;
    }

    /**
     * @param binString the binString to set
     */
    @Override
    public void setBinString(String binString, ResourceRepository resourceRepository){
        this.binString = binString;
    }

    /**
     * @return the label
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    @Override
    public void setLabel(String label) {
        this.label = label;
    }
    

    @Override
    public String toString(){
        return "Bin-"+label+":"+binString;
    }
}
