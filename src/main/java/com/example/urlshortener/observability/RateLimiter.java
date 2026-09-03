package com.example.urlshortener.observability;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {

    private static final int DEFAULT_CREATION_CAPACITY = 20;
    private static final int DEFAULT_CREATION_REFILL = 10;
    private static final int DEFAULT_ANALYTICS_CAPACITY = 60;
    private static final int DEFAULT_ANALYTICS_REFILL = 60;

    private static final long IDLE_EXPIRY_SECONDS = 15 * 60;
    private static final Set<String> TRUSTED_PROXY_HEADERS = Set.of(
            "X-Forwarded-For",
            "X-Real-IP"
    );

    private final ConcurrentHashMap<String, Bucket> creationBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> analyticsBuckets = new ConcurrentHashMap<>();

    private final Set<String> trustedProxyAddresses;

    @Autowired
    public RateLimiter(com.example.urlshortener.config.RateLimitProperties properties) {
        this.trustedProxyAddresses = Set.copyOf(properties.trustedProxyAddresses());
    }

    public RateLimiter(Set<String> trustedProxyAddresses) {
        this.trustedProxyAddresses = Set.copyOf(trustedProxyAddresses);
    }

    public RateLimitResult allowCreation(HttpServletRequest request) {
        String identity = deriveClientIdentity(request);
        Bucket bucket = creationBuckets.computeIfAbsent(identity,
                k -> new Bucket(DEFAULT_CREATION_CAPACITY, DEFAULT_CREATION_REFILL));
        boolean allowed = bucket.tryConsume();
        if (!allowed) {
            long retryAfter = bucket.secondsUntilNextPermit();
            purgeIdle(creationBuckets);
            return new RateLimitResult(false, retryAfter);
        }
        return new RateLimitResult(true, 0);
    }

    public RateLimitResult allowAnalyticsQuery(String bearerToken, HttpServletRequest request) {
        String clientIdentity = deriveClientIdentity(request);
        String key = bearerToken + ":" + clientIdentity;
        Bucket bucket = analyticsBuckets.computeIfAbsent(key,
                k -> new Bucket(DEFAULT_ANALYTICS_CAPACITY, DEFAULT_ANALYTICS_REFILL));
        boolean allowed = bucket.tryConsume();
        if (!allowed) {
            long retryAfter = bucket.secondsUntilNextPermit();
            purgeIdle(analyticsBuckets);
            return new RateLimitResult(false, retryAfter);
        }
        return new RateLimitResult(true, 0);
    }

    String deriveClientIdentity(HttpServletRequest request) {
        String peerAddress = request.getRemoteAddr();
        if (trustedProxyAddresses.contains(peerAddress)) {
            for (String header : TRUSTED_PROXY_HEADERS) {
                String forwarded = request.getHeader(header);
                if (forwarded != null && !forwarded.isBlank()) {
                    String firstIp = forwarded.contains(",")
                            ? forwarded.substring(0, forwarded.indexOf(',')).trim()
                            : forwarded.trim();
                    if (!firstIp.isBlank()) {
                        return "rl:" + firstIp;
                    }
                }
            }
        }
        return "rl:" + peerAddress;
    }

    private static void purgeIdle(ConcurrentHashMap<String, Bucket> buckets) {
        long now = Instant.now().getEpochSecond();
        buckets.entrySet().removeIf(entry -> {
            long lastAccess = entry.getValue().lastConsumedAt();
            return now - lastAccess > IDLE_EXPIRY_SECONDS;
        });
    }

    Map<String, Bucket> creationBuckets() { return creationBuckets; }
    Map<String, Bucket> analyticsBuckets() { return analyticsBuckets; }

    public static final class Bucket {
        private final int capacity;
        private final int refillPerMinute;
        private final AtomicInteger permits;
        private final AtomicInteger lastRefillAt;
        private final AtomicInteger lastConsumedAt;

        Bucket(int capacity, int refillPerMinute) {
            this.capacity = capacity;
            this.refillPerMinute = refillPerMinute;
            this.permits = new AtomicInteger(capacity);
            int now = (int) Instant.now().getEpochSecond();
            this.lastRefillAt = new AtomicInteger(now);
            this.lastConsumedAt = new AtomicInteger(now);
        }

        boolean tryConsume() {
            refill();
            int available = permits.get();
            if (available > 0) {
                permits.decrementAndGet();
                lastConsumedAt.set((int) Instant.now().getEpochSecond());
                return true;
            }
            return false;
        }

        long secondsUntilNextPermit() {
            int available = permits.get();
            if (available > 0) return 0;
            return Math.max(1, 60 / Math.max(1, refillPerMinute));
        }

        private void refill() {
            long now = Instant.now().getEpochSecond();
            long last = lastRefillAt.get();
            long elapsed = now - last;
            if (elapsed <= 0) return;
            int refill = (int) (elapsed * refillPerMinute / 60);
            if (refill > 0) {
                int newPermits = Math.min(capacity, permits.get() + refill);
                if (newPermits > permits.get()) {
                    permits.compareAndSet(permits.get(), newPermits);
                }
                lastRefillAt.set((int) now);
            }
        }

        int availablePermits() { return permits.get(); }
        long lastConsumedAt() { return lastConsumedAt.get(); }
    }

    public record RateLimitResult(boolean allowed, long retryAfterSeconds) { }
}
