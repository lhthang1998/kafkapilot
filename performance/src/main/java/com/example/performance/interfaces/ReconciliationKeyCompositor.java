package com.example.performance.interfaces;

public interface ReconciliationKeyCompositor<T> {
    T compose(T reconciliationKey, T originalKey);
}
