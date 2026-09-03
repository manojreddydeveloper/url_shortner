package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.urlshortener.analytics.ClickEvent;
import com.example.urlshortener.analytics.ClickEventEntity;
import com.example.urlshortener.analytics.ClickEventRepository;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

class TimeBoundaryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    ClickEventRepository clickEventRepository;

    @Autowired
    LinkRepository linkRepository;

    private RestTemplate restTemplate;
    private String code;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        clickEventRepository.deleteAll();
        linkRepository.deleteAll();
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatusCode statusCode) {
                return false;
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":\"https://example.com\"}", headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);
        code = response.getBody().get("code").toString();
        token = response.getBody().get("analyticsToken").toString();
    }

    @Test
    void eventsExactlyAt90DayBoundaryAreExcludedFromAggregates() {
        Instant now = Instant.now();
        Instant exactlyAtBoundary = now.minus(Duration.ofDays(90));

        saveEvent(exactlyAtBoundary, ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(now.minus(Duration.ofDays(10)), ClickEvent.TrafficClass.UNCLASSIFIED);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String from = now.minus(Duration.ofDays(90)).toString();
        String to = now.toString();
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics?from=" + from + "&to=" + to,
                HttpMethod.GET, entity, Map.class, code);

        Map totals = (Map) response.getBody().get("totals");
        assertThat(((Number) totals.get("all")).longValue()).isEqualTo(1L);
    }

    @Test
    void eventsOneSecondAfterBoundaryAreIncludedInAggregates() {
        Instant now = Instant.now();
        Instant oneSecondAfterBoundary = now.minus(Duration.ofDays(90)).plus(Duration.ofMinutes(1));

        saveEvent(oneSecondAfterBoundary, ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(now.minus(Duration.ofDays(10)), ClickEvent.TrafficClass.UNCLASSIFIED);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String from = now.minus(Duration.ofDays(90)).toString();
        String to = now.toString();
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics?from=" + from + "&to=" + to,
                HttpMethod.GET, entity, Map.class, code);

        Map totals = (Map) response.getBody().get("totals");
        assertThat(((Number) totals.get("all")).longValue()).isEqualTo(2L);
    }

    @Test
    void deleteExpiredRemovesEventsExactlyAtBoundary() {
        Instant now = Instant.now();
        Instant exactlyAtBoundary = now.minus(Duration.ofDays(90));
        Instant oneSecondBeforeBoundary = now.minus(Duration.ofDays(90)).minus(Duration.ofSeconds(1));
        Instant oneSecondAfterBoundary = now.minus(Duration.ofDays(90)).plus(Duration.ofSeconds(1));

        saveEvent(oneSecondBeforeBoundary, ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(exactlyAtBoundary, ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(oneSecondAfterBoundary, ClickEvent.TrafficClass.UNCLASSIFIED);

        int deleted = clickEventRepository.deleteExpired(exactlyAtBoundary);
        assertThat(deleted).isEqualTo(2);
        assertThat(clickEventRepository.count()).isEqualTo(1);
    }

    @Test
    void dailyBucketsAreUTCAligned() {
        Instant now = Instant.now();
        Instant utcDay = now.truncatedTo(java.time.temporal.ChronoUnit.DAYS).minus(Duration.ofDays(1));

        saveEvent(utcDay.plus(Duration.ofHours(2)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(utcDay.plus(Duration.ofHours(14)), ClickEvent.TrafficClass.UNCLASSIFIED);

        LinkEntity link = linkRepository.findByShortCode(code).orElseThrow();
        var daily = clickEventRepository.aggregateDaily(
                link.getId(),
                now.minus(Duration.ofDays(30)),
                now,
                now.minus(Duration.ofDays(90)));

        assertThat(daily).hasSize(1);
        assertThat(daily.get(0).getBucketStart()).isEqualTo(utcDay);
        assertThat(daily.get(0).getAllCount()).isEqualTo(2);
    }

    private void saveEvent(Instant occurredAt, ClickEvent.TrafficClass trafficClass) {
        LinkEntity link = linkRepository.findByShortCode(code).orElseThrow();
        clickEventRepository.saveAndFlush(
                new ClickEventEntity(new ClickEvent(link.getId(), occurredAt, trafficClass)));
    }
}
