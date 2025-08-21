package com.example.performance.kafka;

import com.example.performance.actions.AsyncEvent;
import com.example.performance.details.KafkaMessageDetails;
import com.example.performance.integrations.BaseSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ConcreteKafkaProducer<K, V> extends BaseSender<K, Long> {
    private final KafkaProducer<K, V> producer;

    public ConcreteKafkaProducer(Properties properties) {
        super();
        this.producer = new KafkaProducer<>(properties);
    }

    public ConcreteKafkaProducer(Properties properties, String topic) {
        this(properties);
        this.producer.partitionsFor(topic).forEach(partitionInfo -> {

        });;
    }

    public Long publish(String topic, K key, V value) throws ExecutionException, InterruptedException {
        var record = new ProducerRecord<>(topic, key, value);
        var metadata = producer.send(record).get();
        var timestamp = metadata.timestamp();
        log.debug("Send kafka message with key={} to topic={}, timestamp={}", key, topic, timestamp);
        return timestamp;
    }

    public AsyncEvent<KafkaMessageDetails<K>> publicAndReturnAsyncEvent(Supplier<K> keysuppler, Function<K, V> valueSupplier, String inputTopic) {
        var key = keysuppler.get();
        try {
            var timeStamp = publish(inputTopic, key, valueSupplier.apply(key));
            return new AsyncEvent<>(timeStamp, new KafkaMessageDetails<>(key));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
