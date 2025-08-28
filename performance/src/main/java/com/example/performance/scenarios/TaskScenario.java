package com.example.performance.scenarios;

import com.example.performance.AbstractScenario;
import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.kafka.ConcreteKafkaConsumer;
import com.example.performance.kafka.ConcreteKafkaProducer;
import demo.avro.MyTask;
import io.gatling.javaapi.core.ScenarioBuilder;
import org.apache.avro.specific.SpecificRecord;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import static com.example.performance.actions.KafkaToKafkaActionDsl.kafkaToKafka;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class TaskScenario extends AbstractScenario {
    private final ConcreteKafkaConsumer<String, SpecificRecord> consumer;
    private final ConcreteKafkaProducer<String, MyTask> producer;
    public TaskScenario(String scenarioName, EnvironmentConfigLoader config, ConcreteKafkaConsumer<String, SpecificRecord> consumer, ConcreteKafkaProducer<String, MyTask> producer) {
        super(scenarioName, config);
        this.consumer = consumer;
        this.producer = producer;
    }

    @Override
    public ScenarioBuilder getScenario() {

        // Define the scenario
        return scenario(scenarioName)
                .exec(kafkaToKafka(String.class, MyTask.class)
                        .executor(getExecutor()) // Change to your target endpoint
                        .name(scenarioName)
                        .consumer(consumer)
                        .producer(producer)
                        .topic(config.getKafkaTopic("myTaskTopic"))
                        .keySupplier(() -> UUID.randomUUID().toString())
                        .valueSupplier(getValue())
                        .build());
    }

    private Function<String, MyTask> getValue() {
        return key -> MyTask.newBuilder().setName(key).setDescription(key).setTime(Instant.now().toEpochMilli()).build();
    }
}
