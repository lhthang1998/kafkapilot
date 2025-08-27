package com.example.performance.actions;

import io.gatling.core.action.Action;
import io.gatling.core.structure.ScenarioContext;
import io.gatling.javaapi.core.ActionBuilder;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@SuperBuilder
public abstract class IntegrationActionDsl implements ActionBuilder {
    @Builder.Default
    protected Executor executor = ForkJoinPool.commonPool();

    public abstract Action build(ScenarioContext ctx, Action action);

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return new IntegrationActionBuilder(this);
    }

    private static class IntegrationActionBuilder implements io.gatling.core.action.builder.ActionBuilder {
        private final IntegrationActionDsl dsl;

        public IntegrationActionBuilder(IntegrationActionDsl dsl) {
            this.dsl = dsl;
        }

        @Override
        public Action build(ScenarioContext ctx, Action next) {
            return dsl.build(ctx, next);
        }
    }
}
