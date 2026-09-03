package com.example.urlshortener.url;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;

class LinkCreationServiceTest {
    @Test void createsDurableMappingAndReturnsToken() {
        LinkRepository repo = mock(LinkRepository.class); ShortCodeGenerator gen = mock(ShortCodeGenerator.class);
        when(gen.generate()).thenReturn("aZ3kP9mQ2x"); when(repo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var service = new LinkCreationService(new DestinationUrlValidator(), gen, repo, new SecureRandom(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var result = service.create("https://example.com/path");
        assertEquals("aZ3kP9mQ2x", result.code()); assertEquals(43, result.analyticsToken().length()); verify(repo).saveAndFlush(any(LinkEntity.class));
    }
    @Test void retriesOnlyUniqueCollisionsAndStopsAfterSixAttempts() {
        LinkRepository repo = mock(LinkRepository.class); ShortCodeGenerator gen = mock(ShortCodeGenerator.class);
        when(gen.generate()).thenReturn("aZ3kP9mQ2x"); when(repo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_links_short_code"));
        when(gen.mayRetryCollision(anyInt())).thenAnswer(i -> i.getArgument(0, Integer.class) < 6);
        var service = new LinkCreationService(new DestinationUrlValidator(), gen, repo);
        assertThrows(LinkCreationService.CreationUnavailableException.class, () -> service.create("https://example.com"));
        verify(gen, times(6)).generate(); verify(repo, times(6)).saveAndFlush(any());
    }
    @Test void mapsDatastoreFailureWithoutRetry() {
        LinkRepository repo = mock(LinkRepository.class); ShortCodeGenerator gen = mock(ShortCodeGenerator.class);
        when(gen.generate()).thenReturn("aZ3kP9mQ2x");
        when(repo.saveAndFlush(any())).thenThrow(new DataAccessResourceFailureException("unavailable"));
        var service = new LinkCreationService(new DestinationUrlValidator(), gen, repo);
        assertThrows(LinkCreationService.DependencyUnavailableException.class,
                () -> service.create("https://example.com"));
        verify(repo, times(1)).saveAndFlush(any());
    }
    @Test void doesNotRetryUnrelatedIntegrityFailure() {
        LinkRepository repo = mock(LinkRepository.class); ShortCodeGenerator gen = mock(ShortCodeGenerator.class);
        when(gen.generate()).thenReturn("aZ3kP9mQ2x");
        when(repo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("other_constraint"));
        var service = new LinkCreationService(new DestinationUrlValidator(), gen, repo);
        assertThrows(LinkCreationService.DependencyUnavailableException.class,
                () -> service.create("https://example.com"));
        verify(repo, times(1)).saveAndFlush(any());
    }
}
