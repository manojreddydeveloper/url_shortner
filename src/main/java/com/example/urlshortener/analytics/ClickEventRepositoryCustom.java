package com.example.urlshortener.analytics;

import java.time.Instant;
import java.util.List;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
interface ClickEventRepositoryCustom {
    TrafficTotals aggregateTotals(
            long linkId,
            Instant fromInclusive,
            Instant toExclusive,
            Instant retentionCutoffExclusive);

    List<DailyTrafficTotals> aggregateDaily(
            long linkId,
            Instant fromInclusive,
            Instant toExclusive,
            Instant retentionCutoffExclusive);

    interface TrafficTotals {
        long getAllCount();
        long getSuspectedAutomated();
        long getUnclassified();
    }

    interface DailyTrafficTotals extends TrafficTotals {
        Instant getBucketStart();
    }
}
