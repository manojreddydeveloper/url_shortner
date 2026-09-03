package com.example.urlshortener.web;

import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Operation;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import com.example.urlshortener.observability.RateLimiter;
import com.example.urlshortener.web.error.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public final class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final OperationalMetrics metrics;

    public RateLimitInterceptor(RateLimiter rateLimiter, OperationalMetrics metrics) {
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();
        boolean isCreation = isCreationRequest(request, path);
        boolean isAnalytics = isAnalyticsRequest(request, path);

        if (!isCreation && !isAnalytics) {
            return true;
        }

        RateLimiter.RateLimitResult result;
        Operation operation;

        if (isCreation) {
            result = rateLimiter.allowCreation(request);
            operation = Operation.CREATION;
        } else {
            String token = bearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
            if (token == null) {
                return true;
            }
            result = rateLimiter.allowAnalyticsQuery(token, request);
            operation = Operation.ANALYTICS;
        }

        if (result.allowed()) {
            metrics.rateLimit(operation, Outcome.ALLOWED);
            return true;
        }

        metrics.rateLimit(operation, Outcome.REJECTED);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":{\"code\":\"RATE_LIMITED\","
                + "\"message\":\"Rate limit exceeded. Please retry after "
                + result.retryAfterSeconds() + " seconds.\","
                + "\"requestId\":\"" + requestId(request) + "\","
                + "\"details\":[]}}");
        response.getWriter().flush();
        return false;
    }

    private static boolean isCreationRequest(HttpServletRequest request, String path) {
        return "/api/v1/links".equals(path)
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private static boolean isAnalyticsRequest(HttpServletRequest request, String path) {
        return path.matches("/api/v1/links/[^/]+/analytics")
                && "GET".equalsIgnoreCase(request.getMethod());
    }

    private static String bearerToken(String authorization) {
        if (authorization == null
                || authorization.length() <= 7
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorization.substring(7);
        return token.isBlank() ? null : token;
    }

    private static String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(
                RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);
        return requestId instanceof String value ? value : "unavailable";
    }
}
