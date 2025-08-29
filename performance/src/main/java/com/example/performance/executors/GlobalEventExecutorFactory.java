package com.example.performance.executors;

import com.example.performance.actions.AbstractAction;
import com.example.performance.actions.AsyncEvent;
import com.example.performance.kafka.Representable;
import io.gatling.core.session.Session;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

@Slf4j
public class GlobalEventExecutorFactory<S extends Representable, T> {
    private final ConcurrentLinkedQueue<Tuple3<AsyncEvent<S>, Session, AbstractAction<S, T>>> eventsQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Tuple4<AsyncEvent<S>, AsyncEvent<T>, Session, AbstractAction<S, T>>> postEventQueue = new ConcurrentLinkedQueue<>();

    private static volatile GlobalEventExecutorFactory<?, ?> instance = null;

    private GlobalEventExecutorFactory() {
        this.startEventsProcessor();
        this.startPostEventsProcessor();
    }

    public static <S extends  Representable, T> GlobalEventExecutorFactory<S, T> instance() {
        if (instance == null) {
            synchronized (GlobalEventExecutorFactory.class) {
                if (instance == null) {
                    instance = new GlobalEventExecutorFactory<>();
                }
            }
        }
        return (GlobalEventExecutorFactory<S, T>) instance;
    }

    private void startEventsProcessor() {
        Executors.newSingleThreadExecutor().submit(() -> {
            log.info("Starting the event processor for GlobalEventExecutorFactory");
            try {
                while (true) {
                    Tuple3<AsyncEvent<S>, Session, AbstractAction<S, T>> event;
                    while ((event = eventsQueue.poll()) == null) {
                    }
                    GlobalExecutorFactory.getInstance().getSharedExecutor().execute(event._3.getEventTask(event._1, event._2));
                }
            } catch (Exception e) {
                log.error("Error when processing the events queue of GlobalEventExecutorFactory", e);
            }
        });
    }

    private void startPostEventsProcessor() {
        Executors.newSingleThreadExecutor().submit(() -> {
            log.info("Starting the post event processor for GlobalEventExecutorFactory");
            try {
                while (true) {
                    Tuple4<AsyncEvent<S>, AsyncEvent<T>, Session, AbstractAction<S, T>> event;
                    while ((event = postEventQueue.poll()) == null) {
                    }
                    event._4().executePostValidation(event._1, event._2, event._3, GlobalExecutorFactory.getInstance().getSharedExecutor());
                }
            } catch (Exception e) {
                log.error("Error when processing the events queue of GlobalEventExecutorFactory", e);
            }
        });
    }

    public void submitEvent(AsyncEvent<S> event, Session session, AbstractAction<S, T> action) {
        eventsQueue.add(new Tuple3<>(event, session, action));
    }

    public void submitPostEvent(AsyncEvent<S> event, AsyncEvent<T> resp, Session session, AbstractAction<S, T> action) {
        postEventQueue.add(new Tuple4<>(event, resp, session, action));
    }
}
