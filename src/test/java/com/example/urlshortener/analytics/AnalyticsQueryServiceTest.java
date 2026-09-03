package com.example.urlshortener.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

class AnalyticsQueryServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");
    private static final Instant DEFAULT_FROM = Instant.parse("2026-08-03T12:00:00Z");
    private static final Instant RETENTION_CUTOFF = Instant.parse("2026-06-04T12:00:00Z");
    private static final String CODE = "aZ3kP9mQ2x";

    private LinkRepository links;
    private ClickEventRepository events;
    private AnalyticsQueryService service;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        links = mock(LinkRepository.class);
        events = mock(ClickEventRepository.class);
        service = new AnalyticsQueryService(links, events, Clock.fixed(NOW, ZoneOffset.UTC));

        byte[] rawToken = new byte[32];
        java.util.Arrays.fill(rawToken, (byte) 7);
        token = Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken);
        LinkEntity link = mock(LinkEntity.class);
        when(link.getId()).thenReturn(42L);
        when(link.getAnalyticsTokenHash())
                .thenReturn(MessageDigest.getInstance("SHA-256").digest(rawToken));
        when(links.findByShortCode(CODE)).thenReturn(Optional.of(link));
    }

    @Test
    void returnsAuthorizedDefaultRangeTotalsAndUtcBuckets() {
        ClickEventRepository.TrafficTotals totals = totals(3, 1, 2);
        ClickEventRepository.DailyTrafficTotals daily = mock(
                ClickEventRepository.DailyTrafficTotals.class);
        when(daily.getBucketStart()).thenReturn(Instant.parse("2026-09-02T00:00:00Z"));
        when(daily.getAllCount()).thenReturn(3L);
        when(daily.getSuspectedAutomated()).thenReturn(1L);
        when(daily.getUnclassified()).thenReturn(2L);
        when(events.aggregateTotals(42L, DEFAULT_FROM, NOW, RETENTION_CUTOFF))
                .thenReturn(totals);
        when(events.aggregateDaily(42L, DEFAULT_FROM, NOW, RETENTION_CUTOFF))
                .thenReturn(List.of(daily));

        AnalyticsQueryService.Result result = service.query(CODE, token, null, null, null);

        assertEquals(DEFAULT_FROM, result.from());
        assertEquals(NOW, result.to());
        assertEquals(NOW, result.asOf());
        assertEquals(new AnalyticsQueryService.Totals(3, 1, 2), result.totals());
        assertEquals(List.of(new AnalyticsQueryService.Bucket(
                Instant.parse("2026-09-02T00:00:00Z"), 3, 1, 2)), result.buckets());
    }

    @Test
    void returnsZeroTotalsAndNoBucketsForAnEmptyAuthorizedRange() {
        ClickEventRepository.TrafficTotals zeroTotals = totals(0, 0, 0);
        when(events.aggregateTotals(42L, NOW, NOW, RETENTION_CUTOFF))
                .thenReturn(zeroTotals);
        when(events.aggregateDaily(42L, NOW, NOW, RETENTION_CUTOFF))
                .thenReturn(List.of());

        AnalyticsQueryService.Result result = service.query(
                CODE, token, NOW.toString(), NOW.toString(), "day");

        assertEquals(new AnalyticsQueryService.Totals(0, 0, 0), result.totals());
        assertEquals(List.of(), result.buckets());
    }

    @Test
    void rejectsMissingAuthenticationBeforeDatabaseAccess() {
        assertThrows(AnalyticsQueryService.AuthenticationRequiredException.class,
                () -> service.query(CODE, null, null, null, null));
        verify(links, never()).findByShortCode(any());
    }

    @Test
    void invalidTokenAndUnknownLinkUseTheSameOutcome() {
        assertThrows(AnalyticsQueryService.NotFoundException.class,
                () -> service.query(CODE, "invalid-token", null, null, null));

        when(links.findByShortCode(CODE)).thenReturn(Optional.empty());
        assertThrows(AnalyticsQueryService.NotFoundException.class,
                () -> service.query(CODE, token, null, null, null));
    }

    @Test
    void rejectsInvalidCodeTimeAndBucketInputs() {
        assertThrows(AnalyticsQueryService.ValidationException.class,
                () -> service.query("bad", token, null, null, null));
        assertThrows(AnalyticsQueryService.ValidationException.class,
                () -> service.query(CODE, token, "not-an-instant", null, null));
        assertThrows(AnalyticsQueryService.ValidationException.class,
                () -> service.query(CODE, token, NOW.toString(), DEFAULT_FROM.toString(), null));
        assertThrows(AnalyticsQueryService.ValidationException.class,
                () -> service.query(
                        CODE,
                        token,
                        RETENTION_CUTOFF.minusSeconds(1).toString(),
                        NOW.toString(),
                        null));
        assertThrows(AnalyticsQueryService.ValidationException.class,
                () -> service.query(CODE, token, null, null, "hour"));
    }

    @Test
    void mapsLinkAndAggregateDatastoreFailuresToUnavailable() {
        when(links.findByShortCode(CODE))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        assertThrows(AnalyticsQueryService.QueryUnavailableException.class,
                () -> service.query(CODE, token, null, null, null));

        setUpAuthorizedZeroTotals();
        when(events.aggregateDaily(eq(42L), any(), any(), any()))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        assertThrows(AnalyticsQueryService.QueryUnavailableException.class,
                () -> service.query(CODE, token, null, null, null));
    }

    @Test
    void usesOneRepeatableReadTransactionForAuthorizationAndAggregates() throws Exception {
        Transactional transaction = AnalyticsQueryService.class
                .getMethod("query", String.class, String.class, String.class, String.class, String.class)
                .getAnnotation(Transactional.class);

        assertEquals(true, transaction.readOnly());
        assertEquals(Isolation.REPEATABLE_READ, transaction.isolation());
    }

    private ClickEventRepository.TrafficTotals totals(long all, long suspected, long unclassified) {
        ClickEventRepository.TrafficTotals totals = mock(ClickEventRepository.TrafficTotals.class);
        when(totals.getAllCount()).thenReturn(all);
        when(totals.getSuspectedAutomated()).thenReturn(suspected);
        when(totals.getUnclassified()).thenReturn(unclassified);
        return totals;
    }

    private void setUpAuthorizedZeroTotals() {
        try {
            setUp();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        ClickEventRepository.TrafficTotals zeroTotals = totals(0, 0, 0);
        when(events.aggregateTotals(eq(42L), any(), any(), any())).thenReturn(zeroTotals);
    }
}
