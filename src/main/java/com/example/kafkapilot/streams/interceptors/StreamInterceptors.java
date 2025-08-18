package com.example.kafkapilot.streams.interceptors;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StreamInterceptors implements ProducerInterceptor<byte[], byte[]>, ConsumerInterceptor<byte[], byte[]> {
    private static final Logger log = LoggerFactory.getLogger(StreamInterceptors.class);
    private final StringDeserializer stringDeserializer = new StringDeserializer();

    @Override
    public ConsumerRecords<byte[], byte[]> onConsume(ConsumerRecords<byte[], byte[]> consumerRecords) {
        for (ConsumerRecord<byte[], byte[]> record : consumerRecords) {
            var key = stringDeserializer.deserialize(record.topic(), record.key());
            log.info("Started processing message from topic={} with key={}", record.topic(), key);
        }
        return consumerRecords;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {

    }

    @Override
    public ProducerRecord<byte[], byte[]> onSend(ProducerRecord<byte[], byte[]> producerRecord) {
        var key = stringDeserializer.deserialize(producerRecord.topic(), producerRecord.key());
        log.info("Published message to topic={} with key={}", producerRecord.topic(), key);
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
