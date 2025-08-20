package com.example.performance.model;

import com.example.performance.config.ExecutionConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
@Getter
public class TestPlanLoader<T extends ExecutionConfiguration> {
    private static final String DEFAULT_TEST_PLAN = "default-test-plan.yml";
    private Class<T> testPlanType;

    private T testPlan;

    public TestPlanLoader(Class<T> testPlanType) {
        this.testPlanType = testPlanType;
        this.loadTestPlan();
    }

    private void loadTestPlan() {
        Yaml yaml = new Yaml();
        try {
            getOrDefaultTestPlan(yaml);
        } catch (FileNotFoundException e) {
            log.error("Error test plan file not found", e);
        }
    }


    private void getOrDefaultTestPlan(Yaml parser) throws FileNotFoundException {
        InputStream inputStream = null;
        if ("lower".equalsIgnoreCase(System.getenv().getOrDefault("ENVIRONMENT", "local"))) {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_TEST_PLAN);
            this.testPlan = parser.loadAs(inputStream, this.testPlanType);
        } else {
            var file = new FileInputStream(System.getenv("TEST_PLAN_PATH"));
            this.testPlan = parser.loadAs(file, this.testPlanType);
        }
    }
}
