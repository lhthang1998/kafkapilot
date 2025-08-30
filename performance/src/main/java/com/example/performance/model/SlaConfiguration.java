package com.example.performance.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaConfiguration {
    private Integer p95;
    private Integer p99;
    private Integer p9995;
}
