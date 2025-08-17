package com.example.kafkapilot.controller;


import com.example.kafkapilot.streams.KafkaTopics;
import demo.avro.MyTask;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController("/kafka")
@RequiredArgsConstructor
public class TopicController {
    private final KafkaTemplate kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    @PostMapping("/send")
    public ResponseEntity sendMessage() {
        kafkaTemplate.send(kafkaTopics.getMyTask(), UUID.randomUUID().toString(), MyTask.newBuilder().setName("a").setTime(System.currentTimeMillis()).build());
        return ResponseEntity.ok(null);
    }
}
