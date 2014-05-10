/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

/**
 *
 * @author Andrew
 */
public interface Opinion {
    
    public void incorporate(Assessment as);
    public String getLabel();
    public void setLabel(String label); 
}
