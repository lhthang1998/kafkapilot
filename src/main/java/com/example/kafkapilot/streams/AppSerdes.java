package com.example.kafkapilot.streams;


import demo.avro.MyTask;
import demo.avro.TaskStatus;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AppSerdes {
    private final KafkaProperties kafkaProperties;

    public Serde<MyTask> myTaskSerde() {
        SpecificAvroSerde<MyTask> serde = new SpecificAvroSerde<>();
        serde.configure(getAvroSerdeConfig(), false);
        return serde;
    }

    public Serde<TaskStatus> taskStatusSerde() {
        SpecificAvroSerde<TaskStatus> serde = new SpecificAvroSerde<>();
        serde.configure(getAvroSerdeConfig(), false);
        return serde;
    }

    public Map<String, String> getAvroSerdeConfig() {
        final var config = new HashMap<>(kafkaProperties.getProperties());
        return config;
    }
}
