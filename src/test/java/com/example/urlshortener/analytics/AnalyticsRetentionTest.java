package com.example.urlshortener.analytics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class AnalyticsRetentionTest {
    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");
    private static final Instant RETENTION_CUTOFF = Instant.parse("2026-06-04T12:00:00Z");

    @Test
    void deletesEventsAtOrOlderThanTheNinetyDayBoundary() {
        ClickEventRepository repository = mock(ClickEventRepository.class);
        AnalyticsRetention retention = new AnalyticsRetention(
                repository, Clock.fixed(NOW, ZoneOffset.UTC));

        retention.deleteExpiredEvents();

        verify(repository, times(1)).deleteExpired(RETENTION_CUTOFF);
    }

    @Test
    void cleanupFailureRemainsObservableWithoutStoppingTheScheduler() {
        ClickEventRepository repository = mock(ClickEventRepository.class);
        when(repository.deleteExpired(RETENTION_CUTOFF))
                .thenThrow(new IllegalStateException("database unavailable"));
        AnalyticsRetention retention = new AnalyticsRetention(
                repository, Clock.fixed(NOW, ZoneOffset.UTC));

        assertDoesNotThrow(retention::deleteExpiredEvents);
        verify(repository, times(1)).deleteExpired(RETENTION_CUTOFF);
    }

    @Test
    void cleanupRunsHourlyInUtcWithinTheApprovedDeletionWindow() throws Exception {
        Scheduled schedule = AnalyticsRetention.class
                .getMethod("deleteExpiredEvents")
                .getAnnotation(Scheduled.class);

        assertNotNull(schedule);
        assertEquals("0 0 * * * *", schedule.cron());
        assertEquals("UTC", schedule.zone());
    }
}
