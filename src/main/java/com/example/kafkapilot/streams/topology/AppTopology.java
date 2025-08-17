package com.example.kafkapilot.streams.topology;

import demo.avro.MyTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppTopology {
    private final MyTaskTopology myTaskTopology;

    @Bean
    public KStream<String, MyTask> buildTopology(StreamsBuilder builder) {
        return myTaskTopology.stream(builder);
    }
}
