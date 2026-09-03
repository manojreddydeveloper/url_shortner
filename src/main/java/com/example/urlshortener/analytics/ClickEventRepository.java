package com.example.urlshortener.analytics;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClickEventRepository extends JpaRepository<ClickEventEntity, Long> {
    @Query(value = """
            SELECT COUNT(*) AS "allCount",
                   COUNT(*) FILTER (WHERE traffic_class = 'SUSPECTED_AUTOMATED') AS "suspectedAutomated",
                   COUNT(*) FILTER (WHERE traffic_class = 'UNCLASSIFIED') AS "unclassified"
              FROM click_events
             WHERE link_id = :linkId
               AND occurred_at >= :fromInclusive
               AND occurred_at < :toExclusive
               AND occurred_at > :retentionCutoffExclusive
            """, nativeQuery = true)
    TrafficTotals aggregateTotals(
            @Param("linkId") long linkId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive,
            @Param("retentionCutoffExclusive") Instant retentionCutoffExclusive);

    @Query(value = """
            SELECT date_trunc('day', occurred_at AT TIME ZONE 'UTC') AT TIME ZONE 'UTC' AS "bucketStart",
                   COUNT(*) AS "allCount",
                   COUNT(*) FILTER (WHERE traffic_class = 'SUSPECTED_AUTOMATED') AS "suspectedAutomated",
                   COUNT(*) FILTER (WHERE traffic_class = 'UNCLASSIFIED') AS "unclassified"
              FROM click_events
             WHERE link_id = :linkId
               AND occurred_at >= :fromInclusive
               AND occurred_at < :toExclusive
               AND occurred_at > :retentionCutoffExclusive
             GROUP BY 1
             ORDER BY 1
            """, nativeQuery = true)
    List<DailyTrafficTotals> aggregateDaily(
            @Param("linkId") long linkId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive,
            @Param("retentionCutoffExclusive") Instant retentionCutoffExclusive);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ClickEventEntity event where event.occurredAt <= :cutoffInclusive")
    int deleteExpired(@Param("cutoffInclusive") Instant cutoffInclusive);

    interface TrafficTotals {
        long getAllCount();
        long getSuspectedAutomated();
        long getUnclassified();
    }

    interface DailyTrafficTotals extends TrafficTotals {
        Instant getBucketStart();
    }
}
