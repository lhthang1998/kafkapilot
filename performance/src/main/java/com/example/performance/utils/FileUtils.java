package com.example.performance.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public class FileUtils {
    public static String getFilePath(String fileName) {
        return Path.of("").toAbsolutePath() + File.separator + getBaseReportFilePath() + fileName;
    }

    public static String getBaseReportFilePath() {
        var path = "performance/build/reports/results/reconciliation/";
        return "local".equals(System.getenv().getOrDefault("ENVIRONMENT", "local".toLowerCase())) ? path : String.format("performance/%s" + path);
    }
}
