package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.urlshortener.analytics.ClickEvent;
import com.example.urlshortener.analytics.ClickEventEntity;
import com.example.urlshortener.analytics.ClickEventRepository;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class ClickEventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    ClickEventRepository clickEventRepository;

    @Autowired
    LinkRepository linkRepository;

    private LinkEntity testLink;

    @BeforeEach
    void setUp() {
        clickEventRepository.deleteAll();
        linkRepository.deleteAll();
        byte[] hash = new byte[32];
        java.util.Arrays.fill(hash, (byte) 5);
        testLink = linkRepository.saveAndFlush(
                new LinkEntity("aB3kP9mQ2x", "https://example.com", hash, Instant.now()));
    }

    @Test
    void savesClickEventWithValidForeignKey() {
        ClickEvent event = new ClickEvent(testLink.getId(), Instant.now(),
                ClickEvent.TrafficClass.UNCLASSIFIED);
        clickEventRepository.saveAndFlush(new ClickEventEntity(event));
        assertThat(clickEventRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsClickEventWithInvalidForeignKey() {
        ClickEvent event = new ClickEvent(99999L, Instant.now(),
                ClickEvent.TrafficClass.UNCLASSIFIED);
        assertThatThrownBy(() -> {
            clickEventRepository.saveAndFlush(new ClickEventEntity(event));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aggregateTotalsReturnsCorrectCounts() {
        Instant now = Instant.now();
        saveEvent(testLink.getId(), now.minus(Duration.ofDays(10)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(testLink.getId(), now.minus(Duration.ofDays(5)), ClickEvent.TrafficClass.SUSPECTED_AUTOMATED);
        saveEvent(testLink.getId(), now.minus(Duration.ofDays(2)), ClickEvent.TrafficClass.UNCLASSIFIED);

        ClickEventRepository.TrafficTotals totals = clickEventRepository.aggregateTotals(
                testLink.getId(),
                now.minus(Duration.ofDays(30)),
                now,
                now.minus(Duration.ofDays(90)));

        assertThat(totals.getAllCount()).isEqualTo(3);
        assertThat(totals.getSuspectedAutomated()).isEqualTo(1);
        assertThat(totals.getUnclassified()).isEqualTo(2);
    }

    @Test
    void aggregateDailyReturnsCorrectBuckets() {
        Instant now = Instant.now();
        Instant day1 = now.truncatedTo(java.time.temporal.ChronoUnit.DAYS).minus(Duration.ofDays(2));
        Instant day2 = now.truncatedTo(java.time.temporal.ChronoUnit.DAYS).minus(Duration.ofDays(1));

        saveEvent(testLink.getId(), day1.plus(Duration.ofHours(10)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(testLink.getId(), day1.plus(Duration.ofHours(14)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(testLink.getId(), day2.plus(Duration.ofHours(8)), ClickEvent.TrafficClass.SUSPECTED_AUTOMATED);

        List<ClickEventRepository.DailyTrafficTotals> daily = clickEventRepository.aggregateDaily(
                testLink.getId(),
                now.minus(Duration.ofDays(30)),
                now,
                now.minus(Duration.ofDays(90)));

        assertThat(daily).hasSize(2);
        assertThat(daily.get(0).getAllCount()).isEqualTo(2);
        assertThat(daily.get(0).getBucketStart()).isEqualTo(day1);
        assertThat(daily.get(1).getAllCount()).isEqualTo(1);
        assertThat(daily.get(1).getBucketStart()).isEqualTo(day2);
    }

    @Test
    void deleteExpiredRemovesOldEvents() {
        Instant now = Instant.now();
        Instant old = now.minus(Duration.ofDays(91));
        Instant recent = now.minus(Duration.ofDays(10));

        saveEvent(testLink.getId(), old, ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(testLink.getId(), recent, ClickEvent.TrafficClass.UNCLASSIFIED);
        assertThat(clickEventRepository.count()).isEqualTo(2);

        int deleted = clickEventRepository.deleteExpired(now.minus(Duration.ofDays(90)));
        assertThat(deleted).isEqualTo(1);
        assertThat(clickEventRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteExpiredDoesNotDeleteRecentEvents() {
        Instant now = Instant.now();
        saveEvent(testLink.getId(), now.minus(Duration.ofDays(10)), ClickEvent.TrafficClass.UNCLASSIFIED);

        int deleted = clickEventRepository.deleteExpired(now.minus(Duration.ofDays(90)));
        assertThat(deleted).isEqualTo(0);
        assertThat(clickEventRepository.count()).isEqualTo(1);
    }

    @Test
    void retentionBoundaryExcludesOldEventsFromAggregates() {
        Instant now = Instant.now();
        Instant old = now.minus(Duration.ofDays(91));
        Instant recent = now.minus(Duration.ofDays(10));

        saveEvent(testLink.getId(), old, ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(testLink.getId(), recent, ClickEvent.TrafficClass.UNCLASSIFIED);

        ClickEventRepository.TrafficTotals totals = clickEventRepository.aggregateTotals(
                testLink.getId(),
                now.minus(Duration.ofDays(30)),
                now,
                now.minus(Duration.ofDays(90)));

        assertThat(totals.getAllCount()).isEqualTo(1);
    }

    private void saveEvent(long linkId, Instant occurredAt, ClickEvent.TrafficClass trafficClass) {
        clickEventRepository.saveAndFlush(
                new ClickEventEntity(new ClickEvent(linkId, occurredAt, trafficClass)));
    }
}
