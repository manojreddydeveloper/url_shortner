package com.example.urlshortener.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTimeBudget {
    private final Supplier<EntityManager> entityManager;

    @Autowired
    public DatabaseTimeBudget(ObjectProvider<EntityManager> entityManager) {
        this(entityManager::getIfAvailable);
    }

    DatabaseTimeBudget(EntityManager entityManager) { this(() -> entityManager); }

    private DatabaseTimeBudget(Supplier<EntityManager> entityManager) { this.entityManager = entityManager; }

    public void apply(Operation operation) {
        try {
            EntityManager availableEntityManager = entityManager.get();
            if (availableEntityManager == null) {
                throw new DataAccessResourceFailureException("Database persistence is unavailable");
            }
            availableEntityManager.createNativeQuery(operation.statement()).executeUpdate();
        } catch (PersistenceException exception) {
            throw new DataAccessResourceFailureException("Unable to apply database time budget", exception);
        }
    }

    public enum Operation {
        MAPPING_LOOKUP("SET LOCAL statement_timeout = '150ms'"),
        LINK_CREATION("SET LOCAL statement_timeout = '500ms'"),
        ANALYTICS_APPEND("SET LOCAL statement_timeout = '50ms'"),
        ANALYTICS_QUERY("SET LOCAL statement_timeout = '1000ms'");

        private final String statement;

        Operation(String statement) { this.statement = statement; }

        String statement() { return statement; }
    }
}
