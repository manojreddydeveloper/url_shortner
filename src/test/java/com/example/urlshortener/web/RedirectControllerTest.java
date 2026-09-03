package com.example.urlshortener.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.redirect.LinkResolver;
import com.example.urlshortener.analytics.AnalyticsCapture;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RedirectControllerTest {
    @Test void returnsTemporaryNoStoreRedirect() {
        LinkResolver resolver = mock(LinkResolver.class);
        LinkEntity link = new LinkEntity("aZ3kP9mQ2x", "https://example.com/path", new byte[32], Instant.EPOCH);
        when(resolver.resolve("aZ3kP9mQ2x")).thenReturn(new LinkResolver.Resolution(LinkResolver.Outcome.ACTIVE, link));
        var response = new RedirectController(resolver, new AnalyticsCapture(event -> {})).redirect("aZ3kP9mQ2x");
        assertEquals(302, response.getStatusCode().value());
        assertEquals("https://example.com/path", response.getHeaders().getLocation().toString());
        assertEquals("no-store", response.getHeaders().getCacheControl());
    }
    @Test void rejectsNotFoundAndDependencyOutcomes() {
        LinkResolver resolver = mock(LinkResolver.class);
        when(resolver.resolve("missingcode")).thenReturn(new LinkResolver.Resolution(LinkResolver.Outcome.NOT_FOUND, null));
        when(resolver.resolve("aZ3kP9mQ2x")).thenReturn(new LinkResolver.Resolution(LinkResolver.Outcome.DEPENDENCY_UNAVAILABLE, null));
        var controller = new RedirectController(resolver, new AnalyticsCapture(event -> {}));
        assertEquals(404, assertThrows(com.example.urlshortener.web.error.ApiException.class, () -> controller.redirect("missingcode")).status().value());
        assertEquals(503, assertThrows(com.example.urlshortener.web.error.ApiException.class, () -> controller.redirect("aZ3kP9mQ2x")).status().value());
    }
}
