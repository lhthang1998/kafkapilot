package com.example.performance.actions;

import com.example.performance.details.KafkaMessageDetails;
import com.example.performance.kafka.ConcreteKafkaConsumer;
import com.example.performance.kafka.ConcreteKafkaProducer;
import com.typesafe.scalalogging.Logger;
import io.gatling.core.action.Action;
import io.gatling.core.session.Session;
import io.gatling.core.stats.StatsEngine;
import io.gatling.core.structure.ScenarioContext;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@SuperBuilder
public class KafkaToKafkaActionDsl<IK, IV> extends IntegrationActionDsl {
    protected final String name;

    protected final String topic;

    protected final ConcreteKafkaProducer<IK, IV> producer;
    protected final ConcreteKafkaConsumer<IK, SpecificRecord> consumer;
    protected final List<ConcreteKafkaConsumer<IK, SpecificRecord>> kafkaConsumers;

    protected final Function<KafkaMessageDetails<IK>, Optional<AsyncEvent<KafkaMessageDetails<IK>>>> expectedEventFetcher;

    protected final Supplier<IK> keySupplier;
    protected final Function<IK, IV> valueSupplier;
    protected Long maxTimeoutMs = Long.valueOf(System.getenv().getOrDefault("MAX_TIMEOUT_MS", "30000"));

    @Override
    public Action build(ScenarioContext ctx, Action action) {
        var inputCache = Objects.nonNull(consumer)? consumer.getInputCache() : null;
        if (!kafkaConsumers.isEmpty() && Objects.nonNull(inputCache)) {
            producer.setRawKeyMap(kafkaConsumers.get(0).getRawKeyMap());
        }
        producer.setInputCache(inputCache);
        return new KafkaToKafkaAction(ctx.coreComponents().statsEngine(), action);
    }

    public static <IK, IV> KafkaToKafkaActionDslBuilder<IK, IV, ?, ?> kafkaToKafka(Class<IK> inputKeyClass, Class<IV> outputValue) {
        return KafkaToKafkaActionDsl.builder();
    }

    public class KafkaToKafkaAction extends IntegrationAction<KafkaMessageDetails<IK>, KafkaMessageDetails<IK>> {

        public KafkaToKafkaAction(StatsEngine statsEngine, Action action) {
            super(KafkaToKafkaActionDsl.this.name, statsEngine, action, executor);
        }


        @Override
        protected void trackOkRecord(KafkaMessageDetails<IK> details, String timeframe) {
            if (!kafkaConsumers.isEmpty()) {
                kafkaConsumers.get(0).trackOkRecord(details.getKey(), timeframe);
            } else {
                consumer.trackOkRecord(details.getKey(), timeframe);
            }
        }

        @Override
        protected void trackKoRecord(KafkaMessageDetails<IK> details, String timeframe) {
            if (!kafkaConsumers.isEmpty()) {
                kafkaConsumers.get(0).trackKoRecord(details.getKey(), timeframe);
            } else {
                consumer.trackKoRecord(details.getKey(), timeframe);
            }
        }

        @Override
        protected AsyncEvent<KafkaMessageDetails<IK>> initRequest(Session session) {
            var sendTimestamp = Instant.now().toEpochMilli();
            var key = keySupplier.get();
            var inputCache = Objects.nonNull(consumer)? consumer.getInputCache() : null;
            var scenarioCache = Objects.nonNull(consumer)? consumer.getScenarioMap() : null;
            if (!CollectionUtils.isEmpty(kafkaConsumers) && Objects.nonNull(inputCache)) {
                inputCache = kafkaConsumers.get(0).getInputCache();
                scenarioCache = kafkaConsumers.get(0).getScenarioMap();
            }
            try {
                var startTimestamp = producer.publish(topic, key, valueSupplier.apply(key));
                if (inputCache != null) {
                    inputCache.put(key, startTimestamp);
                    scenarioCache.put(key, name);
                }
                return new AsyncEvent<>(startTimestamp, new KafkaMessageDetails<>(key));
            } catch (Exception e) {
                consumer.trackFailedRequest(key, sendTimestamp);
                throw new RuntimeException("Unable to publish kafka message", e);
            }
        }

        @Override
        protected Optional<AsyncEvent<KafkaMessageDetails<IK>>> fetchExpectedResponse(KafkaMessageDetails<IK> details) {
            return expectedEventFetcher == null ? findAsyncEvent(details) : expectedEventFetcher.apply(details);
        }

        private Optional<AsyncEvent<KafkaMessageDetails<IK>>> findAsyncEvent(KafkaMessageDetails<IK> details) {
            return consumer.findAsyncEventForKey(details.getKey());
        }

        @Override
        protected Long getTimeout() {
            return maxTimeoutMs;
        }

    }
}
