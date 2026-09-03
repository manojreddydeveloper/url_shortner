package com.example.urlshortener.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.urlshortener.analytics.AnalyticsQueryService;
import com.example.urlshortener.web.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AnalyticsControllerTest {
    private static final String CODE = "aZ3kP9mQ2x";
    private static final String TOKEN = "BwcHBwcHBwcHBwcHBwcHBwcHBwcHBwcHBwcHBwcHBwc";

    private AnalyticsQueryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AnalyticsQueryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalyticsController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsOnlyTheApprovedAggregateContract() throws Exception {
        when(service.query(CODE, TOKEN, null, null, null)).thenReturn(
                new AnalyticsQueryService.Result(
                        CODE,
                        Instant.parse("2026-08-03T12:00:00Z"),
                        Instant.parse("2026-09-02T12:00:00Z"),
                        new AnalyticsQueryService.Totals(3, 1, 2),
                        List.of(new AnalyticsQueryService.Bucket(
                                Instant.parse("2026-09-02T00:00:00Z"), 3, 1, 2)),
                        Instant.parse("2026-09-02T12:00:00Z")));

        mockMvc.perform(get("/api/v1/links/{code}/analytics", CODE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(CODE))
                .andExpect(jsonPath("$.bucket").value("day"))
                .andExpect(jsonPath("$.totals.all").value(3))
                .andExpect(jsonPath("$.totals.suspectedAutomated").value(1))
                .andExpect(jsonPath("$.totals.unclassified").value(2))
                .andExpect(jsonPath("$.buckets[0].start").value("2026-09-02T00:00:00Z"))
                .andExpect(jsonPath("$.asOf").value("2026-09-02T12:00:00Z"))
                .andExpect(content().string(Matchers.not(Matchers.containsString(TOKEN))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("userAgent"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("destination"))));
    }

    @Test
    void queryParameterCannotReplaceMissingAuthorizationHeader() throws Exception {
        when(service.query(CODE, null, null, null, null))
                .thenThrow(new AnalyticsQueryService.AuthenticationRequiredException());

        mockMvc.perform(get("/api/v1/links/{code}/analytics", CODE)
                        .queryParam("analyticsToken", TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void mapsInvalidUnknownAndDependencyOutcomesToSafeErrors() throws Exception {
        when(service.query(CODE, TOKEN, "bad", null, null))
                .thenThrow(new AnalyticsQueryService.ValidationException());
        mockMvc.perform(get("/api/v1/links/{code}/analytics", CODE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                        .queryParam("from", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        when(service.query(CODE, TOKEN, null, null, null))
                .thenThrow(new AnalyticsQueryService.NotFoundException());
        mockMvc.perform(get("/api/v1/links/{code}/analytics", CODE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));

        when(service.query(CODE, TOKEN, null, null, "day"))
                .thenThrow(new AnalyticsQueryService.QueryUnavailableException());
        mockMvc.perform(get("/api/v1/links/{code}/analytics", CODE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                        .queryParam("bucket", "day"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("DEPENDENCY_UNAVAILABLE"));
    }

    @Test
    void malformedAuthorizationSchemeIsTreatedAsMissing() throws Exception {
        when(service.query(CODE, null, null, null, null))
                .thenThrow(new AnalyticsQueryService.AuthenticationRequiredException());

        mockMvc.perform(get("/api/v1/links/{code}/analytics", CODE)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }
}
