package com.example.urlshortener.redirect;

import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.urlshortener.cache.LinkCache;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import com.example.urlshortener.persistence.DatabaseTimeBudget;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Operation;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;

@Service
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class LinkResolver {
    private static final Pattern CODE = Pattern.compile("[0-9A-Za-z]{10}");
    private final LinkRepository repository;
    private final DatabaseTimeBudget timeBudget;
    private final LinkCache cache;
    private final OperationalMetrics metrics;

    @Autowired
    public LinkResolver(LinkRepository repository, DatabaseTimeBudget timeBudget, LinkCache cache,
            ObjectProvider<OperationalMetrics> metrics) {
        this(repository, timeBudget, cache, metrics.getIfAvailable());
    }

    public LinkResolver(LinkRepository repository, DatabaseTimeBudget timeBudget, LinkCache cache) {
        this(repository, timeBudget, cache, (OperationalMetrics) null);
    }

    public LinkResolver(LinkRepository repository, LinkCache cache) {
        this(repository, null, cache, (OperationalMetrics) null);
    }

    public LinkResolver(LinkRepository repository) {
        this(repository, null, new LinkCache(10_000), (OperationalMetrics) null);
    }

    private LinkResolver(LinkRepository repository, DatabaseTimeBudget timeBudget, LinkCache cache,
            OperationalMetrics metrics) {
        this.repository = repository;
        this.timeBudget = timeBudget;
        this.cache = cache;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true)
    public Resolution resolve(String code) {
        if (code == null || !CODE.matcher(code).matches()) {
            metric(OperationalMetrics.Outcome.MALFORMED);
            return Resolution.notFound();
        }
        long started = System.nanoTime();
        try {
            if (cache != null) {
                var cached = cache.get(code);
                if (cached.isPresent()) {
                    if (metrics != null) metrics.cache("hit");
                    Resolution result = Resolution.active(cached.get());
                    metric(OperationalMetrics.Outcome.SUCCESS);
                    return result;
                }
                if (metrics != null) metrics.cache("miss");
            }
            if (timeBudget != null) timeBudget.apply(DatabaseTimeBudget.Operation.MAPPING_LOOKUP);
            Resolution result = repository.findByShortCode(code).map(link -> {
                if (cache != null) {
                    cache.put(link);
                    if (metrics != null) metrics.cache("write");
                }
                return Resolution.active(link);
            }).orElseGet(Resolution::notFound);
            OperationalMetrics.Outcome outcome = result.outcome() == Outcome.ACTIVE
                    ? OperationalMetrics.Outcome.SUCCESS : OperationalMetrics.Outcome.UNKNOWN;
            metric(outcome);
            dependency(outcome, started);
            return result;
        } catch (DataAccessException exception) {
            metric(OperationalMetrics.Outcome.DEPENDENCY_FAILURE);
            dependency(OperationalMetrics.Outcome.DEPENDENCY_FAILURE, started);
            return Resolution.dependencyUnavailable();
        }
    }

    private void metric(OperationalMetrics.Outcome outcome) { if (metrics != null) metrics.redirect(outcome); }
    private void dependency(OperationalMetrics.Outcome outcome, long started) {
        if (metrics != null) metrics.dependency(Operation.REDIRECT, outcome, Duration.ofNanos(System.nanoTime() - started));
    }

    public record Resolution(Outcome outcome, LinkEntity link) {
        static Resolution active(LinkEntity link) { return new Resolution(Outcome.ACTIVE, link); }
        static Resolution notFound() { return new Resolution(Outcome.NOT_FOUND, null); }
        static Resolution dependencyUnavailable() { return new Resolution(Outcome.DEPENDENCY_UNAVAILABLE, null); }
    }
    public enum Outcome { ACTIVE, NOT_FOUND, DEPENDENCY_UNAVAILABLE }
}
