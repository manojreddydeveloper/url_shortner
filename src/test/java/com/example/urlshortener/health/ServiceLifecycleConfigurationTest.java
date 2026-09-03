package com.example.urlshortener.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class ServiceLifecycleConfigurationTest {
    @Test
    void configuresGracefulShutdownWithTheApprovedHardLimit() throws IOException {
        Properties properties = new Properties();
        try (var input = new ClassPathResource("application.properties").getInputStream()) {
            properties.load(input);
        }

        assertThat(properties.getProperty("server.shutdown")).isEqualTo("graceful");
        assertThat(properties.getProperty("spring.lifecycle.timeout-per-shutdown-phase")).isEqualTo("30s");
        assertThat(properties.stringPropertyNames())
                .noneMatch(name -> name.contains("analytics") && name.contains("buffer"));
    }
}
