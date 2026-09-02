package com.example.urlshortener.redirect;

import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;

@Service
@ConditionalOnBean(LinkRepository.class)
public class LinkResolver {
    private static final Pattern CODE = Pattern.compile("[0-9A-Za-z]{10}");
    private final LinkRepository repository;
    public LinkResolver(LinkRepository repository) { this.repository = repository; }

    public Resolution resolve(String code) {
        if (code == null || !CODE.matcher(code).matches()) return Resolution.notFound();
        try {
            return repository.findByShortCode(code).map(Resolution::active).orElseGet(Resolution::notFound);
        } catch (DataAccessException exception) {
            return Resolution.dependencyUnavailable();
        }
    }

    public record Resolution(Outcome outcome, LinkEntity link) {
        static Resolution active(LinkEntity link) { return new Resolution(Outcome.ACTIVE, link); }
        static Resolution notFound() { return new Resolution(Outcome.NOT_FOUND, null); }
        static Resolution dependencyUnavailable() { return new Resolution(Outcome.DEPENDENCY_UNAVAILABLE, null); }
    }
    public enum Outcome { ACTIVE, NOT_FOUND, DEPENDENCY_UNAVAILABLE }
}
