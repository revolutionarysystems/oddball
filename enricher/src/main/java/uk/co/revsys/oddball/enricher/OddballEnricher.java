package uk.co.revsys.oddball.enricher;

import org.json.JSONObject;
import uk.co.revsys.enricher.Enricher;
import uk.co.revsys.enricher.EnrichmentException;
import uk.co.revsys.enricher.config.ConfigurationParseException;
import uk.co.revsys.oddball.enricher.config.ConfigurationLoadException;
import uk.co.revsys.oddball.submission.OddballSubmission;
import uk.co.revsys.oddball.submission.SubmissionException;

public class OddballEnricher {

    private EnricherFactory enricherFactory;
    private OddballSubmission preEnrichmentSubmission;
    private OddballSubmission postEnrichmentSubmission;

    public OddballEnricher(EnricherFactory enricherFactory, OddballSubmission preEnrichmentSubmission, OddballSubmission postEnrichmentSubmission) {
        this.enricherFactory = enricherFactory;
        this.preEnrichmentSubmission = preEnrichmentSubmission;
        this.postEnrichmentSubmission = postEnrichmentSubmission;
    }
    
    public JSONObject enrich(String key, JSONObject data) throws SubmissionException, EnrichmentException, ConfigurationLoadException, ConfigurationParseException{
        preEnrichmentSubmission.submit(data);
        Enricher enricher = enricherFactory.getEnricher(key);
        JSONObject result = enricher.enrich(data);
        postEnrichmentSubmission.submit(result);
        return result;
    }
}
