package com.example.urlshortener.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.urlshortener.persistence.LinkEntity;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class AnalyticsCaptureTest {
    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    @Test
    void appendsOneMinimalClassifiedEvent() {
        List<ClickEvent> events = new ArrayList<>();
        AnalyticsCapture capture = new AnalyticsCapture(events::add, Clock.fixed(NOW, ZoneOffset.UTC));
        LinkEntity link = mock(LinkEntity.class);
        when(link.getId()).thenReturn(42L);

        capture.capture(link, "ExampleBot/1.0");

        assertEquals(List.of(new ClickEvent(
                42L, NOW, ClickEvent.TrafficClass.SUSPECTED_AUTOMATED)), events);
    }

    @Test
    void persistenceFailureDoesNotEscapeTheRedirectBoundaryOrRetry(CapturedOutput output) {
        int[] attempts = {0};
        AnalyticsCapture capture = new AnalyticsCapture(event -> {
            attempts[0]++;
            throw new IllegalStateException("database unavailable");
        }, Clock.fixed(NOW, ZoneOffset.UTC));
        LinkEntity link = mock(LinkEntity.class);
        when(link.getId()).thenReturn(42L);

        assertDoesNotThrow(() -> capture.capture(link, null));
        assertEquals(1, attempts[0]);
        assertThat(output.getOut())
                .contains("Analytics event append failed; continuing redirect")
                .doesNotContain("database unavailable");
    }

    @Test
    void repeatedEligibleClicksAreNotDeduplicatedOrBuffered() {
        List<ClickEvent> events = new ArrayList<>();
        AnalyticsCapture capture = new AnalyticsCapture(events::add, Clock.fixed(NOW, ZoneOffset.UTC));
        LinkEntity link = mock(LinkEntity.class);
        when(link.getId()).thenReturn(42L);

        capture.capture(link, null);
        capture.capture(link, null);

        assertEquals(2, events.size());
    }
}
