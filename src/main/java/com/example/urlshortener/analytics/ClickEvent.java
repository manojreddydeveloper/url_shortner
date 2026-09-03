package com.example.urlshortener.analytics;

import java.time.Instant;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public record ClickEvent(Long linkId, Instant occurredAt, TrafficClass trafficClass) {
    public enum TrafficClass { SUSPECTED_AUTOMATED, UNCLASSIFIED }
}
