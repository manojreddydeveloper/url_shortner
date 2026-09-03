package com.example.urlshortener.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class ClickEventRepositoryContractTest {
    @Test
    void aggregateQueriesUseApprovedRangeRetentionAndUtcBoundaries() throws Exception {
        Method totals = ClickEventRepository.class.getMethod(
                "aggregateTotals", long.class, Instant.class, Instant.class, Instant.class);
        Method daily = ClickEventRepository.class.getMethod(
                "aggregateDaily", long.class, Instant.class, Instant.class, Instant.class);

        assertRangeAndRetentionBoundaries(totals.getAnnotation(Query.class).value());
        assertRangeAndRetentionBoundaries(daily.getAnnotation(Query.class).value());
        assertThat(daily.getAnnotation(Query.class).value())
                .contains("AT TIME ZONE 'UTC'")
                .contains("GROUP BY 1")
                .contains("ORDER BY 1");
    }

    @Test
    void cleanupDeletesTheInclusiveRetentionBoundary() throws Exception {
        Method cleanup = ClickEventRepository.class.getMethod("deleteExpired", Instant.class);

        assertThat(cleanup.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(cleanup.getAnnotation(Query.class).value())
                .contains("event.occurredAt <= :cutoffInclusive");
    }

    private void assertRangeAndRetentionBoundaries(String query) {
        assertThat(query)
                .contains("occurred_at >= :fromInclusive")
                .contains("occurred_at < :toExclusive")
                .contains("occurred_at > :retentionCutoffExclusive");
    }
}
