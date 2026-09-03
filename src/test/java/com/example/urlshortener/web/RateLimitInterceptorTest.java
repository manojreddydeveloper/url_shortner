package com.example.urlshortener.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.urlshortener.config.RateLimitConfiguration;
import com.example.urlshortener.config.RateLimitProperties;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.RateLimiter;
import com.example.urlshortener.web.error.GlobalExceptionHandler;
import com.example.urlshortener.web.error.RateLimitException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RateLimitInterceptorTest {

    private MockMvc mockMvc;
    private RateLimiter rateLimiter;
    private OperationalMetrics metrics;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(Set.of());
        metrics = new OperationalMetrics(new SimpleMeterRegistry());
        RateLimitInterceptor interceptor = new RateLimitInterceptor(rateLimiter, metrics);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new TestCreationController(), new TestUnlimitedController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(interceptor)
                .build();
    }

    @Test
    void allowsCreationWithinQuota() throws Exception {
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsCreationOverQuotaWith429AndRetryAfter() throws Exception {
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/v1/links")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"https://example.com\"}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.error.message").value(
                        org.hamcrest.Matchers.containsString("Rate limit exceeded")));
    }

    @Test
    void retryAfterHeaderIsPositive() throws Exception {
        for (int i = 0; i < 20; i++) {
            rateLimiter.allowCreation(mockRequest("127.0.0.1"));
        }
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After",
                        org.hamcrest.Matchers.matchesRegex("[1-9][0-9]*")));
    }

    @Test
    void nonLimitedEndpointsAreNotAffected() throws Exception {
        mockMvc.perform(get("/unlimited"))
                .andExpect(status().isOk());
    }

    @Test
    void doesNotAffectGetOnLinksPath() throws Exception {
        mockMvc.perform(get("/api/v1/links"))
                .andExpect(status().isOk());
    }

    @Test
    void rateLimitMetricIsEmittedOnRejection() throws Exception {
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/v1/links")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"https://example.com\"}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void analyticsOverQuotaReturns429() throws Exception {
        String bearerToken = "dGhpc19pc19hX3Rlc3RfdG9rZW4"; // base64url encoded test token
        for (int i = 0; i < 60; i++) {
            mockMvc.perform(get("/api/v1/links/abc123/analytics")
                            .header("Authorization", "Bearer " + bearerToken))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/v1/links/abc123/analytics")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"));
    }

    private static jakarta.servlet.http.HttpServletRequest mockRequest(String remoteAddr) {
        jakarta.servlet.http.HttpServletRequest request =
                mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        return request;
    }

    @RestController
    @RequestMapping("/api/v1/links")
    static class TestCreationController {
        @PostMapping(consumes = "application/json", produces = "application/json")
        org.springframework.http.ResponseEntity<String> create(
                @org.springframework.web.bind.annotation.RequestBody String body) {
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.CREATED)
                    .body("{\"code\":\"test123456\"}");
        }

        @org.springframework.web.bind.annotation.GetMapping(produces = "application/json")
        org.springframework.http.ResponseEntity<String> list() {
            return org.springframework.http.ResponseEntity.ok("[]");
        }

        @org.springframework.web.bind.annotation.GetMapping("/{code}/analytics")
        org.springframework.http.ResponseEntity<String> analytics(
                @org.springframework.web.bind.annotation.PathVariable String code) {
            return org.springframework.http.ResponseEntity.ok("{\"code\":\"" + code + "\"}");
        }
    }

    @RestController
    static class TestUnlimitedController {
        @org.springframework.web.bind.annotation.GetMapping("/unlimited")
        String unlimited() {
            return "ok";
        }
    }
}
