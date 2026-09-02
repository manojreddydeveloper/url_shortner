package com.example.urlshortener.analytics;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.urlshortener.persistence.LinkEntity;

@Service
public class AnalyticsCapture {
    private final EventSink sink;
    private final Clock clock;
    @Autowired
    public AnalyticsCapture(EventSink sink) { this(sink, Clock.systemUTC()); }
    AnalyticsCapture(EventSink sink, Clock clock) { this.sink = sink; this.clock = clock; }
    public void capture(LinkEntity link, String userAgent) {
        ClickEvent.TrafficClass traffic = userAgent != null && userAgent.toLowerCase().contains("bot")
                ? ClickEvent.TrafficClass.SUSPECTED_AUTOMATED : ClickEvent.TrafficClass.UNCLASSIFIED;
        try { sink.append(new ClickEvent(link.getId(), Instant.now(clock), traffic)); }
        catch (RuntimeException ignored) { }
    }
    public interface EventSink { void append(ClickEvent event); }

    @org.springframework.stereotype.Component
    static final class NoOpEventSink implements EventSink {
        public void append(ClickEvent event) { }
    }
}
