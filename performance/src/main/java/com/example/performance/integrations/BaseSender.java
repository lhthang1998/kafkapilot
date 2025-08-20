package com.example.performance.integrations;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public abstract class BaseSender<K, V> {
    protected Cache<K, V> inputCache;
    protected Cache<K, V> rawKeyMap;

    protected Cache<K, String> scenarioMap;

    public void start() {}

    public void stop() {}
}
