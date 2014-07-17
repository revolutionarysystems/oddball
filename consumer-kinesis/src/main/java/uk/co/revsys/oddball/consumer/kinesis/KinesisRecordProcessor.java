package uk.co.revsys.oddball.consumer.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.revsys.oddball.Oddball;
import uk.co.revsys.oddball.cases.StringCase;

public class KinesisRecordProcessor implements IRecordProcessor {

    private final Oddball oddball;
    private final Map<String, List<String>> ruleSets;

    static final Logger LOG = LoggerFactory.getLogger("oddball");
    private String kinesisShardId;

    private static long backoffTime = 3000L;
    private static int numRetries = 10;

    private static long checkpointInterval = 60000L;
    private long nextCheckpointTimeInMillis;

    private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

    public KinesisRecordProcessor(Oddball oddball, Map<String, List<String>> ruleSets) {
        this.oddball = oddball;
        LOG.debug("ruleSets = "+ruleSets.toString());
        this.ruleSets = ruleSets;
    }

    public static void setBackoffTime(long backoffTime) {
        KinesisRecordProcessor.backoffTime = backoffTime;
    }

    public static void setNumRetries(int numRetries) {
        KinesisRecordProcessor.numRetries = numRetries;
    }

    public static void setCheckpointInterval(long checkpointInterval) {
        KinesisRecordProcessor.checkpointInterval = checkpointInterval;
    }

    @Override
    public void initialize(String shardId) {
        System.out.println("Initializing record processor for shard: " + shardId);
        LOG.info("Initializing record processor for shard: " + shardId);
        this.kinesisShardId = shardId;
    }

    @Override
    public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
        System.out.println("Processing " + records.size() + " records from " + kinesisShardId);
        LOG.info("Processing " + records.size() + " records from " + kinesisShardId);

        processRecordsWithRetries(records);

        if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
            checkpoint(checkpointer);
            nextCheckpointTimeInMillis = System.currentTimeMillis() + checkpointInterval;
        }

    }

    private void processRecordsWithRetries(List<Record> records) {
        for (Record record : records) {
            boolean processedSuccessfully = false;
            String data = null;
            LOG.debug("Processing record");
            for (int i = 0; i < numRetries; i++) {
                try {
                    data = decoder.decode(record.getData()).toString();
                    String partitionKey = record.getPartitionKey();
                    LOG.info(record.getSequenceNumber() + ", " + partitionKey + ", " + data);
                    List<String> recordRuleSets = ruleSets.get(partitionKey);
                    if (recordRuleSets == null) {
                        LOG.error("No rule sets found for " + partitionKey);
                    } else {
                        LOG.debug("Rule sets found for " + partitionKey+" = "+recordRuleSets);
                        for(String ruleSet: recordRuleSets){
                            LOG.debug("Assessing " + ruleSet);
                            oddball.assessCase(ruleSet, new StringCase(data));
                        }
                        processedSuccessfully = true;
                    }
                    break;
                } catch (CharacterCodingException e) {
                    LOG.error("Malformed data: " + data, e);
                    break;
                } catch (Throwable t) {
                    LOG.warn("Caught throwable while processing record " + record, t);
                }

                try {
                    Thread.sleep(backoffTime);
                } catch (InterruptedException e) {
                    LOG.debug("Interrupted sleep", e);
                }
            }

            if (!processedSuccessfully) {
                LOG.error("Couldn't process record " + record + ". Skipping the record.");
            }
        }
    }

    @Override
    public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
        LOG.info("Shutting down record processor for shard: " + kinesisShardId);
        if (reason == ShutdownReason.TERMINATE) {
            checkpoint(checkpointer);
        }
    }

    private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        LOG.info("Checkpointing shard " + kinesisShardId);
        for (int i = 0; i < numRetries; i++) {
            try {
                checkpointer.checkpoint();
                break;
            } catch (ShutdownException se) {
                LOG.info("Caught shutdown exception, skipping checkpoint.", se);
                break;
            } catch (ThrottlingException e) {
                if (i >= (numRetries - 1)) {
                    LOG.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                    break;
                } else {
                    LOG.info("Transient issue when checkpointing - attempt " + (i + 1) + " of "
                            + numRetries, e);
                }
            } catch (InvalidStateException e) {
                LOG.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                break;
            }
            try {
                Thread.sleep(backoffTime);
            } catch (InterruptedException e) {
                LOG.debug("Interrupted sleep", e);
            }
        }
    }

}
