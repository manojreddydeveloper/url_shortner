package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.urlshortener.analytics.ClickEventRepository;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class LinkRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    ClickEventRepository clickEventRepository;

    @BeforeEach
    void setUp() {
        clickEventRepository.deleteAll();
        linkRepository.deleteAll();
    }

    @Test
    void savesAndRetrievesLinkByShortCode() {
        byte[] hash = new byte[32];
        java.util.Arrays.fill(hash, (byte) 7);
        LinkEntity entity = new LinkEntity("aB3kP9mQ2x", "https://example.com", hash, Instant.now());
        linkRepository.save(entity);
        linkRepository.flush();

        Optional<LinkEntity> found = linkRepository.findByShortCode("aB3kP9mQ2x");
        assertThat(found).isPresent();
        assertThat(found.get().getDestinationUrl()).isEqualTo("https://example.com");
        assertThat(found.get().getShortCode()).isEqualTo("aB3kP9mQ2x");
        assertThat(found.get().getAnalyticsTokenHash()).hasSize(32);
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void uniqueConstraintEnforcedOnShortCode() {
        byte[] hash = new byte[32];
        java.util.Arrays.fill(hash, (byte) 1);
        LinkEntity first = new LinkEntity("aB3kP9mQ2x", "https://first.com", hash, Instant.now());
        linkRepository.save(first);
        linkRepository.flush();

        byte[] hash2 = new byte[32];
        java.util.Arrays.fill(hash2, (byte) 2);
        LinkEntity duplicate = new LinkEntity("aB3kP9mQ2x", "https://second.com", hash2, Instant.now());
        assertThatThrownBy(() -> {
            linkRepository.save(duplicate);
            linkRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByShortCodeReturnsEmptyForUnknown() {
        assertThat(linkRepository.findByShortCode("nonexistent")).isEmpty();
    }

    @Test
    void savesLinkWithMaximumLengthDestinationUrl() {
        byte[] hash = new byte[32];
        java.util.Arrays.fill(hash, (byte) 3);
        String longUrl = "https://example.com/" + "a".repeat(4096 - "https://example.com/".length());
        LinkEntity entity = new LinkEntity("xY7zK3nR8w", longUrl, hash, Instant.now());
        linkRepository.save(entity);
        linkRepository.flush();

        Optional<LinkEntity> found = linkRepository.findByShortCode("xY7zK3nR8w");
        assertThat(found).isPresent();
        assertThat(found.get().getDestinationUrl()).hasSize(4096);
    }

    @Test
    void savesMultipleDistinctLinks() {
        byte[] hash = new byte[32];
        java.util.Arrays.fill(hash, (byte) 4);
        linkRepository.save(new LinkEntity("aB3kP9mQ2x", "https://first.com", hash, Instant.now()));
        linkRepository.save(new LinkEntity("xY7zK3nR8w", "https://second.com", hash, Instant.now()));
        linkRepository.flush();

        assertThat(linkRepository.count()).isEqualTo(2);
    }
}
