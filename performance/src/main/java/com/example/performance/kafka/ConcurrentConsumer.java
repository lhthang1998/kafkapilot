package com.example.performance.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class ConcurrentConsumer<K, V> implements Runnable, ConsumerRebalanceListener {
    private final KafkaConsumer<K, V> consumer;
    private final ExecutorService executorService = Executors.newFixedThreadPool(12);
    private final ExecutorService consumerExecutor = Executors.newFixedThreadPool(1);
    private final Map<TopicPartition, ConsumerTask> activeTasks = new HashMap<>();
    private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private long lastCommitTime = System.currentTimeMillis();
    private final List<String> topics;

    private final Duration pollDuration;
    private final Consumer<ConsumerRecord<K,V>> retainRecordConsumer;

    public ConcurrentConsumer(KafkaConsumer<K, V> consumer, List<String> topics, Consumer<ConsumerRecord<K,V>> retainRecordConsumer, Duration pollDuration) {
        this.consumer = consumer;
        this.topics = topics;
        this.retainRecordConsumer = retainRecordConsumer;
        this.pollDuration = pollDuration;
        consumerExecutor.submit(this);
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics, this);
            while (!stopped.get()) {
                ConsumerRecords<K, V> records = consumer.poll(pollDuration);
                handleFetchedRecords(records);
                checkActiveTasks();
                commitOffsets();
            }
        }  catch (WakeupException e) {
            if (!stopped.get()) {
                throw e;
            }
        } finally {
            consumer.close();
        }
    }

    private void handleFetchedRecords(ConsumerRecords<K, V> records) {
        if (records.count() > 0) {
            var partitionsToPause = new ArrayList<TopicPartition>();
            records.partitions().forEach(partition -> {
                List<ConsumerRecord<K, V>> partitionRecords = records.records(partition);
                var task = new ConsumerTask<>(partitionRecords, retainRecordConsumer);
                partitionsToPause.add(partition);
                executorService.execute(task);
                activeTasks.put(partition, task);
            });
            consumer.pause(partitionsToPause);
        }
    }

    private void commitOffsets() {
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCommitTime > 5000) {
                if (!offsetsToCommit.isEmpty()) {
                    consumer.commitSync(offsetsToCommit);
                    offsetsToCommit.clear();
                }
            }
            lastCommitTime = currentTime;
        } catch (Exception e) {
            log.error("Failed to commit offsets", e);
        }
    }

    private void checkActiveTasks() {
        var finishedTasksPartions = new ArrayList<TopicPartition>();
        activeTasks.forEach((topicPartition, consumerTask) -> {
            if (consumerTask.isFinished()) {
                finishedTasksPartions.add(topicPartition);
            }
            long offset = consumerTask.getCurrentOffset();
            if (offset > 0) {
                offsetsToCommit.put(topicPartition, new OffsetAndMetadata(offset));
            }
        });
        finishedTasksPartions.forEach(activeTasks::remove);
        consumer.resume(finishedTasksPartions);
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> collection) {
        Map<TopicPartition, ConsumerTask> revokedTasks = new HashMap<>();
        for (TopicPartition topicPartition : collection) {
            var task = activeTasks.remove(topicPartition);
            if (task != null) {
                task.stop();
                revokedTasks.put(topicPartition, task);
            }
        }
        revokedTasks.forEach((partition, task) -> {
            long offset = task.waitForCompletion();
            if (offset > 0) {
                offsetsToCommit.put(partition, new OffsetAndMetadata(offset));
            }
        });
        Map<TopicPartition, OffsetAndMetadata> revokedPartitionOffsets = new HashMap<>();
        collection.forEach(topicPartition -> {
            var offset = offsetsToCommit.remove(topicPartition);
            if (offset != null) {
                revokedPartitionOffsets.put(topicPartition, offset);
            }
        });
        try {
            consumer.commitSync(revokedPartitionOffsets);
        } catch (Exception e) {
            log.warn("Failed to commit offsets for revoked paritions");
        }
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> collection) {
        consumer.resume(collection);
    }

    public void stop() {
        stopped.set(true);
        consumer.wakeup();
    }
}
