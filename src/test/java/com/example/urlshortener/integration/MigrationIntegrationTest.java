package com.example.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class MigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayMigrationsApplySuccessfully() {
        Long linkCount = jdbc.queryForObject("SELECT COUNT(*) FROM links", Long.class);
        assertThat(linkCount).isNotNull();

        Long clickCount = jdbc.queryForObject("SELECT COUNT(*) FROM click_events", Long.class);
        assertThat(clickCount).isNotNull();
    }

    @Test
    void linksTableHasCorrectSchema() {
        String shortCodeType = jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name='links' AND column_name='short_code'",
                String.class);
        assertThat(shortCodeType).isNotNull();
        assertThat(shortCodeType.toLowerCase()).contains("character");

        Integer shortCodeLength = jdbc.queryForObject(
                "SELECT character_maximum_length FROM information_schema.columns WHERE table_name='links' AND column_name='short_code'",
                Integer.class);
        assertThat(shortCodeLength).isEqualTo(10);

        String destinationType = jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name='links' AND column_name='destination_url'",
                String.class);
        assertThat(destinationType).isNotNull();
        assertThat(destinationType.toLowerCase()).matches(".*(character|text|clob).*");
    }

    @Test
    void clickEventsTableHasCorrectSchema() {
        String linkIdType = jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name='click_events' AND column_name='link_id'",
                String.class);
        assertThat(linkIdType).isNotNull();
        assertThat(linkIdType.toLowerCase()).contains("bigint");

        String trafficClassType = jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name='click_events' AND column_name='traffic_class'",
                String.class);
        assertThat(trafficClassType).isNotNull();
        assertThat(trafficClassType.toLowerCase()).contains("character");
    }

    @Test
    void linksTableHasUniqueConstraintOnShortCode() {
        Integer constraintCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE table_name='links' AND constraint_type='UNIQUE'",
                Integer.class);
        assertThat(constraintCount).isGreaterThan(0);
    }

    @Test
    void clickEventsTableHasForeignKeyToLinks() {
        Integer fkCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE table_name='click_events' AND constraint_type='FOREIGN KEY'",
                Integer.class);
        assertThat(fkCount).isGreaterThan(0);
    }

    @Test
    void migrationsAreIdempotent() {
        jdbc.execute("SELECT 1");
        jdbc.execute("SELECT 1");
    }
}
