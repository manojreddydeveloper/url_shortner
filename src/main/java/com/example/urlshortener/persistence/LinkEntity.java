package com.example.urlshortener.persistence;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "links", uniqueConstraints = @UniqueConstraint(name = "uk_links_short_code", columnNames = "short_code"))
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class LinkEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "short_code", nullable = false, length = 10)
    private String shortCode;
    @Column(name = "destination_url", nullable = false, length = 4096)
    private String destinationUrl;
    @Column(name = "analytics_token_hash", nullable = false, columnDefinition = "bytea")
    private byte[] analyticsTokenHash;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    protected LinkEntity() { }
    public LinkEntity(String shortCode, String destinationUrl, byte[] analyticsTokenHash, Instant createdAt) {
        this.shortCode = shortCode; this.destinationUrl = destinationUrl; this.analyticsTokenHash = analyticsTokenHash; this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getDestinationUrl() { return destinationUrl; }
    public byte[] getAnalyticsTokenHash() { return analyticsTokenHash; }
    public Instant getCreatedAt() { return createdAt; }
}
