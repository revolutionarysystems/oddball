package uk.co.revsys.oddball.submission;

import org.json.JSONObject;

public class KinesisSubmission implements OddballSubmission{

    @Override
    public void submit(JSONObject data) throws SubmissionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
