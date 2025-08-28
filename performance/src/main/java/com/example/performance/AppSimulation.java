package com.example.performance;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.integrations.BaseReceiver;
import com.example.performance.integrations.BaseSender;
import com.example.performance.kafka.ConcreteKafkaProducer;
import com.example.performance.kafka.KeyOnlyKafkaConsumer;
import com.example.performance.scenarios.HttpBaseScenario;
import com.example.performance.scenarios.TaskScenario;
import demo.avro.MyTask;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.apache.avro.specific.SpecificRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.http.HttpDsl.http;

public class AppSimulation extends BaseSimulation {
    private final EnvironmentConfigLoader config  = new EnvironmentConfigLoader("application.conf");
    private final KeyOnlyKafkaConsumer<String, SpecificRecord> consumer;
    private final ConcreteKafkaProducer<String, MyTask> producer;
    // Define the HTTP protocol
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://www.example.com/") // Change to your target URL
            .acceptHeader("application/json");

    public AppSimulation() {
        this.consumer = new KeyOnlyKafkaConsumer<>(config.getKafkaConsumerProperties(), List.of(config.getKafkaTopic("outputTopic")));
        this.producer = new ConcreteKafkaProducer<>(config.getKafkaProducerProperties());
        var simulation = setUp(getScenarios()).protocols(httpProtocol);
        simulation.assertions(details("TaskScenario").successfulRequests().percent().is(100D));
    }

    @Override
    protected Map<String, AbstractScenario> getScenarioMap() {
        var scenarioMap = new HashMap<String, AbstractScenario>();
//        scenarioMap.put("ABC", new HttpBaseScenario("ABC", config));
        scenarioMap.put("TaskScenario", new TaskScenario("TaskScenario", config, consumer, producer));
        return scenarioMap;
    }

    @Override
    protected List<BaseReceiver> getReceivers() {
        return List.of(consumer);
    }

    @Override
    protected List<BaseSender> getSenders() {
        return List.of(producer);
    }
}
