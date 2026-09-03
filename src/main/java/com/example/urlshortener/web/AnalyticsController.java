package com.example.urlshortener.web;

import com.example.urlshortener.analytics.AnalyticsQueryService;
import com.example.urlshortener.analytics.ClickEventRepository;
import com.example.urlshortener.persistence.LinkRepository;
import com.example.urlshortener.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean({LinkRepository.class, ClickEventRepository.class})
@RequestMapping("/api/v1/links")
public class AnalyticsController {
    private final AnalyticsQueryService service;

    public AnalyticsController(AnalyticsQueryService service) { this.service = service; }

    @GetMapping(value = "/{code}/analytics", produces = "application/json")
    public ResponseEntity<Response> analytics(
            @PathVariable String code,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String bucket) {
        try {
            AnalyticsQueryService.Result result = service.query(
                    code, bearerToken(authorization), from, to, bucket);
            return ResponseEntity.ok(Response.from(result));
        } catch (AnalyticsQueryService.AuthenticationRequiredException exception) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTHENTICATION_REQUIRED",
                    "An analytics token is required.");
        } catch (AnalyticsQueryService.ValidationException exception) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "The request contains invalid fields.");
        } catch (AnalyticsQueryService.NotFoundException exception) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "NOT_FOUND",
                    "The requested link was not found.");
        } catch (AnalyticsQueryService.QueryUnavailableException exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "DEPENDENCY_UNAVAILABLE",
                    "Analytics is temporarily unavailable.");
        }
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

    public record Response(
            String code,
            Instant from,
            Instant to,
            String bucket,
            Totals totals,
            List<Bucket> buckets,
            Instant asOf) {
        static Response from(AnalyticsQueryService.Result result) {
            return new Response(
                    result.code(),
                    result.from(),
                    result.to(),
                    "day",
                    new Totals(
                            result.totals().all(),
                            result.totals().suspectedAutomated(),
                            result.totals().unclassified()),
                    result.buckets().stream()
                            .map(value -> new Bucket(
                                    value.start(),
                                    value.all(),
                                    value.suspectedAutomated(),
                                    value.unclassified()))
                            .toList(),
                    result.asOf());
        }
    }

    public record Totals(long all, long suspectedAutomated, long unclassified) { }

    public record Bucket(
            Instant start,
            long all,
            long suspectedAutomated,
            long unclassified) { }
}
