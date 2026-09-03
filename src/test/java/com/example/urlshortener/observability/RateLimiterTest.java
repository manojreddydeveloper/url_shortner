package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(Set.of("10.0.0.1"));
    }

    @Test
    void allowsCreationWithinQuota() {
        HttpServletRequest request = mockRequest("192.168.1.1");
        for (int i = 0; i < 20; i++) {
            assertThat(rateLimiter.allowCreation(request).allowed()).isTrue();
        }
    }

    @Test
    void rejectsCreationOverQuota() {
        HttpServletRequest request = mockRequest("192.168.1.2");
        for (int i = 0; i < 20; i++) {
            rateLimiter.allowCreation(request);
        }
        RateLimiter.RateLimitResult result = rateLimiter.allowCreation(request);
        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfterSeconds()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void allowsAnalyticsWithinQuota() {
        HttpServletRequest request = mockRequest("192.168.1.3");
        for (int i = 0; i < 60; i++) {
            assertThat(rateLimiter.allowAnalyticsQuery("token-abc", request).allowed()).isTrue();
        }
    }

    @Test
    void rejectsAnalyticsOverQuota() {
        HttpServletRequest request = mockRequest("192.168.1.4");
        for (int i = 0; i < 60; i++) {
            rateLimiter.allowAnalyticsQuery("token-xyz", request);
        }
        RateLimiter.RateLimitResult result = rateLimiter.allowAnalyticsQuery("token-xyz", request);
        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfterSeconds()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void separatesBucketsByClientIdentity() {
        HttpServletRequest request1 = mockRequest("192.168.1.10");
        HttpServletRequest request2 = mockRequest("192.168.1.11");
        for (int i = 0; i < 20; i++) {
            rateLimiter.allowCreation(request1);
        }
        assertThat(rateLimiter.allowCreation(request1).allowed()).isFalse();
        assertThat(rateLimiter.allowCreation(request2).allowed()).isTrue();
    }

    @Test
    void separatesAnalyticsByTokenAndIdentity() {
        HttpServletRequest request = mockRequest("192.168.1.20");
        for (int i = 0; i < 60; i++) {
            rateLimiter.allowAnalyticsQuery("token-a", request);
        }
        assertThat(rateLimiter.allowAnalyticsQuery("token-a", request).allowed()).isFalse();
        assertThat(rateLimiter.allowAnalyticsQuery("token-b", request).allowed()).isTrue();
    }

    @Test
    void derivesClientIdentityFromDirectPeerIp() {
        HttpServletRequest request = mockRequest("192.168.1.50");
        String identity = rateLimiter.deriveClientIdentity(request);
        assertThat(identity).isEqualTo("rl:192.168.1.50");
    }

    @Test
    void derivesClientIdentityFromForwardedHeaderWhenTrustedProxy() {
        HttpServletRequest request = mockRequest("10.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.50, 70.41.3.18");
        String identity = rateLimiter.deriveClientIdentity(request);
        assertThat(identity).isEqualTo("rl:203.0.113.50");
    }

    @Test
    void derivesClientIdentityFromRealIpWhenTrustedProxy() {
        HttpServletRequest request = mockRequest("10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.60");
        String identity = rateLimiter.deriveClientIdentity(request);
        assertThat(identity).isEqualTo("rl:203.0.113.60");
    }

    @Test
    void ignoresForwardedHeadersFromUntrustedProxy() {
        HttpServletRequest request = mockRequest("192.168.1.100");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.50");
        String identity = rateLimiter.deriveClientIdentity(request);
        assertThat(identity).isEqualTo("rl:192.168.1.100");
    }

    @Test
    void ignoresBlankForwardedHeader() {
        HttpServletRequest request = mockRequest("10.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
        String identity = rateLimiter.deriveClientIdentity(request);
        assertThat(identity).isEqualTo("rl:10.0.0.1");
    }

    @Test
    void idleBucketsArePurged() {
        HttpServletRequest request = mockRequest("192.168.1.200");
        for (int i = 0; i < 20; i++) {
            rateLimiter.allowCreation(request);
        }
        assertThat(rateLimiter.allowCreation(request).allowed()).isFalse();
        assertThat(rateLimiter.creationBuckets()).isNotEmpty();
    }

    private static HttpServletRequest mockRequest(String remoteAddr) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        return request;
    }
}
