package com.example.urlshortener.url;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;

@Service
@ConditionalOnBean(LinkRepository.class)
public class LinkCreationService {
    private final DestinationUrlValidator validator;
    private final ShortCodeGenerator codeGenerator;
    private final LinkWriter writer;
    private final SecureRandom tokenRandom;
    private final Clock clock;

    @Autowired
    public LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkWriter writer) {
        this(validator, codeGenerator, writer, new SecureRandom(), Clock.systemUTC());
    }
    public LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkRepository repository) { this(validator, codeGenerator, new LinkWriter(repository), new SecureRandom(), Clock.systemUTC()); }
    LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkRepository repository, SecureRandom tokenRandom, Clock clock) {
        this(validator, codeGenerator, new LinkWriter(repository), tokenRandom, clock);
    }
    LinkCreationService(DestinationUrlValidator validator, ShortCodeGenerator codeGenerator,
            LinkWriter writer, SecureRandom tokenRandom, Clock clock) {
        this.validator = validator; this.codeGenerator = codeGenerator; this.writer = writer;
        this.tokenRandom = tokenRandom; this.clock = clock;
    }

    public Result create(String destination) {
        String url = validator.validate(destination);
        byte[] token = new byte[32]; tokenRandom.nextBytes(token);
        byte[] hash = sha256(token);
        for (int attempt = 1; attempt <= ShortCodeGenerator.MAX_ATTEMPTS; attempt++) {
            try {
                String code = codeGenerator.generate();
                writer.save(new LinkEntity(code, url, hash, Instant.now(clock)));
                return new Result(code, url, Instant.now(clock), Base64.getUrlEncoder().withoutPadding().encodeToString(token));
            } catch (DataIntegrityViolationException collision) {
                if (!isShortCodeCollision(collision)) throw new DependencyUnavailableException();
                if (!codeGenerator.mayRetryCollision(attempt)) throw new CreationUnavailableException();
            } catch (org.springframework.dao.DataAccessException exception) {
                throw new DependencyUnavailableException();
            }
        }
        throw new CreationUnavailableException();
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
