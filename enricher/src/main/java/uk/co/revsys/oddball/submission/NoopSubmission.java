package uk.co.revsys.oddball.submission;

import org.json.JSONObject;

public class NoopSubmission implements OddballSubmission{

    @Override
    public void submit(JSONObject data) throws SubmissionException {
        
    }

}
