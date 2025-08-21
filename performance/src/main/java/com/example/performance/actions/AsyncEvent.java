package com.example.performance.actions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AsyncEvent<V> {
    private Long timestamp;
    private V details;
    private boolean postValidations;

    public AsyncEvent(Long timestamp, V details) {
        this.timestamp = timestamp;
        this.details = details;
    }
}
