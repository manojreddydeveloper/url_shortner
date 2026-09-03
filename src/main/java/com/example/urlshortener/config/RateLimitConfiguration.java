package com.example.urlshortener.config;

import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.RateLimiter;
import com.example.urlshortener.web.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class RateLimitConfiguration implements WebMvcConfigurer {

    private final RateLimiter rateLimiter;
    private final OperationalMetrics metrics;

    public RateLimitConfiguration(RateLimiter rateLimiter, OperationalMetrics metrics) {
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter, metrics))
                .addPathPatterns("/api/v1/links")
                .addPathPatterns("/api/v1/links/*/analytics");
    }
}
