package com.example.urlshortener.config;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("url-shortener.rate-limit")
public record RateLimitProperties(
        Set<String> trustedProxyAddresses,
        int creationCapacity,
        int creationRefillPerMinute,
        int analyticsCapacity,
        int analyticsRefillPerMinute,
        long idleExpirySeconds) {

    public RateLimitProperties {
        if (trustedProxyAddresses == null) {
            trustedProxyAddresses = Set.of();
        }
        if (creationCapacity <= 0) creationCapacity = 20;
        if (creationRefillPerMinute <= 0) creationRefillPerMinute = 10;
        if (analyticsCapacity <= 0) analyticsCapacity = 60;
        if (analyticsRefillPerMinute <= 0) analyticsRefillPerMinute = 60;
        if (idleExpirySeconds <= 0) idleExpirySeconds = 15 * 60;
    }
}
