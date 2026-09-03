package com.example.urlshortener.redirect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class LinkResolverTest {
    @Test void resolvesActiveMapping() {
        LinkRepository repo = mock(LinkRepository.class);
        LinkEntity link = new LinkEntity("aZ3kP9mQ2x", "https://example.com", new byte[32], java.time.Instant.EPOCH);
        when(repo.findByShortCode("aZ3kP9mQ2x")).thenReturn(Optional.of(link));
        var result = new LinkResolver(repo).resolve("aZ3kP9mQ2x");
        assertEquals(LinkResolver.Outcome.ACTIVE, result.outcome()); assertEquals(link, result.link());
    }
    @Test void malformedAndUnknownCodesAreNotFoundWithoutLookup() {
        LinkRepository repo = mock(LinkRepository.class);
        assertEquals(LinkResolver.Outcome.NOT_FOUND, new LinkResolver(repo).resolve("bad" ).outcome());
        verifyNoInteractions(repo);
    }
    @Test void datastoreFailureIsDistinctFromNotFound() {
        LinkRepository repo = mock(LinkRepository.class);
        when(repo.findByShortCode(any())).thenThrow(new DataAccessResourceFailureException("down"));
        assertEquals(LinkResolver.Outcome.DEPENDENCY_UNAVAILABLE, new LinkResolver(repo).resolve("aZ3kP9mQ2x").outcome());
    }
    @Test void nullCodeIsNotFoundWithoutLookup() {
        LinkRepository repo = mock(LinkRepository.class);
        assertEquals(LinkResolver.Outcome.NOT_FOUND, new LinkResolver(repo).resolve(null).outcome());
        verifyNoInteractions(repo);
    }
    @Test void nonTenCharCodeIsNotFoundWithoutLookup() {
        LinkRepository repo = mock(LinkRepository.class);
        assertEquals(LinkResolver.Outcome.NOT_FOUND, new LinkResolver(repo).resolve("short").outcome());
        verifyNoInteractions(repo);
    }
    @Test void elevenCharCodeIsNotFoundWithoutLookup() {
        LinkRepository repo = mock(LinkRepository.class);
        assertEquals(LinkResolver.Outcome.NOT_FOUND, new LinkResolver(repo).resolve("aZ3kP9mQ2xB").outcome());
        verifyNoInteractions(repo);
    }
    @Test void codeWithSpecialCharsIsNotFoundWithoutLookup() {
        LinkRepository repo = mock(LinkRepository.class);
        assertEquals(LinkResolver.Outcome.NOT_FOUND, new LinkResolver(repo).resolve("aZ3kP9mQ2!").outcome());
        verifyNoInteractions(repo);
    }
}
