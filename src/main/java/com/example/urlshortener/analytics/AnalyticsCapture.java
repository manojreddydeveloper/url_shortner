package com.example.urlshortener.analytics;

import java.time.Clock;
import java.time.Instant;
import com.example.urlshortener.persistence.LinkEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsCapture.class);

    private final EventSink sink;
    private final Clock clock;

    @Autowired
    public AnalyticsCapture(EventSink sink) { this(sink, Clock.systemUTC()); }

    AnalyticsCapture(EventSink sink, Clock clock) { this.sink = sink; this.clock = clock; }

    public void capture(LinkEntity link, String userAgent) {
        ClickEvent.TrafficClass traffic = userAgent != null && userAgent.toLowerCase().contains("bot")
                ? ClickEvent.TrafficClass.SUSPECTED_AUTOMATED : ClickEvent.TrafficClass.UNCLASSIFIED;
        try { sink.append(new ClickEvent(link.getId(), Instant.now(clock), traffic)); }
        catch (RuntimeException ignored) {
            LOGGER.warn("Analytics event append failed; continuing redirect");
        }
    }

    public interface EventSink { void append(ClickEvent event); }
}
