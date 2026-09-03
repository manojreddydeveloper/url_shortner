package com.example.urlshortener.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class ClickEventRepositoryContractTest {
    @Test
    void aggregateMethodsAreProvidedByTheCustomRepositoryContract() throws Exception {
        assertThat(ClickEventRepositoryCustom.class.isAssignableFrom(ClickEventRepository.class)).isTrue();

        Method totals = ClickEventRepositoryCustom.class.getMethod(
                "aggregateTotals", long.class, Instant.class, Instant.class, Instant.class);
        Method daily = ClickEventRepositoryCustom.class.getMethod(
                "aggregateDaily", long.class, Instant.class, Instant.class, Instant.class);

        assertThat(totals.getReturnType()).isEqualTo(ClickEventRepositoryCustom.TrafficTotals.class);
        assertThat(daily.getReturnType()).isEqualTo(java.util.List.class);
    }

    @Test
    void cleanupDeletesTheInclusiveRetentionBoundary() throws Exception {
        Method cleanup = ClickEventRepository.class.getMethod("deleteExpired", Instant.class);

        assertThat(cleanup.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(cleanup.getAnnotation(Query.class).value())
                .contains("event.occurredAt <= :cutoffInclusive");
    }
}
