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

@Component
public class AnalyticsRetention {
    static final Duration RETENTION = Duration.ofDays(90);

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsRetention.class);

    private final Supplier<ClickEventRepository> repository;
    private final Clock clock;

    @Autowired
    public AnalyticsRetention(ObjectProvider<ClickEventRepository> repository) {
        this(repository::getIfAvailable, Clock.systemUTC());
    }

    AnalyticsRetention(ClickEventRepository repository, Clock clock) {
        this(() -> repository, clock);
    }

    private AnalyticsRetention(Supplier<ClickEventRepository> repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    public void deleteExpiredEvents() {
        ClickEventRepository availableRepository = repository.get();
        if (availableRepository == null) {
            LOGGER.warn("Analytics retention cleanup skipped because persistence is unavailable");
            return;
        }

        Instant cutoffInclusive = clock.instant().minus(RETENTION);
        try {
            int deleted = availableRepository.deleteExpired(cutoffInclusive);
            LOGGER.info("Analytics retention cleanup completed; deletedCount={}", deleted);
        } catch (RuntimeException ignored) {
            LOGGER.warn("Analytics retention cleanup failed");
        }
    }
}
