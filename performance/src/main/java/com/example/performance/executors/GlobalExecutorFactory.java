package com.example.performance.executors;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.model.TestPlanLoader;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.max;
import static java.lang.Math.round;

@Slf4j
public class GlobalExecutorFactory implements ExecutorFactory {
    public static final String TOTAL_ASYNC_THREADS = "totalAsyncThreads";
    public static final int DEFAULT_TOTAL_ASYNC_THREADS = 20;
    private static final String MIN_ASYNC_THREADS_PER_SCENARIO = "minAsyncThreadsPerScenario";
    private static final int DEFAULT_MIN_ASYNC_THREADS_PER_SCENARIO = 2;
    private static final int DEFAULT_ASYNC_THREADS_PER_SCENARIO = 2;
    private final int asyncThreadPerScenario;
    private final List<ExecutorService> allExecutors = new ArrayList<>();

    private static GlobalExecutorFactory instance;
    private final EnvironmentConfigLoader configLoader;


    private final Map<String, ExecutorService> preparedExecutors = new HashMap<>();

    public GlobalExecutorFactory(EnvironmentConfigLoader configLoader, int asyncThreadPerScenario) {
        this.asyncThreadPerScenario = asyncThreadPerScenario;
        this.configLoader = configLoader;
    }

    public static void init() {
        instance = new GlobalExecutorFactory(null, DEFAULT_ASYNC_THREADS_PER_SCENARIO);
    }

    public static void init(EnvironmentConfigLoader configLoader) {
        var asyncThreads  = Try.of(() -> configLoader.getGlobalConfig().getInt("asyncThreadsPerScenario")).toJavaOptional().orElse(DEFAULT_ASYNC_THREADS_PER_SCENARIO);
        instance = new GlobalExecutorFactory(configLoader, asyncThreads);
    }

    public static void init(EnvironmentConfigLoader configLoader, TestPlanLoader testPlanLoader) {
        init(configLoader);
        var totalAsyncThreads = Try.of(() -> configLoader.getGlobalConfig().getInt(TOTAL_ASYNC_THREADS)).toJavaOptional().orElse(null);
        if (Objects.nonNull(totalAsyncThreads)) {
            var optPlan = Optional.ofNullable(testPlanLoader).map(pl -> pl.getTestPlan().getPlans()).map(s-> s.get(0));
            optPlan.ifPresentOrElse(plan -> {
                var planScenarios = plan.getScenarios();
                final var byTps = Objects.nonNull(planScenarios.get(0).getTps());
                final int minAsyncThreads = Try.of(() -> configLoader.getGlobalConfig().getInt(MIN_ASYNC_THREADS_PER_SCENARIO)).toJavaOptional().orElse(DEFAULT_MIN_ASYNC_THREADS_PER_SCENARIO);
                planScenarios.forEach(scenario -> {
                    double ratio = scenario.getRatio();
                    if (byTps) {
                        ratio = scenario.getTps() / plan.getTotalReferenceTps();
                    }
                    var calculatedThread = (int) max(round(ratio * totalAsyncThreads), minAsyncThreads);
                    instance.preparedExecutors.put(scenario.getName(), Executors.newFixedThreadPool(calculatedThread));
                });
            }, () -> log.warn("Can not get test plan loader. Skipping executor services initialization"));
        }
    }

    public static GlobalExecutorFactory getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    @Override
    public ExecutorService getSingleThreadExecutor() {
        var singleThreadExecutor = Executors.newSingleThreadExecutor();
        allExecutors.add(singleThreadExecutor);
        return singleThreadExecutor;
    }

    @Override
    public ExecutorService getFixedThreadPoolExecutor(int size) {
        var fixedThreadPool = Executors.newFixedThreadPool(size);
        allExecutors.add(fixedThreadPool);
        return fixedThreadPool;
    }

    @Override
    public ExecutorService getExecutorForScenario() {
        var fixedThreadPool = Executors.newFixedThreadPool(asyncThreadPerScenario);
        allExecutors.add(fixedThreadPool);
        return fixedThreadPool;
    }

    @Override
    public ExecutorService getExecutorForScenario(String name) {
        return Optional.ofNullable(preparedExecutors.get(name)).orElseGet(this::getExecutorForScenario);
    }

    @Override
    public ExecutorService getSharedExecutor() {
        return getSharedExecutor(TOTAL_ASYNC_THREADS);
    }

    @Override
    public ExecutorService getSharedExecutor(String name) {
        if (!instance.preparedExecutors.containsKey(name)) {
            int totalAsyncThreads = Try.of(() -> this.configLoader.getGlobalConfig().getInt(name)).toJavaOptional().orElse(DEFAULT_TOTAL_ASYNC_THREADS);
            var sharedExecutor = Executors.newFixedThreadPool(totalAsyncThreads);
            instance.preparedExecutors.put(name, sharedExecutor);
        }
        return instance.preparedExecutors.get(name);
    }
}
