package uk.co.revsys.oddball.submission;

import org.json.JSONObject;

public interface OddballSubmission {

    public void submit(JSONObject data) throws SubmissionException;
    
}
