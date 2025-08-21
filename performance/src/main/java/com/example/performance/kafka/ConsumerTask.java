package com.example.performance.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public class ConsumerTask<K, V> implements Runnable {
    private final List<ConsumerRecord<K, V>> records;

    private volatile boolean stopped = false;
    private volatile boolean started = false;
    @Getter
    private volatile boolean finished = false;

    private final CompletableFuture<Long> completion = new CompletableFuture<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong currentOffset = new AtomicLong();
    private final Consumer<ConsumerRecord<K,V>> recordConsumer;

    public ConsumerTask(List<ConsumerRecord<K, V>> records, Consumer<ConsumerRecord<K,V>> recordConsumer) {
        this.records = records;
        this.recordConsumer = recordConsumer;
    }

    @Override
    public void run() {
        lock.lock();
        if (stopped) {
            return;
        }
        started = true;
        lock.unlock();
        for (ConsumerRecord<K,V> record : records) {
            if (stopped) {
                break;
            }
            recordConsumer.accept(record);
            currentOffset.set(record.offset() + 1);
        }
        finished = true;
        completion.complete(currentOffset.get());
    }

    public long getCurrentOffset() { return currentOffset.get(); }

    public void stop() {
        lock.lock();
        this.stopped = true;
        if (!started) {
            finished = true;
            completion.complete(currentOffset.get());
        }
        lock.unlock();
    }

    public long waitForCompletion() {
        try {
            return completion.get();
        } catch (InterruptedException | ExecutionException e) {
            return -1;
        }
    }

}
