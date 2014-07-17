package uk.co.revsys.oddball.consumer.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;

public class KinesisRecordProcessorFactory implements IRecordProcessorFactory{

    private final Oddball oddball;
    private final Map<String, List<String>> ruleSets;

    static final Logger LOG = LoggerFactory.getLogger("oddball");
    
    public KinesisRecordProcessorFactory(Oddball oddball, Map<String, List<String>> ruleSets) {
        this.oddball = oddball;
        LOG.debug("ruleSets = "+ruleSets.toString());
        this.ruleSets = ruleSets;
    }
    
    @Override
    public IRecordProcessor createProcessor() {
        return new KinesisRecordProcessor(oddball, ruleSets);
    }

}
