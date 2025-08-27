package com.example.performance.actions;

import com.example.performance.kafka.Representable;
import io.gatling.core.action.Action;
import io.gatling.core.session.Session;
import io.gatling.core.stats.StatsEngine;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Getter
@Slf4j
public abstract class IntegrationAction<S extends Representable, T> extends AbstractAction<S, T> {
    private final ConcurrentLinkedQueue<Tuple2<AsyncEvent<S>, Session>> eventsQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Tuple3<AsyncEvent<S>, AsyncEvent<T>, Session>> postEventQueue = new ConcurrentLinkedQueue<>();

    public IntegrationAction(String name, StatsEngine statsEngine, Action action, Executor eventsExecutor) {
        this(name, statsEngine, action, eventsExecutor, eventsExecutor);
    }

    public IntegrationAction(String name, StatsEngine statsEngine, Action action, Executor eventsExecutor, Executor postExecutionsExecutor) {
        super(name, statsEngine, action, eventsExecutor, postExecutionsExecutor);
        this.startEventsProcessor(eventsQueue, eventsExecutor);
        this.startPostEventsProcessor(postEventQueue, postExecutionsExecutor);
    }

    protected void startPostEventsProcessor(ConcurrentLinkedQueue<Tuple3<AsyncEvent<S>, AsyncEvent<T>, Session>> postEventQueue, Executor postExecutionsExecutor) {
        Executors.newSingleThreadExecutor().submit(() -> {
            log.info("Starting the post event processor for {}", name);
            try {
                while (true) {
                    Tuple3<AsyncEvent<S>, AsyncEvent<T>, Session> event;
                    while ((event = postEventQueue.poll()) == null) {
                    }
                    executePostValidation(event._1, event._2, event._3, postEventExecutor);
                }
            } catch (Exception e) {
                log.error("Error when processing the events queue of {}", name, e);
            }
        });
    }

    protected void startEventsProcessor(ConcurrentLinkedQueue<Tuple2<AsyncEvent<S>, Session>> eventsQueue, Executor eventsExecutor) {
        Executors.newSingleThreadExecutor().submit(() -> {
           log.info("Starting the event processor for {}", name);
           try {
               while (true) {
                   Tuple2<AsyncEvent<S>, Session> event;
                   while ((event = eventsQueue.poll()) == null) {
                   }
                   eventsExecutor.execute(getEventTask(event._1, event._2));
               }
           } catch (Exception e) {
               log.error("Error when processing the events queue of {}", name, e);
           }
        });
    }

    @Override
    protected void submitEvent(AsyncEvent<S> sendEvent, Session session, AbstractAction<S, T> stAbstractAction) {
        eventsQueue.add(Tuple.of(sendEvent, session));
    }

    @Override
    protected void submitPostEvent(AsyncEvent<S> sendEvent, AsyncEvent<T> responseEvent, Session session, AbstractAction<S, T> stAbstractAction) {
        postEventQueue.add(Tuple.of(sendEvent, responseEvent, session));
    }


}
