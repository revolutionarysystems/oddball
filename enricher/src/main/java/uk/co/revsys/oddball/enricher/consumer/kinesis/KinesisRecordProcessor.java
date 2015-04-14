package uk.co.revsys.oddball.enricher.consumer.kinesis;

import com.amazonaws.services.kinesis.model.Record;
import org.json.JSONObject;
import uk.co.revsys.enricher.EnrichmentException;
import uk.co.revsys.enricher.config.ConfigurationParseException;
import uk.co.revsys.oddball.enricher.OddballEnricher;
import uk.co.revsys.oddball.enricher.config.ConfigurationLoadException;
import uk.co.revsys.oddball.submission.SubmissionException;

public class KinesisRecordProcessor extends uk.co.revsys.kinesis.KinesisRecordProcessor {

    private OddballEnricher enricher;

    public KinesisRecordProcessor(OddballEnricher enricher) {
        this.enricher = enricher;
    }

    @Override
    protected void processRecord(Record record) throws EnrichmentException, ConfigurationLoadException, ConfigurationParseException, SubmissionException {
        String data = decodeData(record.getData());
        String partitionKey = record.getPartitionKey();
        JSONObject json = new JSONObject(data);
        enricher.enrich(partitionKey, json);
    }

}
