package com.example.performance.scenarios;

import com.example.performance.AbstractScenario;
import com.example.performance.config.EnvironmentConfigLoader;
import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class HttpBaseScenario extends AbstractScenario {
    public HttpBaseScenario(String scenarioName, EnvironmentConfigLoader config) {
        super(scenarioName, config);
    }

    @Override
    public ScenarioBuilder getScenario() {

        // Define the scenario
        return scenario("MyScenario")
                .exec(http("Get Request")
                        .get("") // Change to your target endpoint
                        .check(status().is(200)));
    }
}
