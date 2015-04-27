package uk.co.revsys.oddball.enricher.consumer.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import uk.co.revsys.oddball.enricher.OddballEnricher;

public class KinesisRecordProcessorFactory implements IRecordProcessorFactory {

    private OddballEnricher enricher;

    public KinesisRecordProcessorFactory(OddballEnricher enricher) {
        this.enricher = enricher;
    }

    @Override
    public IRecordProcessor createProcessor() {
        return new KinesisRecordProcessor(enricher);
    }

}
