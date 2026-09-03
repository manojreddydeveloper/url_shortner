package com.example.urlshortener.analytics;

import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JpaEventSink implements AnalyticsCapture.EventSink {
    private final Supplier<ClickEventRepository> repository;

    @Autowired
    public JpaEventSink(ObjectProvider<ClickEventRepository> repository) {
        this(repository::getIfAvailable);
    }

    JpaEventSink(ClickEventRepository repository) { this(() -> repository); }

    private JpaEventSink(Supplier<ClickEventRepository> repository) { this.repository = repository; }

    @Override
    public void append(ClickEvent event) {
        ClickEventRepository availableRepository = repository.get();
        if (availableRepository == null) {
            throw new IllegalStateException("Analytics persistence is unavailable");
        }
        availableRepository.saveAndFlush(new ClickEventEntity(event));
    }
}
