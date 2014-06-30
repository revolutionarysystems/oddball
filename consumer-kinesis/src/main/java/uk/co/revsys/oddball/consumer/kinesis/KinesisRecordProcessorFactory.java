package uk.co.revsys.oddball.consumer.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import java.util.List;
import java.util.Map;
import uk.co.revsys.oddball.Oddball;

public class KinesisRecordProcessorFactory implements IRecordProcessorFactory{

    private final Oddball oddball;
    private final Map<String, List<String>> ruleSets;

    public KinesisRecordProcessorFactory(Oddball oddball, Map<String, List<String>> ruleSets) {
        this.oddball = oddball;
        this.ruleSets = ruleSets;
    }
    
    @Override
    public IRecordProcessor createProcessor() {
        return new KinesisRecordProcessor(oddball, ruleSets);
    }

}
