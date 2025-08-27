package com.example.performance.actions;

import com.example.performance.interfaces.StrictLogging;
import com.example.performance.kafka.Representable;
import io.gatling.commons.stats.Status;
import io.gatling.core.action.Action;
import io.gatling.core.action.ChainableAction;
import io.gatling.core.session.Session;
import io.gatling.core.stats.StatsEngine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scala.Option;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.String.format;

@Getter
@Slf4j
public abstract class AbstractAction<S extends Representable, T> implements ChainableAction, StrictLogging {
    protected final String name;
    protected final StatsEngine statsEngine;
    protected final Action action;
    protected final Executor eventsExecutor;
    protected final Executor postEventExecutor;

    public AbstractAction(String name, StatsEngine statsEngine, Action action, Executor eventsExecutor, Executor postEventExecutor) {
        this.name = name;
        this.statsEngine = statsEngine;
        this.action = action;
        this.eventsExecutor = eventsExecutor;
        this.postEventExecutor = postEventExecutor;
    }

    protected Optional<AsyncEvent<T>> fetchExpectedResponse(S details, Session session) {
        return fetchExpectedResponse(details);
    }

    protected Optional<AsyncEvent<T>> fetchExpectedResponse(S details) {
        throw new UnsupportedOperationException("This method must be overridden");
    }

    protected abstract void trackOkRecord(S details, String timeframe);
    protected abstract void trackKoRecord(S details, String timeframe);

    public void execute(Session session) {
        var startTime = System.currentTimeMillis();
        try {
            var sendEvent = initRequest(session);
            submitEvent(sendEvent, session, this);
        } catch (Exception ex) {
            log.error("Error when submit event ", ex);
        }
    }

    protected abstract void submitEvent(AsyncEvent<S> sendEvent, Session session, AbstractAction<S,T> stAbstractAction);
    protected abstract void submitPostEvent(AsyncEvent<S> sendEvent, AsyncEvent<T> responseEvent, Session session, AbstractAction<S,T> stAbstractAction);

    protected abstract AsyncEvent<S> initRequest(Session session);

    public Runnable getEventTask(AsyncEvent<S> sendEvent, Session session) {
        return () -> {
          try {
              var result = fetchExpectedResponse(sendEvent.getDetails(), session);
              if (result.isPresent()) {
                  var resp = result.get();
                  if (resp.isPostValidations()) {
                      submitPostEvent(sendEvent, resp, session, this);
                      return;
                  }
                  statsEngine.logResponse(session.scenario(), session.groups(), name, sendEvent.getTimestamp(),
                          resp.getTimestamp(), Status.apply("OK"), Option.empty(), Option.apply(format("%s - found matching record", name)));
                  trackOkRecord(sendEvent.getDetails(), sendEvent.getTimestamp() + "," + resp.getTimestamp());
                  if (sendEvent.getTimestamp() > resp.getTimestamp()) {
                      var details = sendEvent.getDetails().getRepr();
                      log.warn("End time {} is less than start time {} - {}", resp.getTimestamp(), sendEvent.getTimestamp(), details);
                  }
                  next().$bang(session.markAsSucceeded());
              } else {
                  if (Instant.now().toEpochMilli() - sendEvent.getTimestamp() > getTimeout()) {
                      trackKoRecord(sendEvent.getDetails(), sendEvent.getTimestamp() + "," + Instant.now().toEpochMilli());
                      doTimeout(sendEvent, session);
                      return;
                  }
                  submitEvent(sendEvent, session, this);
              }
          } catch (Exception ex) {
              trackKoRecord(sendEvent.getDetails(), sendEvent.getTimestamp() + "," + Instant.now().toEpochMilli());
              handleErrors(sendEvent, session, ex);
          }
        };
    }

    public void executePostValidation(AsyncEvent<S> sendEvent, AsyncEvent<T> responseEvent, Session session, Executor postEventExecutor) {
        var startTime = Instant.now().toEpochMilli();
        try {
            CompletableFuture.supplyAsync(() -> awaitForPostValidation(responseEvent.getDetails()), postEventExecutor)
                    .thenAccept(postValidationResult -> {
                        if (postValidationResult) {
                            statsEngine.logResponse(session.scenario(), session.groups(), name, sendEvent.getTimestamp(),
                                    responseEvent.getTimestamp(), Status.apply("OK"), Option.empty(), Option.apply(format("%s - found matching record", name)));
                            trackOkRecord(sendEvent.getDetails(), sendEvent.getTimestamp() + "," + Instant.now().toEpochMilli());
                            next().$bang(session.markAsSucceeded());
                        } else {
                            trackKoRecord(sendEvent.getDetails(), sendEvent.getTimestamp() + "," + Instant.now().toEpochMilli());
                            next().$bang(session.markAsFailed());
                        }
                    }).exceptionally(exc -> {
                        trackKoRecord(sendEvent.getDetails(), sendEvent.getTimestamp() + "," + Instant.now().toEpochMilli());
                        handleErrors(sendEvent, session, exc);
                        return null;
                    });
        } catch (Exception ex) {
            handleErrors(sendEvent, session, ex);

        }
    }

    protected boolean awaitForPostValidation(T details) {
        var startTime = Instant.now().toEpochMilli();
        do {
            if (validatePostExecution(details)) {
                return true;
            }
        } while (Instant.now().toEpochMilli() - startTime <= getTimeout());
        return validatePostExecution(details);
    }

    protected boolean validatePostExecution(T details) {
        return true;
    }

    protected void handleErrors(AsyncEvent<S> sendEvent, Session session, Throwable ex) {
        var startTime= sendEvent.getTimestamp();
        var extraInfo =  sendEvent == null ? name : sendEvent.getDetails().getRepr();
        log.error("Encountered an unexpected error", ex);
        statsEngine.logResponse(session.scenario(), session.groups(), name, startTime, Instant.now().toEpochMilli(),
                Status.apply("KO"), Option.empty(), Option.apply(format("%s - %s", extraInfo, ex.getMessage())));
        next().$bang(session.markAsFailed());
    }

    protected void doTimeout(AsyncEvent<S> sendEvent, Session session) {
        var startTime= sendEvent.getTimestamp();
        log.error("Did not find response for action={}", name);
        statsEngine.logResponse(session.scenario(), session.groups(), name, startTime, Instant.now().toEpochMilli(),
                Status.apply("KO"), Option.empty(), Option.apply(format("%s - timed out", name)));
        next().$bang(session.markAsFailed());
    }

    protected abstract Long getTimeout();

    public Action next() {
        return action;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public StatsEngine statsEngine() {
        return statsEngine;
    }
}
