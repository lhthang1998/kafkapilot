package com.example.performance.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public class EnviromentConfigLoader {
    @Getter
    private final Config environmentConfig;

    @Getter
    private final Config globalConfig;

    @Getter
    private final String environmentName;

    public EnviromentConfigLoader(String configFile) {
        environmentName = getEnv();
        globalConfig = ConfigFactory.load(configFile).getConfig("gatling");
        environmentConfig = globalConfig.getConfig("environments." + environmentName);
    }

    public static final String getEnv() {
        return System.getenv().getOrDefault("ENVIRONMENT", "local");
    }

    public Duration getDuration() {
        return Optional.ofNullable(Optional.ofNullable(System.getenv("DURATION"))
                .orElseGet(() -> environmentConfig.getString("duration")))
                .map(Long::parseLong)
                .map(Duration::ofSeconds)
                .orElseThrow();
    }

    public double getTotalTps() {
        return Double.parseDouble(Optional.ofNullable(System.getenv("TOTAL_TPS"))
                .map((item) -> item.isBlank() ? null : item).orElseGet(() -> environmentConfig.getString("totalTps")));
    }
}
