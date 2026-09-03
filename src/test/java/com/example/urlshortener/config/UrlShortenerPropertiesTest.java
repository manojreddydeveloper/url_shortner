package com.example.urlshortener.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class UrlShortenerPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void bindsValidExternalConfiguration() {
        contextRunner
                .withPropertyValues("url-shortener.public-base-url=https://sho.rt")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(UrlShortenerProperties.class).publicBaseUrl())
                            .isEqualTo(URI.create("https://sho.rt"));
                });
    }

    @Test
    void failsWhenRequiredConfigurationIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseMessage("url-shortener.public-base-url is required");
        });
    }

    @Test
    void rejectsUnsafePublicBaseUrl() {
        contextRunner
                .withPropertyValues("url-shortener.public-base-url=https://user:password@sho.rt/path?x=1")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage(
                                    "url-shortener.public-base-url must be an origin without credentials, path, query, or fragment");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(UrlShortenerProperties.class)
    static class PropertiesConfiguration {
    }
}
