package com.example.performance;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.config.ExecutionConfiguration;
import com.example.performance.integrations.BaseReceiver;
import com.example.performance.integrations.BaseSender;
import com.example.performance.model.TestPlanLoader;
import com.example.performance.simulation.ResultController;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.PopulationBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scala.Function0;
import scala.runtime.BoxedUnit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

@Slf4j
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

    protected Map<String, AbstractScenario> getScenarioMap() {
        return Map.of();
    }

    public void before() {
        this.start();
    }

    public void after() {
        try {
            getResultController().reconcile();
        } catch (Exception e) {
            log.error("Error when reconciling result", e);
        }
        this.stop();
    }

    protected void start() {
        getReceivers().forEach(BaseReceiver::start);
        getSenders().forEach(BaseSender::start);
    }

    protected void stop() {
        getReceivers().forEach(BaseReceiver::stop);
        getSenders().forEach(BaseSender::stop);
    }

    protected ResultController getResultController() {
        return new ResultController();
    }

    protected TestPlanLoader getTestPlanLoader() {
        try {
            return new TestPlanLoader<>(ExecutionConfiguration.class);
        } catch (Exception e) {
            log.error("Loading test plan loader causes error", e);
            throw new RuntimeException(e);
        }
    }

    protected List<PopulationBuilder> getScenarios() {
        var scenarioMap = getScenarioMap();
        var totalTps = config.getTotalTps();
        var duration = config.getDuration();

        return getTestPlanLoader().getTestPlan().getPlans()
                .stream()
                .flatMap(plan -> plan.getScenarios()
                        .stream().filter(s -> scenarioMap.containsKey(s.getName()))
                        .map(scenario -> scenarioMap.get(scenario.getName()).getScenario().injectOpen(
                                generateInjection(scenario.getTps(), plan.getTotalReferenceTps(), totalTps, duration)
                        )))
                .collect(Collectors.toList());
    }

    protected List<OpenInjectionStep> generateInjection(Double scenarioTps, Double totalReferenceTps, Double totalTps, Duration duration) {
        var injectionSteps = new ArrayList<OpenInjectionStep>();
        injectionSteps.add(constantUsersPerSec((scenarioTps / totalReferenceTps) * totalTps).during(duration));
        return injectionSteps;
    }
}
