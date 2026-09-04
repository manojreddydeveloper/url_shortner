package com.example.urlshortener.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("url-shortener.cache")
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public record UrlShortenerCacheProperties(Integer maxEntries, Duration redisTtl) {
    public static final int DEFAULT_MAX_ENTRIES = 10000;
    public static final Duration DEFAULT_REDIS_TTL = Duration.ofMinutes(30);

    public UrlShortenerCacheProperties {
        if (maxEntries == null) {
            maxEntries = DEFAULT_MAX_ENTRIES;
        }
        if (maxEntries < 1) {
            throw new IllegalArgumentException("url-shortener.cache.max-entries must be at least 1");
        }
        if (redisTtl == null) {
            redisTtl = DEFAULT_REDIS_TTL;
        }
        if (redisTtl.isZero() || redisTtl.isNegative()) {
            throw new IllegalArgumentException("url-shortener.cache.redis-ttl must be positive");
        }
    }
}
