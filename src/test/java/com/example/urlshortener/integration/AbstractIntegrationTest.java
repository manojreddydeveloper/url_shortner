package com.example.urlshortener.integration;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestOverrides.class)
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
abstract class AbstractIntegrationTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    DataSource dataSource;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String databaseName = "url_shortener_test_" + UUID.randomUUID().toString().replace("-", "");
        registry.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + databaseName
                        + ";MODE=PostgreSQL"
                        + ";DB_CLOSE_DELAY=-1"
                        + ";DATABASE_TO_LOWER=TRUE"
                        + ";DEFAULT_NULL_ORDERING=HIGH");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
        registry.add("url-shortener.database-time-budget-enabled", () -> "false");
        registry.add("url-shortener.rate-limit.creation-capacity", () -> "1000");
        registry.add("url-shortener.rate-limit.creation-refill-per-minute", () -> "1000");
        registry.add("url-shortener.rate-limit.analytics-capacity", () -> "1000");
        registry.add("url-shortener.rate-limit.analytics-refill-per-minute", () -> "1000");
        registry.add("url-shortener.public-base-url", () -> "https://sho.rt");
    }

    @PostConstruct
    void runFlywayMigrations() {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
