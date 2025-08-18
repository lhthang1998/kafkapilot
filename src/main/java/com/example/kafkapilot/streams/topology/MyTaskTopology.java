package com.example.kafkapilot.streams.topology;

import com.example.kafkapilot.streams.AppSerdes;
import com.example.kafkapilot.streams.KafkaTopics;
import com.example.kafkapilot.streams.processors.TaskProcessor;
import demo.avro.MyTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MyTaskTopology {
    private final KafkaTopics kafkaTopics;
    private final AppSerdes serdes;
    private final TaskProcessor taskProcessor;

    public KStream<String, MyTask> stream(StreamsBuilder builder) {
        var stream = builder.stream(kafkaTopics.getMyTask(), Consumed.with(Serdes.String(), serdes.myTaskSerde()));
        stream.process(taskProcessor)
                .to(kafkaTopics.getTaskStatus(), Produced.with(Serdes.String(), serdes.taskStatusSerde()));
        return stream;
    }
}
