package com.example.performance.utils;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
public class CsvUtils {
    public static final void writeToCsv(String filePath, String[] headers, List<String[]> records) {
        records.add(0, headers);
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeAll(records);
        } catch (IOException e) {
            log.error("Failed to write csv file to path {}", filePath, e);
        }
    }
}
