package com.example.performance.kafka;

import com.example.performance.actions.AsyncEvent;
import com.example.performance.details.KafkaMessageDetails;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class KeyOnlyKafkaConsumer<K,V> extends ConcreteKafkaConsumer<K, V>{
    public KeyOnlyKafkaConsumer(Properties kafkaProperties, List<String> topics) {
        super(kafkaProperties, topics);
    }

    public KeyOnlyKafkaConsumer(Properties kafkaProperties, List<String> topics, boolean useRecordCreationTimestamp) {
        super(kafkaProperties, topics, useRecordCreationTimestamp);
    }

    @Override
    protected void retainRecord(ConsumerRecord<K, V> record) {
        var key = record.key();
        addResponseCache(key, record.timestamp());
    }

    @Override
    public Optional<AsyncEvent<KafkaMessageDetails<K>>> findAsyncEventForKey(K key) {
        var resp = Optional.ofNullable(getResponseCache(key)).map(timestamp -> new AsyncEvent<>(timestamp, new KafkaMessageDetails<>(key)));
        if (resp.isPresent()) {
            invalidateResponseCache(key);
        }
        return resp;
    }
}
