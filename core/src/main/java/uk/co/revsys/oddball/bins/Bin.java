/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.bins;

import uk.co.revsys.oddball.bins.*;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.util.OddballException;
import uk.co.revsys.resource.repository.ResourceRepository;

/**
 *
 * @author Andrew
 */
public interface Bin {
    
    
    /**
     * @return the binString
     */
    public String getBinString();
    
    /**
     * @param binString the binString to set
     */
    public void setBinString(String binString, ResourceRepository resourceRepository)throws OddballException;

    /**
     * @return the label
     */
    public String getLabel();
    
    /**
     * @param label the label to set
     */
    public void setLabel(String label);

}
