package com.example.performance.kafka;

import com.example.performance.integrations.BaseReceiver;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
public abstract class BaseConreteKafkaConsumer<K, V, S extends Representable, RV> extends BaseReceiver<K, Long, RV> {
    protected final Properties kafkaProperties;
    protected final boolean useRecordCreationTimestamp;
    private final List<String> topics;
    private final Consumer<ConsumerRecord<K, V>> retainRecordConsumer = record -> retainRecord(record);
    private ConcurrentConsumer concurrentConsumer;

    public BaseConreteKafkaConsumer(Properties kafkaProperties, List<String> topics) {
        this(kafkaProperties, topics, false);
    }

    public BaseConreteKafkaConsumer(Properties kafkaProperties, List<String> topics, boolean useRecordCreationTimestamp) {
        this.topics = topics;
        this.kafkaProperties = kafkaProperties;
        this.useRecordCreationTimestamp = useRecordCreationTimestamp;
    }

    protected abstract void retainRecord(ConsumerRecord<K,V> record);

    public CompletableFuture<Void> startListener() {
        return CompletableFuture.runAsync(this::poll);
    }

    private void poll() {
        log.info("Polling thread on topics {}", topics);
        this.concurrentConsumer = new ConcurrentConsumer(
                new KafkaConsumer<K,V>(kafkaProperties),
                topics,
                retainRecordConsumer,
                getPollingDuration());
    }

    private Duration getPollingDuration() {
        return Duration.ofMillis(Long.parseLong(kafkaProperties.getProperty("poll.interval.ms", "10")));
    }

    public void stopListener() {
        if (concurrentConsumer != null) {
            concurrentConsumer.stop();
        }
    }

    public void start() { startListener(); };
    public void stop() { stopListener(); }
}
