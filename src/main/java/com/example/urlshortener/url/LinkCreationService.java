package com.example.urlshortener.url;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.urlshortener.cache.LinkCache;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import org.springframework.beans.factory.ObjectProvider;

@Service
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class LinkCreationService {
    private final DestinationUrlValidator validator;
    private final ShortCodeGenerator codeGenerator;
    private final LinkWriter writer;
    private final LinkCache cache;
    private final SecureRandom tokenRandom;
    private final Clock clock;
    private final OperationalMetrics metrics;

    @Autowired
    public LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkWriter writer, LinkCache cache, ObjectProvider<OperationalMetrics> metrics) {
        this(validator, codeGenerator, writer, cache, new SecureRandom(), Clock.systemUTC(), metrics.getIfAvailable());
    }
    public LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkRepository repository) {
        this(validator, codeGenerator, new LinkWriter(repository), new LinkCache(10_000), new SecureRandom(), Clock.systemUTC());
    }
    public LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkRepository repository, LinkCache cache, SecureRandom tokenRandom, Clock clock) {
        this(validator, codeGenerator, new LinkWriter(repository), cache, tokenRandom, clock);
    }
    LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkRepository repository, SecureRandom tokenRandom, Clock clock) {
        this(validator, codeGenerator, new LinkWriter(repository), new LinkCache(10_000), tokenRandom, clock);
    }
    LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkWriter writer, LinkCache cache, SecureRandom tokenRandom, Clock clock) {
        this(validator, codeGenerator, writer, cache, tokenRandom, clock, null);
    }
    private LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkWriter writer, LinkCache cache, SecureRandom tokenRandom, Clock clock, OperationalMetrics metrics) {
        this.validator = validator; this.codeGenerator = codeGenerator; this.writer = writer; this.cache = cache;
        this.tokenRandom = tokenRandom; this.clock = clock; this.metrics = metrics;
    }

    public Result create(String destination) {
        String url;
        try { url = validator.validate(destination); }
        catch (IllegalArgumentException exception) { metric(Outcome.VALIDATION_REJECTION); throw exception; }
        byte[] token = new byte[32]; tokenRandom.nextBytes(token);
        byte[] hash = sha256(token);
        for (int attempt = 1; attempt <= ShortCodeGenerator.MAX_ATTEMPTS; attempt++) {
            try {
                String code = codeGenerator.generate();
                Instant createdAt = Instant.now(clock);
                LinkEntity saved = writer.save(new LinkEntity(code, url, hash, createdAt));
                if (cache != null) {
                    cache.put(saved);
                    if (metrics != null) metrics.cache("write");
                }
                metric(Outcome.SUCCESS);
                return new Result(code, url, createdAt, Base64.getUrlEncoder().withoutPadding().encodeToString(token));
            } catch (DataIntegrityViolationException collision) {
                if (!isShortCodeCollision(collision)) { metric(Outcome.DEPENDENCY_FAILURE); throw new DependencyUnavailableException(); }
                if (!codeGenerator.mayRetryCollision(attempt)) {
                    metric(Outcome.COLLISION_EXHAUSTION);
                    throw new CreationUnavailableException();
                }
                metric(Outcome.COLLISION_RETRY);
            } catch (org.springframework.dao.DataAccessException exception) {
                metric(Outcome.DEPENDENCY_FAILURE);
                throw new DependencyUnavailableException();
            }
        }
        throw new CreationUnavailableException();
    }

    private void metric(Outcome outcome) {
        if (metrics == null) return;
        if (outcome == Outcome.COLLISION_RETRY || outcome == Outcome.COLLISION_EXHAUSTION) metrics.collision(outcome);
        else metrics.creation(outcome);
    }

    private static byte[] sha256(byte[] value) { try { return MessageDigest.getInstance("SHA-256").digest(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private static boolean isShortCodeCollision(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current.getMessage() != null && current.getMessage().contains("uk_links_short_code")) return true;
        }
        return false;
    }
    public record Result(String code, String url, Instant createdAt, String analyticsToken) { }
    public static final class CreationUnavailableException extends RuntimeException { }
    public static final class DependencyUnavailableException extends RuntimeException { }
}
