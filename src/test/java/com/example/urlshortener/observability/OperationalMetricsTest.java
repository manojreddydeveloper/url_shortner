package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OperationalMetricsTest {
    @Test
    void emitsEveryApprovedMetricFamilyWithBoundedLabels() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);

        metrics.request(Operation.CREATION, Outcome.SUCCESS, 201, 1_000_000);
        metrics.creation(Outcome.VALIDATION_REJECTION);
        metrics.redirect(Outcome.MALFORMED);
        metrics.redirect(Outcome.UNKNOWN);
        metrics.analytics("append", Outcome.ATTEMPTED);
        metrics.analytics("append", Outcome.COMMITTED);
        metrics.analytics("append", Outcome.FAILED);
        metrics.analytics("append", Outcome.AMBIGUOUS_LOST);
        metrics.analytics("query", Outcome.SUCCESS);
        metrics.dependency(Operation.REDIRECT, Outcome.DEPENDENCY_FAILURE, Duration.ofMillis(3));
        metrics.rateLimit(Operation.CREATION, Outcome.REJECTED);
        metrics.collision(Outcome.COLLISION_RETRY);
        metrics.collision(Outcome.COLLISION_EXHAUSTION);
        metrics.readiness(true);
        metrics.shutdown(true);
        metrics.saturation("database", 0.81);
        metrics.deletionLag(Duration.ofHours(25));

        assertThat(registry.getMeters()).extracting(meter -> meter.getId().getName())
                .contains("url_shortener.requests", "url_shortener.request.duration",
                        "url_shortener.creation", "url_shortener.redirect", "url_shortener.analytics",
                        "url_shortener.dependency.duration", "url_shortener.rate_limit",
                        "url_shortener.collision", "url_shortener.lifecycle",
                        "url_shortener.pool.utilization", "url_shortener.analytics.deletion.lag.seconds");
        assertThat(registry.find("url_shortener.lifecycle").tag("state", "ready").gauge().value()).isEqualTo(1);
        assertThat(registry.find("url_shortener.lifecycle").tag("state", "shutdown").gauge().value()).isEqualTo(1);
        assertThat(registry.find("url_shortener.pool.utilization").tag("pool", "database").gauge().value())
                .isEqualTo(0.81);

        Set<String> allowedKeys = Set.of("operation", "outcome", "status_class", "action", "state", "pool", "service");
        registry.getMeters().forEach(meter -> meter.getId().getTags().forEach(tag -> {
            assertThat(allowedKeys).contains(tag.getKey());
            assertThat(tag.getValue()).doesNotContain("https://", "aZ3kP9mQ2x", "Bearer", "request-123");
        }));
    }

    @Test
    void rejectsUnboundedActionAndPoolLabels() {
        OperationalMetrics metrics = new OperationalMetrics(new SimpleMeterRegistry());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> metrics.analytics("user-supplied", Outcome.SUCCESS));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> metrics.saturation("user-supplied", 0.5));
    }
}
