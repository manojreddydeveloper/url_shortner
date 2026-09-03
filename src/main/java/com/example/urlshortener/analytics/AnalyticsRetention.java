package com.example.urlshortener.analytics;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;

@Component
public class AnalyticsRetention {
    static final Duration RETENTION = Duration.ofDays(90);

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsRetention.class);

    private final Supplier<ClickEventRepository> repository;
    private final Clock clock;
    private final OperationalMetrics metrics;
    private Instant lastSuccessfulCleanup;

    @Autowired
    public AnalyticsRetention(ObjectProvider<ClickEventRepository> repository,
            ObjectProvider<OperationalMetrics> metrics) {
        this(repository::getIfAvailable, Clock.systemUTC(), metrics.getIfAvailable());
    }

    AnalyticsRetention(ClickEventRepository repository, Clock clock) {
        this(() -> repository, clock, null);
    }

    private AnalyticsRetention(Supplier<ClickEventRepository> repository, Clock clock,
            OperationalMetrics metrics) {
        this.repository = repository;
        this.clock = clock;
        this.metrics = metrics;
        this.lastSuccessfulCleanup = clock.instant();
    }

    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    public void deleteExpiredEvents() {
        Instant now = clock.instant();
        if (metrics != null) metrics.deletionLag(Duration.between(lastSuccessfulCleanup, now));
        ClickEventRepository availableRepository = repository.get();
        if (availableRepository == null) {
            metric(Outcome.FAILED);
            LOGGER.warn("Analytics retention cleanup skipped because persistence is unavailable");
            return;
        }

        Instant cutoffInclusive = clock.instant().minus(RETENTION);
        try {
            int deleted = availableRepository.deleteExpired(cutoffInclusive);
            lastSuccessfulCleanup = now;
            if (metrics != null) metrics.deletionLag(Duration.ZERO);
            metric(Outcome.SUCCESS);
            LOGGER.info("Analytics retention cleanup completed; deletedCount={}", deleted);
        } catch (RuntimeException ignored) {
            metric(Outcome.FAILED);
            LOGGER.warn("Analytics retention cleanup failed");
        }
    }

    private void metric(Outcome outcome) { if (metrics != null) metrics.analytics("retention", outcome); }
}
