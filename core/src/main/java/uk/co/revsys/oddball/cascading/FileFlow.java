/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.cascading;

import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.local.LocalFlowConnector;
import cascading.operation.Filter;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tap.Tap;
import java.util.Properties;


/**
 *
 * @author Andrew
 */
public class FileFlow {

    private FlowDef flowDef;
//    private FlowDef flowDef2;
    
    public FileFlow(Tap inTap, Tap outTap, Tap otherTap) {
        Pipe copyPipe = new Pipe( "copy" );
        Pipe oddballPipe = new Each( "oddball", new OddballFilter() );
        System.out.println(oddballPipe.getName());
        flowDef = FlowDef.flowDef().addSource( copyPipe, inTap ).addSource(oddballPipe, inTap).addTailSink( copyPipe, outTap ).addTailSink(oddballPipe, otherTap);
    }
    

    public void connect(){
        FlowConnector flowConnector = new LocalFlowConnector(new Properties());
        flowConnector.connect(flowDef).complete();
    }
    
}
