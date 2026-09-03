package com.example.urlshortener.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class LinkEntityTest {
    @Test
    void exposesOnlyApprovedMappingFields() {
        Instant created = Instant.parse("2026-09-02T00:00:00Z");
        byte[] hash = new byte[32];
        LinkEntity link = new LinkEntity("aZ3kP9mQ2x", "https://example.com", hash, created);
        assertEquals("aZ3kP9mQ2x", link.getShortCode());
        assertEquals("https://example.com", link.getDestinationUrl());
        assertEquals(created, link.getCreatedAt());
        assertEquals(32, link.getAnalyticsTokenHash().length);
    }
}
