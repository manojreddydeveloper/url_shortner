package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class ServiceLifecycleMetricsTest {
    @Test
    void shutdownTransitionClearsReadinessAndSetsShutdown() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);
        metrics.readiness(true);

        new ServiceLifecycleMetrics(metrics).shuttingDown();

        assertThat(registry.find("url_shortener.lifecycle").tag("state", "ready").gauge().value()).isZero();
        assertThat(registry.find("url_shortener.lifecycle").tag("state", "shutdown").gauge().value()).isEqualTo(1);
    }
}
