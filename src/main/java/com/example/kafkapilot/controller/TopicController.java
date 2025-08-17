package com.example.kafkapilot.controller;


import com.example.kafkapilot.streams.KafkaTopics;
import demo.avro.MyTask;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class TopicController {
    private final KafkaTemplate kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage() {
        var key = UUID.randomUUID().toString();
        var myTask = MyTask.newBuilder().setName("Task-" + key).setDescription("Description here").setTime(System.currentTimeMillis()).build();
        kafkaTemplate.send(kafkaTopics.getMyTask(), key, myTask);
        return ResponseEntity.ok(null);
    }

}
