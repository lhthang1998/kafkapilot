package com.example.performance.config;

import com.example.performance.model.Plan;
import lombok.Data;

import java.util.List;

@Data
public class ExecutionConfiguration {
    protected int tps;
    protected int duration;
    protected long postTestMs;
    protected int rampDuration;
    protected List<Plan> plans;
}
