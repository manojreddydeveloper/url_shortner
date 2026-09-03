package com.example.urlshortener.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTimeBudget {
    private final Supplier<EntityManager> entityManager;
    private final boolean enabled;

    @Autowired
    public DatabaseTimeBudget(ObjectProvider<EntityManager> entityManager,
            @Value("${url-shortener.database-time-budget-enabled:true}") boolean enabled) {
        this(entityManager::getIfAvailable, enabled);
    }

    DatabaseTimeBudget(EntityManager entityManager) { this(() -> entityManager, true); }

    private DatabaseTimeBudget(Supplier<EntityManager> entityManager, boolean enabled) {
        this.entityManager = entityManager;
        this.enabled = enabled;
    }

    public void apply(Operation operation) {
        if (!enabled) {
            return;
        }
        try {
            EntityManager availableEntityManager = entityManager.get();
            if (availableEntityManager == null) {
                throw new DataAccessResourceFailureException("Database persistence is unavailable");
            }
            availableEntityManager.createNativeQuery(operation.statement()).executeUpdate();
        } catch (PersistenceException exception) {
            // The time-budget statements are PostgreSQL-specific. On embedded test databases,
            // treat them as a best-effort optimization instead of failing the request.
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
