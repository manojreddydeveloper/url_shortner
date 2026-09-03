package com.example.urlshortener.url;

import com.example.urlshortener.persistence.DatabaseTimeBudget;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class LinkWriter {
    private final LinkRepository repository;
    private final DatabaseTimeBudget timeBudget;
    private final OperationalMetrics metrics;

    @Autowired
    public LinkWriter(LinkRepository repository, DatabaseTimeBudget timeBudget,
            ObjectProvider<OperationalMetrics> metrics) {
        this(repository, timeBudget, metrics.getIfAvailable());
    }
    public LinkWriter(LinkRepository repository, DatabaseTimeBudget timeBudget) {
        this(repository, timeBudget, (OperationalMetrics) null);
    }
    private LinkWriter(LinkRepository repository, DatabaseTimeBudget timeBudget, OperationalMetrics metrics) {
        this.repository = repository;
        this.timeBudget = timeBudget;
        this.metrics = metrics;
    }

    LinkWriter(LinkRepository repository) { this(repository, null); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(LinkEntity link) {
        long started = System.nanoTime();
        try {
            if (timeBudget != null) timeBudget.apply(DatabaseTimeBudget.Operation.LINK_CREATION);
            repository.saveAndFlush(link);
            dependency(Outcome.SUCCESS, started);
        } catch (RuntimeException exception) {
            dependency(Outcome.DEPENDENCY_FAILURE, started);
            throw exception;
        }
    }

    private void dependency(Outcome outcome, long started) {
        if (metrics != null) metrics.dependency(Operation.CREATION, outcome,
                Duration.ofNanos(System.nanoTime() - started));
    }
}
