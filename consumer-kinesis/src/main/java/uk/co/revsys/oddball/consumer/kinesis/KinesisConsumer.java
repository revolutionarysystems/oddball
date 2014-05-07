package uk.co.revsys.oddball.consumer.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import uk.co.revsys.oddball.consumer.Consumer;

public class KinesisConsumer extends Consumer{

    private final IRecordProcessorFactory recordProcessorFactory;
    private final KinesisClientLibConfiguration config;
    private final Worker worker;
    
    public KinesisConsumer(IRecordProcessorFactory recordProcessorFactory, KinesisClientLibConfiguration config) {
        this.recordProcessorFactory = recordProcessorFactory;
        this.config = config;
        this.worker = new Worker(recordProcessorFactory, config);
    }

    @Override
    public void run() {
        worker.run();
    }

}
