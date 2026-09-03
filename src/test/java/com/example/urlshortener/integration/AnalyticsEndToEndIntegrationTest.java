package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import com.example.urlshortener.analytics.ClickEvent;
import com.example.urlshortener.analytics.ClickEventEntity;
import com.example.urlshortener.analytics.ClickEventRepository;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;

class AnalyticsEndToEndIntegrationTest extends AbstractIntegrationTest {

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
    void fullAnalyticsLifecycle() {
        Instant now = Instant.now();

        saveEvent(now.minus(Duration.ofDays(5)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(now.minus(Duration.ofDays(5)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(now.minus(Duration.ofDays(3)), ClickEvent.TrafficClass.SUSPECTED_AUTOMATED);
        saveEvent(now.minus(Duration.ofDays(1)), ClickEvent.TrafficClass.UNCLASSIFIED);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics",
                HttpMethod.GET, entity, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = response.getBody();
        assertThat(body.get("code")).isEqualTo(code);

        Map totals = (Map) body.get("totals");
        assertThat(((Number) totals.get("all")).longValue()).isEqualTo(4L);
        assertThat(((Number) totals.get("suspectedAutomated")).longValue()).isEqualTo(1L);
        assertThat(((Number) totals.get("unclassified")).longValue()).isEqualTo(3L);

        assertThat(body.get("buckets")).isNotNull();
    }

    @Test
    void analyticsWithCustomRange() {
        Instant now = Instant.now();
        saveEvent(now.minus(Duration.ofDays(20)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(now.minus(Duration.ofDays(5)), ClickEvent.TrafficClass.UNCLASSIFIED);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String from = now.minus(Duration.ofDays(10)).toString();
        String to = now.toString();

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics?from=" + from + "&to=" + to,
                HttpMethod.GET, entity, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map totals = (Map) response.getBody().get("totals");
        assertThat(((Number) totals.get("all")).longValue()).isEqualTo(1L);
    }

    @Test
    void analyticsWithDefaultRangeReturns30Days() {
        Instant now = Instant.now();
        saveEvent(now.minus(Duration.ofDays(5)), ClickEvent.TrafficClass.UNCLASSIFIED);
        saveEvent(now.minus(Duration.ofDays(35)), ClickEvent.TrafficClass.UNCLASSIFIED);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics",
                HttpMethod.GET, entity, Map.class, code);

        Map totals = (Map) response.getBody().get("totals");
        assertThat(((Number) totals.get("all")).longValue()).isEqualTo(1L);
    }

    @Test
    void analyticsTokenForLinkADeniesAccessToLinkB() {
        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> createRequest = new HttpEntity<>(
                "{\"url\":\"https://other.com\"}", createHeaders);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", createRequest, Map.class);
        String otherToken = createResponse.getBody().get("analyticsToken").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + otherToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics",
                HttpMethod.GET, entity, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void saveEvent(Instant occurredAt, ClickEvent.TrafficClass trafficClass) {
        LinkEntity link = linkRepository.findByShortCode(code).orElseThrow();
        clickEventRepository.saveAndFlush(
                new ClickEventEntity(new ClickEvent(link.getId(), occurredAt, trafficClass)));
    }
}
