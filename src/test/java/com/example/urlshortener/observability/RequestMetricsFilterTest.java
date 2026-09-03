package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RequestMetricsFilterTest {
    @Test
    void normalizesRoutesWithoutUsingPathParametersAsTags() throws Exception {
        assertThat(RequestMetricsFilter.operation("POST", "/api/v1/links")).isEqualTo(Operation.CREATION);
        assertThat(RequestMetricsFilter.operation("GET", "/api/v1/links/aZ3kP9mQ2x/analytics"))
                .isEqualTo(Operation.ANALYTICS);
        assertThat(RequestMetricsFilter.operation("GET", "/health/live")).isEqualTo(Operation.LIVENESS);
        assertThat(RequestMetricsFilter.operation("GET", "/health/ready")).isEqualTo(Operation.READINESS);
        assertThat(RequestMetricsFilter.operation("GET", "/aZ3kP9mQ2x")).isEqualTo(Operation.REDIRECT);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RequestMetricsFilter filter = new RequestMetricsFilter(new OperationalMetrics(registry));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/aZ3kP9mQ2x");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(404);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(registry.find("url_shortener.requests")
                .tags("operation", "redirect", "outcome", "unknown", "status_class", "4xx")
                .counter().count()).isEqualTo(1);
        assertThat(registry.getMeters()).allSatisfy(meter ->
                assertThat(meter.getId().getTag("operation")).isNotEqualTo("aZ3kP9mQ2x"));
    }

    @Test
    void classifiesOnlyFixedOutcomeValues() {
        assertThat(RequestMetricsFilter.outcome(201)).isEqualTo(Outcome.SUCCESS);
        assertThat(RequestMetricsFilter.outcome(400)).isEqualTo(Outcome.VALIDATION_REJECTION);
        assertThat(RequestMetricsFilter.outcome(401)).isEqualTo(Outcome.AUTHENTICATION_REQUIRED);
        assertThat(RequestMetricsFilter.outcome(429)).isEqualTo(Outcome.RATE_LIMITED);
        assertThat(RequestMetricsFilter.outcome(503)).isEqualTo(Outcome.DEPENDENCY_FAILURE);
        assertThat(RequestMetricsFilter.outcome(500)).isEqualTo(Outcome.INTERNAL_FAILURE);
    }

    @Test
    void classifiesUnrecognizedRouteAsOther() {
        assertThat(RequestMetricsFilter.operation("DELETE", "/api/v1/links"))
                .isEqualTo(Operation.OTHER);
        assertThat(RequestMetricsFilter.operation("PUT", "/api/v1/links/abc123"))
                .isEqualTo(Operation.OTHER);
        assertThat(RequestMetricsFilter.operation("POST", "/unknown"))
                .isEqualTo(Operation.OTHER);
    }

    @Test
    void filterRecordsMetricsForAnyRequest() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RequestMetricsFilter filter = new RequestMetricsFilter(new OperationalMetrics(registry));
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/links");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(204);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(registry.find("url_shortener.requests")
                .tags("operation", "other", "outcome", "success", "status_class", "2xx")
                .counter().count()).isEqualTo(1);
    }
}
