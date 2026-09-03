package com.example.urlshortener.observability;

import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class RequestMetricsFilter extends OncePerRequestFilter {
    private final OperationalMetrics metrics;

    public RequestMetricsFilter(OperationalMetrics metrics) { this.metrics = metrics; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long started = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            metrics.request(operation(request.getMethod(), request.getRequestURI()), outcome(status),
                    status, System.nanoTime() - started);
        }
    }

    static Operation operation(String method, String path) {
        if ("POST".equals(method) && "/api/v1/links".equals(path)) return Operation.CREATION;
        if ("GET".equals(method) && path.matches("/api/v1/links/[^/]+/analytics")) return Operation.ANALYTICS;
        if ("GET".equals(method) && "/health/live".equals(path)) return Operation.LIVENESS;
        if ("GET".equals(method) && "/health/ready".equals(path)) return Operation.READINESS;
        if ("GET".equals(method) && path.matches("/[^/]+")) return Operation.REDIRECT;
        return Operation.OTHER;
    }

    static Outcome outcome(int status) {
        if (status < 400) return Outcome.SUCCESS;
        return switch (status) {
            case 400 -> Outcome.VALIDATION_REJECTION;
            case 401 -> Outcome.AUTHENTICATION_REQUIRED;
            case 404 -> Outcome.UNKNOWN;
            case 429 -> Outcome.RATE_LIMITED;
            case 503 -> Outcome.DEPENDENCY_FAILURE;
            default -> Outcome.INTERNAL_FAILURE;
        };
    }
}
