package com.example.urlshortener.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.urlshortener.persistence.LinkEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class LinkCacheTest {

    @Test
    void returnsPositiveEntriesAndEvictsLeastRecentlyUsedEntries() {
        LinkCache cache = new LinkCache(2);
        LinkEntity first = new LinkEntity("aZ3kP9mQ2x", "https://one.example", new byte[32], Instant.EPOCH);
        LinkEntity second = new LinkEntity("bZ3kP9mQ2x", "https://two.example", new byte[32], Instant.EPOCH);
        LinkEntity third = new LinkEntity("cZ3kP9mQ2x", "https://three.example", new byte[32], Instant.EPOCH);

        cache.put(first);
        cache.put(second);
        cache.put(third);

        assertThat(cache.get("aZ3kP9mQ2x")).isEmpty();
        assertThat(cache.get("bZ3kP9mQ2x")).contains(second);
        assertThat(cache.get("cZ3kP9mQ2x")).contains(third);
        assertThat(cache.size()).isEqualTo(2);
    }
}
