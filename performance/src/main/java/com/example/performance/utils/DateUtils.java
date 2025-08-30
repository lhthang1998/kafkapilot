package com.example.performance.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DateUtils {
    private static final String DATETIME_FORMAT_PATTERN = "dd-MM-yyyy HH:mm:ss.SSS";

    public static String convertUnixToDateTime(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(DATETIME_FORMAT_PATTERN));
    }
}
