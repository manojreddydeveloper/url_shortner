package com.example.urlshortener.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class UrlShortenerCachePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void bindsExplicitMaximumEntryCount() {
        contextRunner.withPropertyValues("url-shortener.cache.max-entries=2500").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(UrlShortenerCacheProperties.class).maxEntries()).isEqualTo(2500);
        });
    }

    @Test
    void usesDefaultMaximumEntryCountWhenMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(UrlShortenerCacheProperties.class).maxEntries())
                    .isEqualTo(UrlShortenerCacheProperties.DEFAULT_MAX_ENTRIES);
        });
    }

    @Test
    void rejectsInvalidMaximumEntryCount() {
        contextRunner.withPropertyValues("url-shortener.cache.max-entries=0").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseMessage("url-shortener.cache.max-entries must be at least 1");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(UrlShortenerCacheProperties.class)
    static class PropertiesConfiguration {
    }
}
