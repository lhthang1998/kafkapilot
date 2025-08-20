package com.example.performance.integrations;

import com.example.performance.interfaces.ReconciliationKeyCompositor;
import com.example.performance.utils.CacheUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

@Getter
public abstract class BaseReceiver<K, V, RV>{
    protected final Cache<K,V> inputCache = Caffeine.newBuilder().build();

    protected final Cache<K,V> expectedOutputCache = Caffeine.newBuilder().build();

    protected final Cache<K,K> rawKeyMap = Caffeine.newBuilder().build();

    protected final Cache<String,RV> responseCache = Caffeine.newBuilder().build();

    protected final Cache<K,String> okCache = Caffeine.newBuilder().build();

    protected final Cache<K,String> koCache = Caffeine.newBuilder().build();

    protected final Cache<K,Long> failedRequestCache = Caffeine.newBuilder().build();

    protected final Cache<K,Long> serviceLevelFailedRequestCache = Caffeine.newBuilder().build();

    protected final Cache<K,String> errorTypeCache = Caffeine.newBuilder().build();

    protected final ConcurrentLinkedQueue<K> outputRecords = new ConcurrentLinkedQueue<>();

    protected final Cache<K, String> scenarioMap = Caffeine.newBuilder().build();

    protected ReconciliationKeyCompositor<K> reconciliationKeyCompositor;

    protected K reconciliationKey;

    protected Function<K, String> keyToStringMapper;

    protected Function<Object, K> toKeyHandler = key -> (K) key;

    public void trackOkRecord(K key, String timeframe) { this.okCache.put(key, timeframe); }
    public void trackKoRecord(K key, String timeframe) { this.okCache.put(key, timeframe); }

    public void trackFailedRequest(K key, long timeframe) { this.failedRequestCache.put(key, timeframe); }

    public K compositeKey(Object key) {
        return this.reconciliationKeyCompositor.compose(this.reconciliationKey, this.toKeyHandler.apply(key));
    }

    public void start() {};
    public void stop() {};

    public Map<String, Object> getMapString(Cache cache) {
        var result = new ConcurrentHashMap<String, Object>();
        cache.asMap().forEach((k,v)->result.put(getKey(k), CacheUtils.transformValue(k, v)));
        return result;
    }

    protected String getKey(Object k) {
        String key;
        if (Objects.nonNull(keyToStringMapper)) {
            key = keyToStringMapper.apply((K) k);
        } else {
            key = CacheUtils.transformKey(k);
        }
        return key;
    }

    protected RV getResponseCache(K key) {
        return responseCache.get(getKey(key), k -> null);
    }

    protected void addResponseCache(K key, RV response) { responseCache.put(getKey(key), response); }

    protected void invalidateResponseCache(K key) { responseCache.invalidate(getKey(key)); }
}
