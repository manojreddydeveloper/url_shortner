package com.example.urlshortener.analytics;

import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.urlshortener.persistence.DatabaseTimeBudget;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import java.time.Duration;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class JpaEventSink implements AnalyticsCapture.EventSink {
    private final Supplier<ClickEventRepository> repository;
    private final Supplier<DatabaseTimeBudget> timeBudget;
    private final Supplier<OperationalMetrics> metrics;

    @Autowired
    public JpaEventSink(ObjectProvider<ClickEventRepository> repository,
            ObjectProvider<DatabaseTimeBudget> timeBudget,
            ObjectProvider<OperationalMetrics> metrics) {
        this(repository::getIfAvailable, timeBudget::getIfAvailable, metrics::getIfAvailable);
    }

    JpaEventSink(ObjectProvider<ClickEventRepository> repository) {
        this(repository::getIfAvailable, () -> null, () -> null);
    }

    JpaEventSink(ClickEventRepository repository) { this(() -> repository, () -> null, () -> null); }

    JpaEventSink(ClickEventRepository repository, DatabaseTimeBudget timeBudget) {
        this(() -> repository, () -> timeBudget, () -> null);
    }

    private JpaEventSink(Supplier<ClickEventRepository> repository, Supplier<DatabaseTimeBudget> timeBudget,
            Supplier<OperationalMetrics> metrics) {
        this.repository = repository;
        this.timeBudget = timeBudget;
        this.metrics = metrics;
    }

    @Override
    @Transactional
    public void append(ClickEvent event) {
        long started = System.nanoTime();
        ClickEventRepository availableRepository = repository.get();
        if (availableRepository == null) {
            throw new IllegalStateException("Analytics persistence is unavailable");
        }
        DatabaseTimeBudget availableTimeBudget = timeBudget.get();
        if (availableTimeBudget != null) {
            availableTimeBudget.apply(DatabaseTimeBudget.Operation.ANALYTICS_APPEND);
        }
        try {
            availableRepository.saveAndFlush(new ClickEventEntity(event));
            dependency(Outcome.SUCCESS, started);
        } catch (RuntimeException exception) {
            dependency(Outcome.DEPENDENCY_FAILURE, started);
            throw exception;
        }
    }

    private void dependency(Outcome outcome, long started) {
        OperationalMetrics availableMetrics = metrics.get();
        if (availableMetrics != null) availableMetrics.dependency(Operation.ANALYTICS, outcome,
                Duration.ofNanos(System.nanoTime() - started));
    }
}
