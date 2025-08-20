package com.example.performance.utils;

public class CacheUtils {
    public static String transformKey(Object key) {
        if (key instanceof String) {
            return (String) key;
        }
        return key.toString();
    }

    public static Object transformValue(Object key, Object value) {
        if (key instanceof String && value instanceof String) {
            return (String) value;
        }
        return value;
    }
}
