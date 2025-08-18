package com.example.kafkapilot.streams;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("app.kafka.topics")
public class KafkaTopics {
    private String myTask;
    private String taskStatus;
}
