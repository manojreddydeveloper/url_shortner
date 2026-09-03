package com.example.urlshortener.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

class JpaEventSinkTest {
    @Test
    void persistsEachEventExactlyOnce() {
        ClickEventRepository repository = mock(ClickEventRepository.class);
        JpaEventSink sink = new JpaEventSink(repository);
        ClickEvent event = new ClickEvent(
                42L,
                Instant.parse("2026-09-02T12:00:00Z"),
                ClickEvent.TrafficClass.SUSPECTED_AUTOMATED);

        sink.append(event);

        ArgumentCaptor<ClickEventEntity> captor = ArgumentCaptor.forClass(ClickEventEntity.class);
        verify(repository, times(1)).saveAndFlush(captor.capture());
        ClickEventEntity saved = captor.getValue();
        assertEquals(event.linkId(), saved.getLinkId());
        assertEquals(event.occurredAt(), saved.getOccurredAt());
        assertEquals(event.trafficClass(), saved.getTrafficClass());
    }

    @Test
    void exposesPersistenceFailureToFailOpenCaptureBoundary() {
        ClickEventRepository repository = mock(ClickEventRepository.class);
        when(repository.saveAndFlush(any())).thenThrow(new IllegalStateException("database unavailable"));
        JpaEventSink sink = new JpaEventSink(repository);

        assertThrows(IllegalStateException.class, () -> sink.append(
                new ClickEvent(42L, Instant.EPOCH, ClickEvent.TrafficClass.UNCLASSIFIED)));
        verify(repository, times(1)).saveAndFlush(any());
    }

    @Test
    void reportsUnavailablePersistenceWhenJpaIsDisabled() {
        @SuppressWarnings("unchecked")
        ObjectProvider<ClickEventRepository> repository = mock(ObjectProvider.class);
        JpaEventSink sink = new JpaEventSink(repository);

        assertThrows(IllegalStateException.class, () -> sink.append(
                new ClickEvent(42L, Instant.EPOCH, ClickEvent.TrafficClass.UNCLASSIFIED)));
    }
}
