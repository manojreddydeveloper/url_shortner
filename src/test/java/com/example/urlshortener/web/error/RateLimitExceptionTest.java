package com.example.urlshortener.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RateLimitExceptionTest {

    @Test
    void storesRetryAfterSeconds() {
        RateLimitException ex = new RateLimitException(30);
        assertThat(ex.retryAfterSeconds()).isEqualTo(30);
        assertThat(ex.getMessage()).isEqualTo("Rate limit exceeded");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    void normalizesNonPositiveRetryAfterToAtLeastOne(long input) {
        RateLimitException ex = new RateLimitException(input);
        assertThat(ex.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void largeRetryAfterIsPreserved() {
        RateLimitException ex = new RateLimitException(3600);
        assertThat(ex.retryAfterSeconds()).isEqualTo(3600);
    }
}
