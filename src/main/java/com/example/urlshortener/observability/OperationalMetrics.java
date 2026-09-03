package com.example.urlshortener.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class OperationalMetrics {
    private final MeterRegistry registry;
    private final AtomicInteger liveness = new AtomicInteger(1);
    private final AtomicInteger readiness = new AtomicInteger();
    private final AtomicInteger shutdown = new AtomicInteger();
    private final AtomicLong deletionLagSeconds = new AtomicLong();
    private final ConcurrentHashMap<String, AtomicLong> saturation = new ConcurrentHashMap<>();

    public OperationalMetrics(MeterRegistry registry) {
        this.registry = registry;
        Gauge.builder("url_shortener.lifecycle", liveness, AtomicInteger::get).tag("state", "live").register(registry);
        Gauge.builder("url_shortener.lifecycle", readiness, AtomicInteger::get).tag("state", "ready").register(registry);
        Gauge.builder("url_shortener.lifecycle", shutdown, AtomicInteger::get).tag("state", "shutdown").register(registry);
        Gauge.builder("url_shortener.analytics.deletion.lag.seconds", deletionLagSeconds, AtomicLong::get)
                .register(registry);
    }

    public void request(Operation operation, Outcome outcome, int status, long elapsedNanos) {
        String operationTag = tag(operation);
        String outcomeTag = tag(outcome);
        String statusClass = status / 100 + "xx";
        Counter.builder("url_shortener.requests")
                .tags("operation", operationTag, "outcome", outcomeTag, "status_class", statusClass)
                .register(registry).increment();
        Timer.builder("url_shortener.request.duration")
                .tags("operation", operationTag, "outcome", outcomeTag)
                .publishPercentileHistogram()
                .register(registry).record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void creation(Outcome outcome) { outcomeCounter("url_shortener.creation", outcome); }
    public void redirect(Outcome outcome) { outcomeCounter("url_shortener.redirect", outcome); }
    public void analytics(String action, Outcome outcome) {
        Counter.builder("url_shortener.analytics")
                .tags("action", allowlistedAction(action), "outcome", tag(outcome))
                .register(registry).increment();
    }
    public void cache(String action) {
        Counter.builder("url_shortener.cache")
                .tag("action", allowlistedCacheAction(action))
                .register(registry).increment();
    }
    public void dependency(Operation operation, Outcome outcome, Duration elapsed) {
        Timer.builder("url_shortener.dependency.duration")
                .tags("operation", tag(operation), "outcome", tag(outcome))
                .register(registry).record(elapsed);
    }
    public void collision(Outcome outcome) { outcomeCounter("url_shortener.collision", outcome); }
    public void rateLimit(Operation operation, Outcome outcome) {
        Counter.builder("url_shortener.rate_limit")
                .tags("operation", tag(operation), "outcome", tag(outcome))
                .register(registry).increment();
    }
    public void readiness(boolean ready) { readiness.set(ready ? 1 : 0); }
    public void shutdown(boolean active) { shutdown.set(active ? 1 : 0); }
    public void deletionLag(Duration lag) { deletionLagSeconds.set(Math.max(0, lag.toSeconds())); }
    public void saturation(String pool, double utilization) {
        if (!pool.equals("database") && !pool.equals("request")) throw new IllegalArgumentException("Unknown pool");
        AtomicLong scaled = saturation.computeIfAbsent(pool, value -> {
            AtomicLong gauge = new AtomicLong();
            Gauge.builder("url_shortener.pool.utilization", gauge, state -> state.get() / 10_000.0)
                    .tag("pool", value).register(registry);
            return gauge;
        });
        scaled.set(Math.round(Math.max(0, Math.min(1, utilization)) * 10_000));
    }

    private void outcomeCounter(String name, Outcome outcome) {
        Counter.builder(name).tag("outcome", tag(outcome)).register(registry).increment();
    }

    private static String tag(Enum<?> value) { return value.name().toLowerCase(Locale.ROOT); }
    private static String allowlistedAction(String action) {
        if (!action.equals("append") && !action.equals("query") && !action.equals("retention")) {
            throw new IllegalArgumentException("Unknown analytics action");
        }
        return action;
    }
    private static String allowlistedCacheAction(String action) {
        if (!action.equals("hit") && !action.equals("miss") && !action.equals("write")) {
            throw new IllegalArgumentException("Unknown cache action");
        }
        return action;
    }

    public enum Operation { CREATION, REDIRECT, ANALYTICS, LIVENESS, READINESS, OTHER }
    public enum Outcome {
        SUCCESS, VALIDATION_REJECTION, MALFORMED, UNKNOWN, AUTHENTICATION_REQUIRED,
        RATE_LIMITED, DEPENDENCY_FAILURE, INTERNAL_FAILURE, ATTEMPTED, COMMITTED,
        FAILED, AMBIGUOUS_LOST, COLLISION_RETRY, COLLISION_EXHAUSTION, ALLOWED, REJECTED
    }
}
