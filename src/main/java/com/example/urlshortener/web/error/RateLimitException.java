package com.example.urlshortener.web.error;

public final class RateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitException(long retryAfterSeconds) {
        super("Rate limit exceeded");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
