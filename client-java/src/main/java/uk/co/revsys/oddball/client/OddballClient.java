package uk.co.revsys.oddball.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.revsys.utils.http.BasicAuthCredentials;
import uk.co.revsys.utils.http.HttpClient;
import uk.co.revsys.utils.http.HttpClientImpl;
import uk.co.revsys.utils.http.HttpMethod;
import uk.co.revsys.utils.http.HttpRequest;
import uk.co.revsys.utils.http.HttpResponse;

public class OddballClient {

    private HttpClient httpClient = new HttpClientImpl();

    private String url;
    private String username;
    private String password;

    public OddballClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Assessment applyRuleSet(ApplyRuleSetRequest request) throws OddballClientException, IOException {
        HttpRequest httpRequest = new HttpRequest(url + "/" + request.getRuleSet());
        if (username != null && !username.isEmpty()) {
            httpRequest.setCredentials(new BasicAuthCredentials(username, password));
        }
        httpRequest.setMethod(HttpMethod.GET);
        Map parameters = new HashMap();
        parameters.put("case", request.getBody());
        appendParameter(parameters, "inboundTransformer", request.getInboundTransformer());
        appendParameter(parameters, "processor", request.getProcessor());
        httpRequest.setParameters(parameters);
        HttpResponse response = httpClient.invoke(httpRequest);
        String responseText = readResponse(response);
        return new Assessment(responseText);
    }

    public Case findCase(CaseQuery query) throws OddballClientException, IOException {
        List<Case> cases = findCases(query);
        if(cases.isEmpty()){
            return null;
        }
        return cases.get(0);
    }

    public List<Case> findCases(CaseQuery query) throws OddballClientException, IOException {
        HttpRequest httpRequest = new HttpRequest(url + "/" + query.getRuleSet() + "/case");
        if (username != null && !username.isEmpty()) {
            httpRequest.setCredentials(new BasicAuthCredentials(username, password));
        }
        httpRequest.setMethod(HttpMethod.GET);
        Map parameters = new HashMap();
        appendParameter(parameters, "transformer", query.getTransformer());
        appendParameter(parameters, "processor", query.getProcessor());
        appendParameter(parameters, "account", query.getOwner());
        appendParameter(parameters, "series", query.getSeries());
        appendParameter(parameters, "selector", query.getSelector());
        appendParameter(parameters, "ownerProperty", query.getOwnerProperty());
        appendParameter(parameters, "query", query.getQuery());
        appendParameter(parameters, "forEach", query.getForEach());
        httpRequest.setParameters(parameters);
        HttpResponse response = httpClient.invoke(httpRequest);
        String responseText = readResponse(response);
        JSONArray json = new JSONArray(responseText);
        List<Case> cases = new LinkedList<Case>();
        for(int i=0; i<json.length(); i++){
            JSONObject caseJson = json.getJSONObject(i);
            cases.add(new Case(caseJson.toString()));
        }
        return cases;
    }
    
    private void appendParameter(Map parameters, String key, Object value){
        if(value!=null){
            parameters.put(key, value);
        }
    }

    private String readResponse(HttpResponse response) throws OddballClientException, IOException {
        if (response.getStatusCode() == 200) {
            InputStream responseStream = response.getInputStream();
            String responseText = "";
            if (responseStream != null) {
                responseText = IOUtils.toString(responseStream);
                responseStream.close();
            }
            return responseText;
        } else {
            InputStream responseStream = response.getInputStream();
            String responseText = "";
            if (responseStream != null) {
                responseText = IOUtils.toString(responseStream);
                responseStream.close();
            }
            throw new OddballClientException("Status: " + response.getStatusCode() + " - " + responseText);
        }
    }

}
