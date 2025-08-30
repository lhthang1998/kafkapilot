package com.example.performance;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.executors.GlobalExecutorFactory;
import io.gatling.javaapi.core.ScenarioBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public abstract class AbstractAsyncScenario extends AbstractScenario {

    public AbstractAsyncScenario(String scenarioName, EnvironmentConfigLoader config) {
        super(scenarioName, config);
    }

    public abstract ScenarioBuilder getScenario();

    public Executor getExecutor() {
        return GlobalExecutorFactory.getInstance().getExecutorForScenario();
    }

    public Executor getPostExcutor() {
        return GlobalExecutorFactory.getInstance().getExecutorForScenario();
    }
}
