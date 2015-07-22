package uk.co.revsys.oddball.consumer.kinesis;

import com.amazonaws.services.kinesis.model.Record;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.cases.StringCase;

public class KinesisRecordProcessor extends uk.co.revsys.kinesis.KinesisRecordProcessor {

    private final Oddball oddball;
    private final Map<String, List<String>> ruleSets;

    static final Logger LOG = LoggerFactory.getLogger("oddball");

    public KinesisRecordProcessor(Oddball oddball, Map<String, List<String>> ruleSets) {
        this.oddball = oddball;
        LOG.debug("ruleSets = " + ruleSets.toString());
        this.ruleSets = ruleSets;
    }

    private String extractProperty(String propertyName, String caseString) {
        String quotedName = "\"" + propertyName + "\"";
        int location = caseString.indexOf(quotedName);
        String a = caseString.substring(location + quotedName.length());
        String b = a.substring(a.indexOf("\"") + 1);
        String c = b.substring(0, b.indexOf("\""));
        return c;
    }

    private String extractPropertyName(String ruleString) {
        return ruleString.substring(ruleString.indexOf("{") + 1, ruleString.indexOf("}"));
    }

    private Map splitRuleSetName(String extendedRuleSetName) {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] separated = extendedRuleSetName.split(":");
        result.put("ruleSet", separated[0]);
        if (separated.length > 1) {
            result.put("inboundTransformer", separated[1]);
        }
        if (separated.length > 2) {
            result.put("processor", separated[2]);
        }
        return result;
    }

    @Override
    protected void processRecord(Record record) throws Exception {
        String data = decodeData(record.getData());
        if (data.equals("")) {
            LOG.error("Blank data");
        } else {
            String partitionKey = record.getPartitionKey();
            if(partitionKey.contains("::")){
                partitionKey = partitionKey.substring(partitionKey.indexOf("::")+2);
            }
            LOG.info(record.getSequenceNumber() + ", " + partitionKey + ", " + data);
            List<String> recordRuleSets = ruleSets.get(partitionKey);
            if (recordRuleSets == null) {
                LOG.error("No rule sets found for " + partitionKey);
            } else {
                LOG.debug("Rule sets found for " + partitionKey + " = " + recordRuleSets);
                for (String ruleSetName : recordRuleSets) {
                    Map<String, String> splitRuleSetName = this.splitRuleSetName(ruleSetName);
                    String ruleSet = splitRuleSetName.get("ruleSet");
                    String inboundTransformer = splitRuleSetName.get("inboundTransformer");
                    String processor = splitRuleSetName.get("processor");

                    if (ruleSet.indexOf("{") >= 0) {
                        String placeholder = extractPropertyName(ruleSet);
                        String replacement = extractProperty(placeholder, data);
                        ruleSet = ruleSet.replace("{" + placeholder + "}", replacement);
                    }

                    if (inboundTransformer != null && inboundTransformer.indexOf("{") >= 0) {
                        String placeholder = extractPropertyName(inboundTransformer);
                        String replacement = extractProperty(placeholder, data);
                        inboundTransformer = inboundTransformer.replace("{" + placeholder + "}", replacement);
                    }
                    if (processor != null && processor.indexOf("{") >= 0) {
                        String placeholder = extractPropertyName(processor);
                        String replacement = extractProperty(placeholder, data);
                        processor = processor.replace("{" + placeholder + "}", replacement);
                    }
                    
                    LOG.debug("Assessing " + ruleSet);
                    try {
                        oddball.assessCase(ruleSet, inboundTransformer, processor, new StringCase(data));
                    } catch (Throwable t) {
                        LOG.warn("Caught throwable attempting ruleSet: " + ruleSet);
                        LOG.debug("throwable", t);
                    }
                }

            }

        }
    }

}
