package com.example.urlshortener.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ClickEventEntityTest {
    @Test
    void mapsOnlyApprovedEventFields() {
        ClickEventEntity entity = new ClickEventEntity(
                new ClickEvent(1L, Instant.EPOCH, ClickEvent.TrafficClass.UNCLASSIFIED));

        assertEquals(1L, entity.getLinkId());
        assertEquals(Instant.EPOCH, entity.getOccurredAt());
        assertEquals(ClickEvent.TrafficClass.UNCLASSIFIED, entity.getTrafficClass());
    }
}
