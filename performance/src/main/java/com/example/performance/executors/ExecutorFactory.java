package com.example.performance.executors;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory {
    ExecutorService getSingleThreadExecutor();
    ExecutorService getFixedThreadPoolExecutor(int size);
    ExecutorService getExecutorForScenario();
    ExecutorService getExecutorForScenario(String name);
    ExecutorService getSharedExecutor();
    ExecutorService getSharedExecutor(String name);
}
