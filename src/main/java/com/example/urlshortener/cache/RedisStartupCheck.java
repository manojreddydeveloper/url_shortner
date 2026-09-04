package com.example.urlshortener.cache;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "url-shortener.redis", name = "required", havingValue = "true", matchIfMissing = true)
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class RedisStartupCheck implements ApplicationRunner {
    private final StringRedisTemplate redisTemplate;

    RedisStartupCheck(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String pong = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            if (!"PONG".equalsIgnoreCase(pong)) {
                throw new IllegalStateException("Redis is required and did not respond with PONG");
            }
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Redis is required and must be reachable at startup", exception);
        }
    }
}
