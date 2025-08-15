package com.example.kafkapilot.blackbox;

import com.example.kafkapilot.KafkapilotApplication;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"blackbox", "test"})
@SpringBootTest(classes = KafkapilotApplication.class)
@EnableConfigurationProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlackboxTest {
}
