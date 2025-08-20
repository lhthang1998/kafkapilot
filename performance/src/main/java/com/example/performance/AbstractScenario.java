package com.example.performance;

import com.example.performance.config.EnvironmentConfigLoader;
import io.gatling.javaapi.core.ScenarioBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractScenario {
    protected final String scenarioName;
    protected final EnvironmentConfigLoader config;

    public abstract ScenarioBuilder getScenario();
}
