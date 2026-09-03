package com.example.urlshortener.observability;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {

    private static final int CREATION_CAPACITY = 20;
    private static final int CREATION_REFILL_PER_MINUTE = 10;
    private static final int ANALYTICS_CAPACITY = 60;
    private static final int ANALYTICS_REFILL_PER_MINUTE = 60;

    private final Map<String, Bucket> creationBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> analyticsBuckets = new ConcurrentHashMap<>();

    private static final long IDLE_EXPIRY_SECONDS = 15 * 60;

    private static final long nowSeconds() {
        return Instant.now().getEpochSecond();
    }

    static String deriveClientIdentity(String path, String authorization, boolean trustedProxy) {
        String identity;
        if (trustedProxy && authorization != null) {
            identity = authorization;
        } else {
            identity = path;
        }
        return "rl:" + identity.hashCode();
    }

    public boolean allowCreation(String clientIdentity) {
        Bucket bucket = creationBuckets.computeIfAbsent(clientIdentity, k -> new Bucket(CREATION_CAPACITY));
        boolean allowed = bucket.tryConsume(CREATION_REFILL_PER_MINUTE, ChronoUnit.MINUTES);
        if (!allowed) { purgeIdle(creationBuckets, nowSeconds()); }
        return allowed;
    }

    public boolean allowAnalyticsQuery(String token, String clientIdentity) {
        String key = token + ":" + clientIdentity;
        Bucket bucket = analyticsBuckets.computeIfAbsent(key, k -> new Bucket(ANALYTICS_CAPACITY));
        boolean allowed = bucket.tryConsume(ANALYTICS_REFILL_PER_MINUTE, ChronoUnit.MINUTES);
        if (!allowed) { purgeIdle(analyticsBuckets, nowSeconds()); }
        return allowed;
    }

    private static void purgeIdle(Map<String, Bucket> buckets, long now) {
        buckets.entrySet().removeIf(entry -> {
            long lastAccess = entry.getValue().lastConsumedAt();
            return now - lastAccess > IDLE_EXPIRY_SECONDS;
        });
    }

    public static final class Bucket {
        private final int capacity;
        private final AtomicInteger permits;
        private final AtomicInteger lastRefillAt;
        private AtomicInteger lastConsumedAt;

        Bucket(int capacity) {
            this.capacity = capacity;
            this.permits = new AtomicInteger(capacity);
            this.lastRefillAt = new AtomicInteger((int) Instant.now().getEpochSecond());
            this.lastConsumedAt = new AtomicInteger((int) Instant.now().getEpochSecond());
        }

        boolean tryConsume(int permits, ChronoUnit unit) {
            long now = Instant.now().plus(1, unit).getEpochSecond();
            long last = this.lastRefillAt.get();
            long elapsed = now - last;
            int refill = (int) (elapsed * permits / 60);
            if (refill > 0) {
                int newPermits = Math.min(capacity, this.permits.get() + refill);
                if (newPermits > this.permits.get()) {
                    this.permits.compareAndSet(this.permits.get(), newPermits);
                }
                this.lastRefillAt.set((int) now);
            }
            int available = this.permits.get();
            if (available > 0) {
                this.permits.decrementAndGet();
                this.lastConsumedAt.set((int) Instant.now().getEpochSecond());
                return true;
            }
            return false;
        }

        long lastConsumedAt() { return lastConsumedAt.get(); }
    }
}