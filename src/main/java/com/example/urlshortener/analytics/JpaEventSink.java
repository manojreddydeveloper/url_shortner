package com.example.urlshortener.analytics;

import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.urlshortener.persistence.DatabaseTimeBudget;

@Component
public class JpaEventSink implements AnalyticsCapture.EventSink {
    private final Supplier<ClickEventRepository> repository;
    private final Supplier<DatabaseTimeBudget> timeBudget;

    @Autowired
    public JpaEventSink(ObjectProvider<ClickEventRepository> repository,
            ObjectProvider<DatabaseTimeBudget> timeBudget) {
        this(repository::getIfAvailable, timeBudget::getIfAvailable);
    }

    JpaEventSink(ObjectProvider<ClickEventRepository> repository) {
        this(repository::getIfAvailable, () -> null);
    }

    JpaEventSink(ClickEventRepository repository) { this(() -> repository, () -> null); }

    JpaEventSink(ClickEventRepository repository, DatabaseTimeBudget timeBudget) {
        this(() -> repository, () -> timeBudget);
    }

    private JpaEventSink(Supplier<ClickEventRepository> repository, Supplier<DatabaseTimeBudget> timeBudget) {
        this.repository = repository;
        this.timeBudget = timeBudget;
    }

    @Override
    @Transactional
    public void append(ClickEvent event) {
        ClickEventRepository availableRepository = repository.get();
        if (availableRepository == null) {
            throw new IllegalStateException("Analytics persistence is unavailable");
        }
        DatabaseTimeBudget availableTimeBudget = timeBudget.get();
        if (availableTimeBudget != null) {
            availableTimeBudget.apply(DatabaseTimeBudget.Operation.ANALYTICS_APPEND);
        }
        availableRepository.saveAndFlush(new ClickEventEntity(event));
    }
}
