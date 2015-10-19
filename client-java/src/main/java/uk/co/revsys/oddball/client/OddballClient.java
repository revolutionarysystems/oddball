package uk.co.revsys.oddball.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
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

    public Assessment applyRuleSet(ApplyRuleSetRequest request) throws OddballClientException {
        try {
            HttpRequest httpRequest = new HttpRequest(url + "/" + request.getRuleSet());
            if (username != null && !username.isEmpty()) {
                httpRequest.setCredentials(new BasicAuthCredentials(username, password));
            }
            httpRequest.setMethod(HttpMethod.GET);
            Map parameters = new HashMap();
            parameters.put("case", request.getBody());
            if (request.getInboundTransformer() != null) {
                parameters.put("inboundTransformer", request.getInboundTransformer());
            }
            if (request.getProcessor() != null) {
                parameters.put("processor", request.getProcessor());
            }
            httpRequest.setParameters(parameters);
            HttpResponse response = httpClient.invoke(httpRequest);
            if (response.getStatusCode() == 200) {
                InputStream responseStream = response.getInputStream();
                String responseText = "";
                if (responseStream != null) {
                    responseText = IOUtils.toString(responseStream);
                    responseStream.close();
                }
                return new Assessment(responseText);
            } else {
                InputStream responseStream = response.getInputStream();
                String responseText = "";
                if (responseStream != null) {
                    responseText = IOUtils.toString(responseStream);
                    responseStream.close();
                }
                throw new OddballClientException("Status: " + response.getStatusCode() + " - " + responseText);
            }
        } catch (IOException ex) {
            throw new OddballClientException(ex);
        }
    }

}
