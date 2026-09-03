package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PropertyStyleTests {

    // --- OperationalMetrics.tag(): enum values produce lowercase tags ---

    @ParameterizedTest(name = "Operation tag: {0}")
    @MethodSource("allOperations")
    void operationTagsAreLowercase(Operation op) {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);
        metrics.request(op, Outcome.SUCCESS, 200, 1_000_000);
        assertThat(registry.find("url_shortener.requests")
                .tag("operation", op.name().toLowerCase())
                .counter()).isNotNull();
    }

    static Stream<Arguments> allOperations() {
        return Stream.of(Operation.values()).map(Arguments::of);
    }

    @ParameterizedTest(name = "Outcome tag: {0}")
    @MethodSource("allOutcomes")
    void outcomeTagsAreLowercase(Outcome out) {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);
        metrics.request(Operation.CREATION, out, 200, 1_000_000);
        assertThat(registry.find("url_shortener.requests")
                .tag("outcome", out.name().toLowerCase())
                .counter()).isNotNull();
    }

    static Stream<Arguments> allOutcomes() {
        return Stream.of(Outcome.values()).map(Arguments::of);
    }

    // --- RequestMetricsFilter.outcome(): all status codes map to valid Outcome ---

    @ParameterizedTest(name = "status {0} maps to valid outcome")
    @ValueSource(ints = {100, 200, 201, 204, 301, 302, 400, 401, 403, 404, 429, 500, 502, 503, 504})
    void outcomeAlwaysMapsToValidEnum(int status) {
        Outcome outcome = RequestMetricsFilter.outcome(status);
        assertThat(outcome).isIn(Outcome.values());
    }

    // --- RequestMetricsFilter.operation(): all method/path combos map to valid Operation ---

    @ParameterizedTest(name = "operation for {0} {1}")
    @MethodSource("methodPathCombos")
    void operationAlwaysMapsToValidEnum(String method, String path) {
        Operation op = RequestMetricsFilter.operation(method, path);
        assertThat(op).isIn(Operation.values());
    }

    static Stream<Arguments> methodPathCombos() {
        return Stream.of(
                Arguments.of("POST", "/api/v1/links"),
                Arguments.of("GET", "/api/v1/links/abc123/analytics"),
                Arguments.of("GET", "/health/live"),
                Arguments.of("GET", "/health/ready"),
                Arguments.of("GET", "/abc123"),
                Arguments.of("DELETE", "/api/v1/links"),
                Arguments.of("PUT", "/unknown"),
                Arguments.of("PATCH", "/api/v1/links/abc123")
        );
    }

    // --- RateLimiter.Bucket: available permits always in [0, capacity] ---

    @Test
    void bucketPermitsNeverExceedCapacity() {
        RateLimiter.Bucket bucket = new RateLimiter.Bucket(20, 10);
        for (int i = 0; i < 25; i++) {
            bucket.tryConsume();
        }
        assertThat(bucket.availablePermits()).isBetween(0, 20);
    }

    @Test
    void bucketSecondsUntilNextPermitIsZeroWhenPermitsAvailable() {
        RateLimiter.Bucket bucket = new RateLimiter.Bucket(20, 10);
        assertThat(bucket.secondsUntilNextPermit()).isEqualTo(0);
    }
}
