package com.example.kafkapilot.streams;


import demo.avro.MyTask;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppKafkaStreamConfig {
    private final KafkaProperties kafkaProperties;
    private final KafkaTopics kafkaTopics;

    @Bean
    public KafkaTemplate<String, MyTask> kafkaTemplate() {
        return new KafkaTemplate<>(this.producerFactory());
    }

    @Bean
    public <U,V> ProducerFactory<U, V> producerFactory() {
        final var config = kafkaProperties.buildProducerProperties(null);
        var factory = new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new KafkaAvroSerializer());
        return (ProducerFactory<U, V>) factory;
    }
}
