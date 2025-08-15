package com.example.kafkapilot.blackbox;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DemoBlackboxTest extends BlackboxTest {

    @Test
    void testBlackbox() {
        WebClient client = WebClient.builder().baseUrl("http://localhost:8080/actuator/health").build();
        var resp = client.get().retrieve();

        assertEquals(200, Objects.requireNonNull(resp.toBodilessEntity().block()).getStatusCode().value());
    }

    @Test
    void testWebClient() {
        WebTestClient testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8080/actuator/health")
                .build();
        var resp = testClient.get();
        resp.exchange().expectStatus().is2xxSuccessful();
    }
}
