package com.example.kafkapilot.component;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DemoComponentTest extends ComponentBase {
    @Test
    void testRedis() {
        var x = 1 + 2;
        assertEquals(x, 3);
    }
}
