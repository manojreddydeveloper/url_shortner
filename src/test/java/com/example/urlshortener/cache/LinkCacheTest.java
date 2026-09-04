package com.example.urlshortener.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.urlshortener.persistence.LinkEntity;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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

    @Test
    void roundTripsThroughRedisWhenAvailable() throws Exception {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add(anyString(), any(String.class))).thenReturn(1L);
        when(setOperations.members("url-shortener:links:cache:index")).thenReturn(java.util.Set.of("aZ3kP9mQ2x"));

        AtomicReference<String> storedJson = new AtomicReference<>();
        doAnswer(invocation -> {
            storedJson.set(invocation.getArgument(1, String.class));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(valueOperations.get("url-shortener:links:cache:aZ3kP9mQ2x")).thenAnswer(invocation -> storedJson.get());

        LinkCache cache = new LinkCache(2, Duration.ofMinutes(5), redisTemplate);
        LinkEntity link = new LinkEntity(42L, "aZ3kP9mQ2x", "https://redis.example", new byte[32], Instant.EPOCH);

        cache.put(link);

        assertThat(cache.get("aZ3kP9mQ2x")).hasValueSatisfying(result -> {
            assertThat(result.getId()).isEqualTo(42L);
            assertThat(result.getShortCode()).isEqualTo("aZ3kP9mQ2x");
            assertThat(result.getDestinationUrl()).isEqualTo("https://redis.example");
            assertThat(result.getCreatedAt()).isEqualTo(Instant.EPOCH);
        });
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void doesNotSilentlyFallBackToLocalCacheWhenRedisIsUnavailable() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        doThrow(new RuntimeException("redis down")).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("redis down"));
        when(setOperations.members(anyString())).thenThrow(new RuntimeException("redis down"));

        LinkCache cache = new LinkCache(2, Duration.ofMinutes(5), redisTemplate);
        LinkEntity link = new LinkEntity(42L, "aZ3kP9mQ2x", "https://fallback.example", new byte[32], Instant.EPOCH);

        cache.put(link);

        assertThat(cache.get("aZ3kP9mQ2x")).isEmpty();
        assertThat(cache.size()).isEqualTo(0);
    }
}
