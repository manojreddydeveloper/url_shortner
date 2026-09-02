package com.example.urlshortener.analytics;

import java.time.Instant;

public record ClickEvent(Long linkId, Instant occurredAt, TrafficClass trafficClass) {
    public enum TrafficClass { SUSPECTED_AUTOMATED, UNCLASSIFIED }
}
