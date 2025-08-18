package com.example.performance;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.integrations.BaseReceiver;
import com.example.performance.integrations.BaseSender;
import io.gatling.core.scenario.Simulation;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class BaseSimulation extends Simulation {
    protected final EnvironmentConfigLoader config;

    public BaseSimulation() {
        this(new EnvironmentConfigLoader("application.conf"));
    }
    public BaseSimulation(EnvironmentConfigLoader config) {
        this.config = config;
    }

    protected List<BaseReceiver> getReceivers() { return List.of(); };


    protected List<BaseSender> getSenders() { return List.of(); };
}
