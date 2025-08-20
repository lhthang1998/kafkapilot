package com.example.performance.model;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Plan {
    private String name;
    private String totalTps;
    private Double ratio;
    private Double tps;

    private List<Scenario> scenarios;

    public Double getTotalReferenceTps() {
        return this.scenarios.stream().map(Scenario::getTps).filter(Objects::nonNull).reduce(0.0, Double::sum);
    }
}
