package com.example.performance.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

@Getter
@Slf4j
public class EnvironmentConfigLoader {
    private final Config environmentConfig;

    private final Config globalConfig;

    private final String environmentName;

    public EnvironmentConfigLoader(String configFile) {
        environmentName = getEnv();
        globalConfig = ConfigFactory.load(configFile).getConfig("gatling");
        environmentConfig = globalConfig.getConfig("environments." + environmentName);
    }

    public static String getEnv() {
        return System.getenv().getOrDefault("ENVIRONMENT", "local");
    }

    public Duration getDuration() {
        return Optional.ofNullable(Optional.ofNullable(System.getenv("DURATION"))
                .orElseGet(() -> environmentConfig.getString("duration")))
                .map(Long::parseLong)
                .map(Duration::ofSeconds)
                .orElseThrow();
    }

    public Long getPostTestWaitTime() {
        return Optional.ofNullable(System.getenv("POST_TEST_MS"))
                .map(Long::parseLong)
                .orElseGet(() -> Try.of(() -> environmentConfig.getLong("postTestMs")).getOrElse(0L));
    }

    public double getTotalTps() {
        return Double.parseDouble(Optional.ofNullable(System.getenv("TOTAL_TPS"))
                .map((item) -> item.isBlank() ? null : item).orElseGet(() -> environmentConfig.getString("totalTps")));
    }

    public String getKafkaTopic(String topic) {
        return environmentConfig.getConfig("kafka.topics").getString(topic);
    }

    public Properties getKafkaProducerProperties(String producerName) {
        var kafkaConfig = getKafkaProperties();
        environmentConfig.getConfig("kafka.properties.producers").getConfig(producerName)
                .entrySet()
                .forEach(entry -> kafkaConfig.put(entry.getKey(), entry.getValue().unwrapped()));
        return kafkaConfig;
    }

    public Properties getKafkaProducerProperties() {
        return getKafkaProducerProperties("default");
    }

    public Properties getKafkaConsumerProperties(String consumerName) {
        var kafkaConfig = getKafkaProperties();
        environmentConfig.getConfig("kafka.properties.consumers").getConfig(consumerName)
                .entrySet()
                .forEach(entry -> kafkaConfig.put(entry.getKey(), entry.getValue().unwrapped()));
        return kafkaConfig;
    }

    public Properties getKafkaConsumerProperties() {
        return getKafkaConsumerProperties("default");
    }

    private Properties getKafkaProperties() {
        var props = new Properties();
        environmentConfig.getConfig("kafka.properties")
                .withoutPath("producers")
                .withoutPath("consumers")
                .entrySet()
                .forEach(entry -> props.put(entry.getKey(), entry.getValue().unwrapped()));
        return props;
    }
}
