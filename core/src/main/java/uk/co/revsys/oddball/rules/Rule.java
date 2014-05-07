/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.rules;

import uk.co.revsys.oddball.cases.Case;

/**
 *
 * @author Andrew
 */
public interface Rule {
    
    Assessment apply(Case aCase);
    
}
