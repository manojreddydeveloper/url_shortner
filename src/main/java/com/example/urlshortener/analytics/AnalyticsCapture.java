package com.example.urlshortener.analytics;

import java.time.Clock;
import java.time.Instant;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.observability.OperationalMetrics;
import com.example.urlshortener.observability.OperationalMetrics.Outcome;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class AnalyticsCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsCapture.class);

    private final EventSink sink;
    private final Clock clock;
    private final OperationalMetrics metrics;

    @Autowired
    public AnalyticsCapture(EventSink sink, ObjectProvider<OperationalMetrics> metrics) {
        this(sink, Clock.systemUTC(), metrics.getIfAvailable());
    }

    public AnalyticsCapture(EventSink sink) { this(sink, Clock.systemUTC(), null); }
    AnalyticsCapture(EventSink sink, Clock clock) { this(sink, clock, null); }
    AnalyticsCapture(EventSink sink, Clock clock, OperationalMetrics metrics) {
        this.sink = sink; this.clock = clock; this.metrics = metrics;
    }

    public void capture(LinkEntity link, String userAgent) {
        ClickEvent.TrafficClass traffic = userAgent != null && userAgent.toLowerCase().contains("bot")
                ? ClickEvent.TrafficClass.SUSPECTED_AUTOMATED : ClickEvent.TrafficClass.UNCLASSIFIED;
        metric(Outcome.ATTEMPTED);
        try {
            sink.append(new ClickEvent(link.getId(), Instant.now(clock), traffic));
            metric(Outcome.COMMITTED);
        }
        catch (RuntimeException ignored) {
            metric(Outcome.FAILED);
            metric(Outcome.AMBIGUOUS_LOST);
            LOGGER.warn("Analytics event append failed; continuing redirect");
        }
    }

    private void metric(Outcome outcome) {
        if (metrics != null) metrics.analytics("append", outcome);
    }

    public interface EventSink { void append(ClickEvent event); }
}
