package com.example.urlshortener.analytics;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
class ClickEventRepositoryImpl implements ClickEventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TrafficTotals aggregateTotals(long linkId, Instant fromInclusive, Instant toExclusive,
            Instant retentionCutoffExclusive) {
        List<ClickEventEntity> events = queryEvents(linkId, fromInclusive, toExclusive, retentionCutoffExclusive);
        long suspectedAutomated = 0;
        long unclassified = 0;
        for (ClickEventEntity event : events) {
            if (event.getTrafficClass() == ClickEvent.TrafficClass.SUSPECTED_AUTOMATED) {
                suspectedAutomated++;
            } else if (event.getTrafficClass() == ClickEvent.TrafficClass.UNCLASSIFIED) {
                unclassified++;
            }
        }
        long total = events.size();
        return new Totals(total, suspectedAutomated, unclassified);
    }

    @Override
    public List<DailyTrafficTotals> aggregateDaily(long linkId, Instant fromInclusive, Instant toExclusive,
            Instant retentionCutoffExclusive) {
        List<ClickEventEntity> events = queryEvents(linkId, fromInclusive, toExclusive, retentionCutoffExclusive);
        Map<Instant, MutableTotals> totalsByBucket = new LinkedHashMap<>();
        for (ClickEventEntity event : events.stream()
                .sorted(Comparator.comparing(ClickEventEntity::getOccurredAt))
                .toList()) {
            Instant bucketStart = event.getOccurredAt().truncatedTo(ChronoUnit.DAYS);
            MutableTotals totals = totalsByBucket.computeIfAbsent(bucketStart, ignored -> new MutableTotals());
            totals.accept(event.getTrafficClass());
        }
        List<DailyTrafficTotals> result = new ArrayList<>(totalsByBucket.size());
        for (Map.Entry<Instant, MutableTotals> entry : totalsByBucket.entrySet()) {
            result.add(new DailyTotals(entry.getKey(), entry.getValue().all, entry.getValue().suspectedAutomated,
                    entry.getValue().unclassified));
        }
        return result;
    }

    private List<ClickEventEntity> queryEvents(long linkId, Instant fromInclusive, Instant toExclusive,
            Instant retentionCutoffExclusive) {
        return entityManager.createQuery("""
                        select event
                          from ClickEventEntity event
                         where event.linkId = :linkId
                           and event.occurredAt >= :fromInclusive
                           and event.occurredAt < :toExclusive
                           and event.occurredAt > :retentionCutoffExclusive
                        """, ClickEventEntity.class)
                .setParameter("linkId", linkId)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("retentionCutoffExclusive", retentionCutoffExclusive)
                .getResultList();
    }

    private record Totals(long allCount, long suspectedAutomated, long unclassified)
            implements TrafficTotals {
        @Override public long getAllCount() { return allCount; }
        @Override public long getSuspectedAutomated() { return suspectedAutomated; }
        @Override public long getUnclassified() { return unclassified; }
    }

    private record DailyTotals(Instant bucketStart, long allCount, long suspectedAutomated, long unclassified)
            implements DailyTrafficTotals {
        @Override public Instant getBucketStart() { return bucketStart; }
        @Override public long getAllCount() { return allCount; }
        @Override public long getSuspectedAutomated() { return suspectedAutomated; }
        @Override public long getUnclassified() { return unclassified; }
    }

    private static final class MutableTotals {
        private long all;
        private long suspectedAutomated;
        private long unclassified;

        void accept(ClickEvent.TrafficClass trafficClass) {
            all++;
            if (trafficClass == ClickEvent.TrafficClass.SUSPECTED_AUTOMATED) {
                suspectedAutomated++;
            } else if (trafficClass == ClickEvent.TrafficClass.UNCLASSIFIED) {
                unclassified++;
            }
        }
    }
}
