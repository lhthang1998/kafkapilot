package com.example.kafkapilot.controller;


import com.example.kafkapilot.streams.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/kafka")
@RequiredArgsConstructor
public class TopicController {
    private final KafkaTemplate kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    @PostMapping("/send")
    public ResponseEntity sendMessage() {
        return ResponseEntity.ok(kafkaTemplate.send(kafkaTopics.getMyTask(), "message"));
    }
}
