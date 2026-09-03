package com.example.urlshortener.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "url-shortener.public-base-url=https://sho.rt",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        })
@ExtendWith(OutputCaptureExtension.class)
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class StructuredLoggingIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredLoggingIntegrationTest.class);

    @Test
    void emitsJsonLogsWithCorrelationContext(CapturedOutput output) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("requestId", "request-123")) {
            LOGGER.atInfo()
                    .addKeyValue("operation", "foundation_check")
                    .addKeyValue("outcome", "success")
                    .log("foundation-log-check");
        }

        String logEntry = output.getOut().lines()
                .filter(line -> line.contains("foundation-log-check"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected the test log entry to be captured"));

        assertThat(logEntry)
                .startsWith("{")
                .endsWith("}")
                .contains("\"message\":\"foundation-log-check\"")
                .contains("\"level\":\"INFO\"")
                .contains("\"service\":\"url-shortener\"")
                .contains("\"serviceVersion\":\"development\"")
                .contains("\"operation\":\"foundation_check\"")
                .contains("\"outcome\":\"success\"")
                .contains("\"requestId\":\"request-123\"");
    }
}
