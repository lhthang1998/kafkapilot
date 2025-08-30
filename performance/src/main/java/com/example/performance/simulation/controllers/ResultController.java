package com.example.performance.simulation.controllers;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.config.ExecutionConfiguration;
import com.example.performance.integrations.BaseReceiver;
import com.example.performance.model.SlaConfiguration;
import com.example.performance.model.TestPlanLoader;
import com.example.performance.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.performance.utils.CsvUtils.writeToCsv;
import static com.example.performance.utils.DateUtils.convertUnixToDateTime;
import static com.example.performance.utils.FileUtils.getFilePath;


@Slf4j
public class ResultController<S extends BaseReceiver, E extends ExecutionConfiguration> {
    protected final ConcurrentHashMap<String, Long> inputMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> expectedOutputMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Integer> outputMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> okMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, String> koMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> failedRequestMap = new ConcurrentHashMap<>();

    protected final ConcurrentHashMap<String, Long> expectedOutputAmount = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> actualOutputAmount = new ConcurrentHashMap<>();

    protected final ConcurrentHashMap<String, Long> lostMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> extraMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Integer> dupMap = new ConcurrentHashMap<>();

    protected final List<S> receivers;
    protected final EnvironmentConfigLoader configLoader;
    protected final TestPlanLoader<E> testPlanLoader;
    protected final Map<String, SlaConfiguration> slaConfigurationMap;

    public ResultController(List<S> receivers, EnvironmentConfigLoader configLoader, TestPlanLoader<E> testPlanLoader) {
        this.receivers = receivers;
        this.configLoader = configLoader;
        this.testPlanLoader = testPlanLoader;
        this.slaConfigurationMap = new HashMap<>();
    }

    public ResultController(List<S> receivers, EnvironmentConfigLoader configLoader, TestPlanLoader<E> testPlanLoader, Map<String, SlaConfiguration> slaConfigurationMap) {
        this.receivers = receivers;
        this.configLoader = configLoader;
        this.testPlanLoader = testPlanLoader;
        this.slaConfigurationMap = slaConfigurationMap;
    }

    public void reconcile() {
        long startTime = System.currentTimeMillis();

        var baseReportDir = new File(Path.of("").toAbsolutePath() + File.separator + getBaseReportFilePath());
        if (!baseReportDir.exists()) {
            baseReportDir.mkdirs();
        }

        waitForPostTest();
        log.info("The test has been completed successfully. Collecting results");
        collectResults();

        log.info("Total Gatling Requests - " + getTotalRequests());
        log.info("Total OK Requests - " + okMap.size());
        log.info("Total KO Requests - " + koMap.size());
        log.info("Total Failed Requests - " + failedRequestMap.size());

        if (!extraMap.isEmpty()) {
            log.info("Messages that do not belong to this performance test - " + extraMap.size());

        }
        if (!dupMap.isEmpty()) {
            log.info("Messages that are duplicates - " + dupMap.size());
            dupMap.forEach((key, count) -> log.info("| " + key + " | " + count + " |"));
        }
        extractKoInformation();
        generateCsv();
        log.info("Reconciling results took " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private long getTotalRequests() {
        return inputMap.size() + failedRequestMap.size();
    }

    private void waitForPostTest() {
        long postTestWaitTime = testPlanLoader.getTestPlan().getPostTestMs() > 0 ? testPlanLoader.getTestPlan().getPostTestMs() : configLoader.getPostTestWaitTime();
        log.info("The test has been completed with uncorrelated records.\n Waiting " + postTestWaitTime + " ms for record to arrive");
        try {
            Thread.sleep(postTestWaitTime);
        } catch (InterruptedException e) {
            log.error("Error on post test step", e);
            Thread.currentThread().interrupt();
        }
    }

    private void collectResults() {
        receivers.stream().filter(rc -> BooleanUtils.isFalse(rc.getInputCache().asMap().isEmpty())
                && rc.getExpectedOutputCache().asMap().isEmpty()
                && rc.getFailedRequestCache().asMap().isEmpty())
                .forEach(rc -> {
                    inputMap.putAll(rc.getMapString(rc.getInputCache()));
                    failedRequestMap.putAll(rc.getMapString(rc.getFailedRequestCache()));
                    okMap.putAll(rc.getMapString(rc.getOkCache()));
                    koMap.putAll(rc.getMapString(rc.getKoCache()));

                    if (rc.getExpectedOutputCache().asMap().isEmpty()) {
                        expectedOutputMap.putAll(rc.getMapString(rc.getInputCache()));
                    } else {
                        expectedOutputMap.putAll(rc.getMapString(rc.getExpectedOutputCache()));
                    }
                    rc.getOutputRecords().forEach(key -> {
                        if (key instanceof String) {
                            outputMap.put(key.toString(), outputMap.getOrDefault(key.toString(), -1) + 1);
                        } else {
                            outputMap.put(key.toString(), outputMap.getOrDefault(key.toString(), -1) + 1);
                        }
                    });
                });
        interpretResults();
    }

    private void interpretResults() {
        expectedOutputMap.forEach((key, value) -> {
            if (expectedOutputAmount.containsKey(key)) {
                expectedOutputAmount.put(key, expectedOutputAmount.get(key) + 1);
            } else {
                expectedOutputAmount.put(key, 1L);
            }
        });
        outputMap.forEach((key, count) -> {
            if (count > 0) {
                dupMap.put(key, count);
            }
            if (actualOutputAmount.containsKey(key)) {
                actualOutputAmount.put(key, actualOutputAmount.get(key) + 1);
            } else {
                actualOutputAmount.put(key, 1L);
            }
        });
    }

    private void extractKoInformation() {
        log.info("Extracting KO Information: ");
        log.info("Requests that timed out - " + (koMap.size() - lostMap.size()));
        koMap.forEach((key, timestamp) -> {
            log.info("| " + key + " | " + timestamp);
        });

        log.info("Requests that are lost - " + lostMap.size());
        lostMap.forEach((key, timestamp) -> {
            log.info("| " + key + " | " + timestamp);
        });

        log.info("Requests that did not reach the component - " + failedRequestMap.size());
        failedRequestMap.forEach((key, timestamp) -> {
            log.info("| " + key + " | " + timestamp);
        });

        log.info("Requests that are duplicated - " + dupMap.size());
        dupMap.forEach((key, count) -> {
            log.info("| " + key + " | " + count);
        });
    }

    private void generateCsv() {
        log.info("Exporting data into CSV file");
        if (!inputMap.isEmpty()) {
            writeToCsv(
                    getFilePath("input_records.csv"),
                    new String[] {"id", "injected_time"},
                    inputMap.entrySet().parallelStream().map(e -> new String[] {e.getKey(), convertUnixToDateTime(e.getValue())}).collect(Collectors.toList()));
        }

        if (!koMap.isEmpty()) {
            writeToCsv(
                    getFilePath("ko_records.csv"),
                    new String[] {"id", "start_time", "end_time", "found"},
                    koMap.entrySet().parallelStream()
                            .map(e -> {
                                var key = e.getKey();
                                String[] timeFrame = e.getValue().split(",");
                                var expected = expectedOutputAmount.get(key);
                                var actual = actualOutputAmount.get(key);
                                var isFound = Objects.nonNull(expected) && expected.equals(actual);
                                return new String[] {key, convertUnixToDateTime(Long.valueOf(timeFrame[0])), convertUnixToDateTime(Long.valueOf(timeFrame[1])), String.valueOf(isFound)};
                            })
                            .collect(Collectors.toList()));
        }

        if (!dupMap.isEmpty()) {
            writeToCsv(
                    getFilePath("duplicate_records.csv"),
                    new String[] {"id", "dup_no"},
                    dupMap.entrySet().parallelStream().map(e -> new String[] {e.getKey(), String.valueOf(e.getValue())}).collect(Collectors.toList()));
        }
    }

    protected String getBaseReportFilePath() {
        return FileUtils.getBaseReportFilePath();
    }
}
