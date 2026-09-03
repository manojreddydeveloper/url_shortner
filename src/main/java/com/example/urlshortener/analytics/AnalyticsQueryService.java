package com.example.urlshortener.analytics;

import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import com.example.urlshortener.persistence.DatabaseTimeBudget;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean({LinkRepository.class, ClickEventRepository.class})
public class AnalyticsQueryService {
    private static final Pattern CODE = Pattern.compile("[0-9A-Za-z]{10}");
    private static final Duration DEFAULT_RANGE = Duration.ofDays(30);
    private static final Duration MAXIMUM_RANGE = Duration.ofDays(90);
    private static final byte[] UNKNOWN_TOKEN_HASH = new byte[32];

    private final LinkRepository links;
    private final ClickEventRepository events;
    private final Clock clock;
    private final DatabaseTimeBudget timeBudget;

    @Autowired
    public AnalyticsQueryService(LinkRepository links, ClickEventRepository events, DatabaseTimeBudget timeBudget) {
        this(links, events, Clock.systemUTC(), timeBudget);
    }

    AnalyticsQueryService(LinkRepository links, ClickEventRepository events, Clock clock) {
        this(links, events, clock, null);
    }

    AnalyticsQueryService(LinkRepository links, ClickEventRepository events, Clock clock, DatabaseTimeBudget timeBudget) {
        this.links = links;
        this.events = events;
        this.clock = clock;
        this.timeBudget = timeBudget;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Result query(String code, String token, String fromValue, String toValue, String bucketValue) {
        if (token == null || token.isBlank()) throw new AuthenticationRequiredException();
        if (code == null || !CODE.matcher(code).matches()) throw new ValidationException();
        if (bucketValue != null && !bucketValue.equals("day")) throw new ValidationException();

        Instant asOf = clock.instant();
        Instant from = parseOrDefault(fromValue, asOf.minus(DEFAULT_RANGE));
        Instant to = parseOrDefault(toValue, asOf);
        validateRange(from, to);

        TokenDigest candidate = tokenDigest(token);
        try {
            if (timeBudget != null) timeBudget.apply(DatabaseTimeBudget.Operation.ANALYTICS_QUERY);
            LinkEntity link = links.findByShortCode(code).orElse(null);
            byte[] expectedHash = link == null ? UNKNOWN_TOKEN_HASH : link.getAnalyticsTokenHash();
            boolean tokenMatches = expectedHash != null
                    && MessageDigest.isEqual(expectedHash, candidate.hash());
            if (link == null || !candidate.valid() || !tokenMatches) throw new NotFoundException();

            Instant retentionCutoff = asOf.minus(MAXIMUM_RANGE);
            ClickEventRepository.TrafficTotals totals = events.aggregateTotals(
                    link.getId(), from, to, retentionCutoff);
            List<Bucket> buckets = events.aggregateDaily(link.getId(), from, to, retentionCutoff)
                    .stream()
                    .map(value -> new Bucket(
                            value.getBucketStart(),
                            value.getAllCount(),
                            value.getSuspectedAutomated(),
                            value.getUnclassified()))
                    .toList();
            return new Result(
                    code,
                    from,
                    to,
                    new Totals(
                            totals.getAllCount(),
                            totals.getSuspectedAutomated(),
                            totals.getUnclassified()),
                    buckets,
                    asOf);
        } catch (DataAccessException exception) {
            throw new QueryUnavailableException();
        }
    }

    private static Instant parseOrDefault(String value, Instant defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException();
        }
    }

    private static void validateRange(Instant from, Instant to) {
        try {
            if (from.isAfter(to) || Duration.between(from, to).compareTo(MAXIMUM_RANGE) > 0) {
                throw new ValidationException();
            }
        } catch (ArithmeticException exception) {
            throw new ValidationException();
        }
    }

    private static TokenDigest tokenDigest(String token) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            return new TokenDigest(sha256(decoded), decoded.length == 32);
        } catch (IllegalArgumentException exception) {
            return new TokenDigest(sha256(new byte[0]), false);
        }
    }

    private static byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private record TokenDigest(byte[] hash, boolean valid) { }

    public record Result(
            String code,
            Instant from,
            Instant to,
            Totals totals,
            List<Bucket> buckets,
            Instant asOf) { }

    public record Totals(long all, long suspectedAutomated, long unclassified) { }

    public record Bucket(
            Instant start,
            long all,
            long suspectedAutomated,
            long unclassified) { }

    public static final class ValidationException extends RuntimeException { }
    public static final class AuthenticationRequiredException extends RuntimeException { }
    public static final class NotFoundException extends RuntimeException { }
    public static final class QueryUnavailableException extends RuntimeException { }
}
