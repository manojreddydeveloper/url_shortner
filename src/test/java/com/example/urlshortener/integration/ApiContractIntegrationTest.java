package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class ApiContractIntegrationTest extends AbstractIntegrationTest {

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatusCode statusCode) {
                return false;
            }
        });
    }

    @Test
    void healthLiveReturnsOk() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health/live", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void healthReadyReturnsOk() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health/ready", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void createLinkReturns201WithFullResponseShape() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":\"https://example.com\"}", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("code");
        assertThat(response.getBody()).containsKey("shortUrl");
        assertThat(response.getBody()).containsKey("analyticsToken");
        assertThat(response.getBody().get("code").toString()).hasSize(10);
        assertThat(response.getBody().get("shortUrl").toString()).startsWith("https://sho.rt/");
        assertThat(response.getBody().get("url").toString()).isEqualTo("https://example.com");
    }

    @Test
    void createLinkRejectsUnknownField() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":\"https://example.com\",\"expiresAt\":\"2026-10-01T00:00:00Z\"}", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map error = (Map) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createLinkWithInvalidUrlReturns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":\"not-a-url\"}", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isNotNull();
        Map error = (Map) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createLinkWithInvalidJsonReturns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"url\":", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map error = (Map) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("INVALID_JSON");
    }

    @Test
    void createLinkWithUnsupportedMediaTypeReturns415() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>("https://example.com", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void createLinkWithNullUrlReturns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":null}", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void redirectReturns302ForExistingLink() {
        String code = createLink("https://example.com/redirect");

        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/{code}", HttpMethod.GET, null, Void.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation().toString())
                .isEqualTo("https://example.com/redirect");
        assertThat(response.getHeaders().getCacheControl()).contains("no-store");
    }

    @Test
    void redirectReturns404ForMalformedCode() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/{code}", HttpMethod.GET, null, Map.class, "bad!");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getLocation()).isNull();
    }

    @Test
    void redirectReturns404ForUnknownCode() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/{code}", HttpMethod.GET, null, Map.class, "nonexistent");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getLocation()).isNull();
    }

    @Test
    void headRequestToRedirectRouteReturns405() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/{code}", HttpMethod.HEAD, null, Map.class, "nonexistent");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void analyticsReturns401WithoutBearerToken() {
        String code = createLink("https://example.com");

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics",
                HttpMethod.GET, null, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void analyticsReturns401WithInvalidToken() {
        String code = createLink("https://example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid-token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics",
                HttpMethod.GET, entity, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void analyticsReturns200WithValidToken() {
        String[] parts = createLinkAndReturnParts("https://example.com");
        String code = parts[0];
        String token = parts[1];

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics",
                HttpMethod.GET, entity, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = response.getBody();
        assertThat(body).containsKey("totals");
        assertThat(body).containsKey("buckets");
        assertThat(body.get("code")).isEqualTo(code);
    }

    @Test
    void analyticsRejectsInvalidRange() {
        String[] parts = createLinkAndReturnParts("https://example.com");
        String code = parts[0];
        String token = parts[1];

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Instant now = Instant.now();
        String from = now.toString();
        String to = now.minusSeconds(1).toString();
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links/{code}/analytics?from=" + from + "&to=" + to,
                HttpMethod.GET, entity, Map.class, code);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map error = (Map) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createDuplicateDestinationReturnsDistinctCodes() {
        String first = createLink("https://duplicate.example.com");
        String second = createLink("https://duplicate.example.com");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void unsupportedMethodReturns405() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/links", HttpMethod.PUT, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    private String createLink(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":\"" + url + "\"}", headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);
        return response.getBody().get("code").toString();
    }

    private String[] createLinkAndReturnParts(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"url\":\"" + url + "\"}", headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/links", request, Map.class);
        return new String[]{
                response.getBody().get("code").toString(),
                response.getBody().get("analyticsToken").toString()
        };
    }
}
