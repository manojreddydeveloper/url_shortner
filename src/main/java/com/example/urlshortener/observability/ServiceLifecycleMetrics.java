package com.example.urlshortener.observability;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class ServiceLifecycleMetrics {
    private final OperationalMetrics metrics;

    public ServiceLifecycleMetrics(OperationalMetrics metrics) { this.metrics = metrics; }

    @PreDestroy
    void shuttingDown() {
        metrics.readiness(false);
        metrics.shutdown(true);
    }
}
