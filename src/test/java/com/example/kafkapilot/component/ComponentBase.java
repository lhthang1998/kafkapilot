package com.example.kafkapilot.component;

import com.example.kafkapilot.KafkapilotApplication;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"local", "test"})
@SpringBootTest(classes = KafkapilotApplication.class)
@EnableConfigurationProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComponentBase {
}
