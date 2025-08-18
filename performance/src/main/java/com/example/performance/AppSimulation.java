package com.example.performance;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class AppSimulation extends Simulation {

    // Define the HTTP protocol
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://www.example.com/") // Change to your target URL
            .acceptHeader("application/json");

    // Define the scenario
    ScenarioBuilder scn = scenario("MyScenario")
            .exec(http("Get Request")
                    .get("/api/endpoint") // Change to your target endpoint
                    .check(status().is(200)));

    // Set up the simulation
    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(10) // Simulate 10 users
                )
        ).protocols(httpProtocol);
    }
}
