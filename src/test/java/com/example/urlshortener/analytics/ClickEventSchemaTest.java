package com.example.urlshortener.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class ClickEventSchemaTest {
    @Test
    void migrationContainsOnlyApprovedAnalyticsData() throws IOException {
        try (InputStream resource = getClass().getClassLoader()
                .getResourceAsStream("db/migration/V2__create_click_events.sql")) {
            assertThat(resource).isNotNull();
            String migration = new String(resource.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(migration)
                    .contains("link_id BIGINT NOT NULL")
                    .contains("occurred_at TIMESTAMP WITH TIME ZONE NOT NULL")
                    .contains("traffic_class VARCHAR(24) NOT NULL")
                    .contains("REFERENCES links(id) ON DELETE RESTRICT")
                    .contains("CHECK (traffic_class IN ('SUSPECTED_AUTOMATED', 'UNCLASSIFIED'))")
                    .contains("ix_click_events_link_time ON click_events (link_id, occurred_at)")
                    .contains("ix_click_events_occurred_at ON click_events (occurred_at)")
                    .doesNotContain("ip_address", "user_agent", "referrer", "destination_url",
                            "analytics_token", "correlation_id", "cookie", "geography");
        }
    }
}
