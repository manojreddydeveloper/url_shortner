package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class AlertRulesTest {
    private static final Path RULES = Path.of("ops/alerts.yaml");

    @Test
    void rulesMatchApprovedThresholdsAndWindows() throws IOException {
        Map<String, Rule> rules = rules();

        assertRule(rules, "unexpected-server-errors", "greater_than", 0.01, "PT5M");
        assertRule(rules, "redirect-p95-latency", "greater_than", 0.1, "PT10M");
        assertRule(rules, "creation-p95-latency", "greater_than", 0.2, "PT10M");
        assertRule(rules, "analytics-query-p95-latency", "greater_than", 0.5, "PT10M");
        assertRule(rules, "instance-unready", "less_than", 1, "PT2M");
        assertRule(rules, "datastore-failures", "greater_than", 0.01, "PT5M");
        assertRule(rules, "analytics-loss", "greater_than", 0.01, "PT5M");
        assertRule(rules, "rate-limit-rejections", "greater_than", 0.10, "PT10M");
        assertRule(rules, "collision-retries", "greater_than", 0.001, "PT5M");
        assertRule(rules, "collision-exhaustion", "greater_than", 0, "PT0S");
        assertRule(rules, "pool-saturation", "greater_than", 0.80, "PT10M");
        assertRule(rules, "analytics-deletion-lag", "greater_than", 86400, "PT0S");
    }

    @Test
    void syntheticEvaluationRespectsThresholdDurationFiringAndRecovery() throws IOException {
        Rule errorRate = rules().get("unexpected-server-errors");

        assertThat(errorRate.fires(0.01, Duration.ofMinutes(6))).isFalse();
        assertThat(errorRate.fires(0.02, Duration.ofMinutes(4))).isFalse();
        assertThat(errorRate.fires(0.02, Duration.ofMinutes(5))).isTrue();
        assertThat(errorRate.fires(0.005, Duration.ofMinutes(6))).isFalse();
    }

    @Test
    void alertArtifactContainsNoSensitiveOrUnboundedDimensions() throws IOException {
        String source = Files.readString(RULES);
        assertThat(source).doesNotContain("short_code", "destination", "token", "ip_address",
                "user_agent", "referrer", "request_id", "correlation_id");
    }

    private static void assertRule(Map<String, Rule> rules, String id, String operator,
            double threshold, String duration) {
        assertThat(rules.get(id)).isEqualTo(new Rule(operator, threshold, Duration.parse(duration)));
    }

    private static Map<String, Rule> rules() throws IOException {
        Map<String, Rule> result = new LinkedHashMap<>();
        String id = null, operator = null;
        double threshold = 0;
        for (String line : Files.readAllLines(RULES)) {
            String value = line.trim();
            if (value.startsWith("- id: ")) id = value.substring(6);
            else if (value.startsWith("operator: ")) operator = value.substring(10);
            else if (value.startsWith("threshold: ")) threshold = Double.parseDouble(value.substring(11));
            else if (value.startsWith("for: ")) result.put(id,
                    new Rule(operator, threshold, Duration.parse(value.substring(5))));
        }
        return result;
    }

    private record Rule(String operator, double threshold, Duration duration) {
        boolean fires(double value, Duration sustained) {
            boolean breached = operator.equals("greater_than") ? value > threshold : value < threshold;
            return breached && sustained.compareTo(duration) >= 0;
        }
    }
}
