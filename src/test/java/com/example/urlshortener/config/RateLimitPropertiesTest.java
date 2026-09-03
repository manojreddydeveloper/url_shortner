package com.example.urlshortener.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RateLimitPropertiesTest {

    @Test
    void usesDefaultsForInvalidValues() {
        RateLimitProperties props = new RateLimitProperties(
                null, -1, -1, -1, -1, -1);
        assertThat(props.trustedProxyAddresses()).isEmpty();
        assertThat(props.creationCapacity()).isEqualTo(20);
        assertThat(props.creationRefillPerMinute()).isEqualTo(10);
        assertThat(props.analyticsCapacity()).isEqualTo(60);
        assertThat(props.analyticsRefillPerMinute()).isEqualTo(60);
        assertThat(props.idleExpirySeconds()).isEqualTo(15 * 60);
    }

    @Test
    void acceptsValidValues() {
        RateLimitProperties props = new RateLimitProperties(
                Set.of("10.0.0.1"), 30, 15, 100, 120, 30 * 60);
        assertThat(props.trustedProxyAddresses()).containsExactly("10.0.0.1");
        assertThat(props.creationCapacity()).isEqualTo(30);
        assertThat(props.creationRefillPerMinute()).isEqualTo(15);
        assertThat(props.analyticsCapacity()).isEqualTo(100);
        assertThat(props.analyticsRefillPerMinute()).isEqualTo(120);
        assertThat(props.idleExpirySeconds()).isEqualTo(30 * 60);
    }

    @Test
    void nullTrustedProxyAddressesDefaultsToEmptySet() {
        RateLimitProperties props = new RateLimitProperties(
                null, 20, 10, 60, 60, 900);
        assertThat(props.trustedProxyAddresses()).isEmpty();
    }
}
