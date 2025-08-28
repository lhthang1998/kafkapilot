package com.example.performance.kafka;

import com.example.performance.details.KafkaMessageDetails;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

@Slf4j
public abstract class ConcreteKafkaConsumer<K,V> extends BaseConcreteKafkaConsumer<K, V, KafkaMessageDetails<K>, Long> {
    public ConcreteKafkaConsumer(Properties kafkaProperties, List<String> topics) {
        super(kafkaProperties, topics, true);
    }

    public ConcreteKafkaConsumer(Properties kafkaProperties, List<String> topics, boolean useRecordCreationTimestamp) {
        super(kafkaProperties, topics, useRecordCreationTimestamp);
    }

}
