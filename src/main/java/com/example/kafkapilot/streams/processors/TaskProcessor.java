package com.example.kafkapilot.streams.processors;

import demo.avro.MyTask;
import demo.avro.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskProcessor implements ProcessorSupplier<String, MyTask, String, TaskStatus> {
    @Override
    public Processor<String, MyTask, String, TaskStatus> get() {
        return new Processor<>() {
            private ProcessorContext<String, TaskStatus> processorContext;
            @Override
            public void init(ProcessorContext<String, TaskStatus> context) {
                processorContext = context;
            }

            @Override
            public void process(Record<String, MyTask> record) {
                if (record != null) {
                    var taskStatus = TaskStatus.newBuilder().setName(record.value().getName()).setStatus("COMPLETED").setTime(record.value().getTime()).build();
                    var newRecord = new Record<String, TaskStatus>(record.key(), taskStatus, record.timestamp());
                    processorContext.forward(newRecord);
                    processorContext.commit();
                }
            }

            @Override
            public void close() {
                Processor.super.close();
            }
        };
    }
}
