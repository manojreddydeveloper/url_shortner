package com.example.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("url-shortener.cache")
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public record UrlShortenerCacheProperties(Integer maxEntries) {
    public static final int DEFAULT_MAX_ENTRIES = 10000;

    public UrlShortenerCacheProperties {
        if (maxEntries == null) {
            maxEntries = DEFAULT_MAX_ENTRIES;
        }
        if (maxEntries < 1) {
            throw new IllegalArgumentException("url-shortener.cache.max-entries must be at least 1");
        }
    }
}
