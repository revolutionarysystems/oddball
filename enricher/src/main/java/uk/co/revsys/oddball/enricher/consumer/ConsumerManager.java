package uk.co.revsys.oddball.enricher.consumer;

import java.util.Map;

public class ConsumerManager {

    private final Map<String, Consumer> consumers;

    public ConsumerManager(Map<String, Consumer> consumers) {
        this.consumers = consumers;
    }

    public Map<String, Consumer> getConsumers() {
        return consumers;
    }

    public Consumer getConsumer(String id) {
        return consumers.get(id);
    }

    public void run(String ids) {
        if (ids != null && !ids.isEmpty()) {
            if (ids.contains(",")) {
                for (String id : ids.split(",")) {
                    consumers.get(id).start();
                }
            } else {
                consumers.get(ids).start();
            }
        }
    }

    public void runAll() {
        for (Consumer consumer : consumers.values()) {
            consumer.start();
        }
    }
}
