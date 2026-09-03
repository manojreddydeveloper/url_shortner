package com.example.urlshortener.url;

import com.example.urlshortener.persistence.DatabaseTimeBudget;
import com.example.urlshortener.persistence.LinkEntity;
import com.example.urlshortener.persistence.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnBean(LinkRepository.class)
public class LinkWriter {
    private final LinkRepository repository;
    private final DatabaseTimeBudget timeBudget;

    public LinkWriter(LinkRepository repository, DatabaseTimeBudget timeBudget) {
        this.repository = repository;
        this.timeBudget = timeBudget;
    }

    LinkWriter(LinkRepository repository) { this(repository, null); }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(LinkEntity link) {
        if (timeBudget != null) timeBudget.apply(DatabaseTimeBudget.Operation.LINK_CREATION);
        repository.saveAndFlush(link);
    }
}
