package com.example.performance;

import com.example.performance.config.EnviromentConfigLoader;
import io.gatling.core.scenario.Simulation;

public abstract class BaseSimulation extends Simulation {
    protected final EnviromentConfigLoader config;

    protected BaseSimulation(EnviromentConfigLoader config) {
        this.config = config;
    }

    public EnviromentConfigLoader getConfig() {
        return config;
    }
}
