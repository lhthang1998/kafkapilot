package com.example.performance;

import com.example.performance.scenarios.HttpBaseScenario;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.HashMap;
import java.util.Map;

import static io.gatling.javaapi.http.HttpDsl.http;

public class AppSimulation extends BaseSimulation {

    // Define the HTTP protocol
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://www.example.com/") // Change to your target URL
            .acceptHeader("application/json");

    public AppSimulation() {
        var simulation = setUp(getScenarios()).protocols(httpProtocol);
    }

    @Override
    protected Map<String, AbstractScenario> getScenarioMap() {
        var scenarioMap = new HashMap<String, AbstractScenario>();
        scenarioMap.put("ABC", new HttpBaseScenario("ABC", config));
        return scenarioMap;
    }
}
