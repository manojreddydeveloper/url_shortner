package com.example.urlshortener.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class DatabaseTimeBudgetTest {
    @Test
    void appliesApprovedPostgresStatementBudgets() {
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(org.mockito.ArgumentMatchers.anyString())).thenReturn(query);
        DatabaseTimeBudget budgets = new DatabaseTimeBudget(entityManager);

        budgets.apply(DatabaseTimeBudget.Operation.MAPPING_LOOKUP);
        budgets.apply(DatabaseTimeBudget.Operation.LINK_CREATION);
        budgets.apply(DatabaseTimeBudget.Operation.ANALYTICS_APPEND);
        budgets.apply(DatabaseTimeBudget.Operation.ANALYTICS_QUERY);

        verify(entityManager).createNativeQuery("SET LOCAL statement_timeout = '150ms'");
        verify(entityManager).createNativeQuery("SET LOCAL statement_timeout = '500ms'");
        verify(entityManager).createNativeQuery("SET LOCAL statement_timeout = '50ms'");
        verify(entityManager).createNativeQuery("SET LOCAL statement_timeout = '1000ms'");
    }
}
