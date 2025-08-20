package com.example.performance.simulation;

import com.example.performance.config.EnvironmentConfigLoader;
import com.example.performance.config.ExecutionConfiguration;
import com.example.performance.integrations.BaseReceiver;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class ResultController<S extends BaseReceiver, E extends ExecutionConfiguration> {
    protected final ConcurrentHashMap<String, Long> inputMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> expectedOutputMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> outputMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> okMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> koMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> failedRequestMap = new ConcurrentHashMap<>();


    protected final ConcurrentHashMap<String, Long> lostMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> extraMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Long> dupMap = new ConcurrentHashMap<>();

//    protected final List<S> receivers;
//    protected final EnvironmentConfigLoader configLoader;
//    protected L

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
    }

    private void collectResults() {
    }

    private void extractKoInformation() {
    }

    private void generateCsv() {
    }

    protected String getBaseReportFilePath() {
        return "result/conciliation";
    }
}
