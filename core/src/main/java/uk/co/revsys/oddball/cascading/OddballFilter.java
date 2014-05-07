/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cascading;

import cascading.flow.FlowProcess;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;

/**
 *
 * @author Andrew
 */
public class OddballFilter implements Filter{

    @Override
    public boolean isRemove(FlowProcess fp, FilterCall fc) {
        String assessment = fc.getArguments().getString("assessment");
        if (assessment.equals("*odDball*")){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void prepare(FlowProcess fp, OperationCall oc) {
        
    }

    @Override
    public void flush(FlowProcess fp, OperationCall oc) {
        
    }

    @Override
    public void cleanup(FlowProcess fp, OperationCall oc) {
        
    }

    @Override
    public Fields getFieldDeclaration() {
        return Fields.ALL;
    }

    @Override
    public int getNumArgs() {
        return 5;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
    
}
