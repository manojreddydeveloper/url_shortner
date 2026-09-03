package com.example.urlshortener.integration;

import java.util.Set;
import com.example.urlshortener.observability.RateLimiter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class TestOverrides {

    @Bean
    @Primary
    RateLimiter rateLimiter() {
        return new RateLimiter(Set.of()) {
            @Override
            public RateLimitResult allowCreation(jakarta.servlet.http.HttpServletRequest request) {
                return new RateLimitResult(true, 0);
            }

            @Override
            public RateLimitResult allowAnalyticsQuery(String bearerToken, jakarta.servlet.http.HttpServletRequest request) {
                return new RateLimitResult(true, 0);
            }
        };
    }
}
