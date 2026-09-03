package com.example.urlshortener.analytics;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "click_events", indexes = {
        @Index(name = "ix_click_events_link_time", columnList = "link_id, occurred_at"),
        @Index(name = "ix_click_events_occurred_at", columnList = "occurred_at")
})
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class ClickEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_class", nullable = false, length = 24)
    private ClickEvent.TrafficClass trafficClass;

    protected ClickEventEntity() { }

    public ClickEventEntity(ClickEvent event) {
        this.linkId = event.linkId();
        this.occurredAt = event.occurredAt();
        this.trafficClass = event.trafficClass();
    }

    public Long getLinkId() { return linkId; }
    public Instant getOccurredAt() { return occurredAt; }
    public ClickEvent.TrafficClass getTrafficClass() { return trafficClass; }
}
