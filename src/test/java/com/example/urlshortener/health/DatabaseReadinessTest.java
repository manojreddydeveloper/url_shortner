package com.example.urlshortener.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DatabaseReadinessTest {
    @Test
    void reportsUnreadyWhenTheDatabaseIsNotConfigured() {
        assertThat(new DatabaseReadiness((DataSource) null).isReady()).isFalse();
    }

    @Test
    void reportsReadyWhenPostgresConnectionValidates() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(1)).thenReturn(true);

        assertThat(new DatabaseReadiness(dataSource).isReady()).isTrue();
        verify(connection).close();
    }

    @Test
    void reportsUnreadyWithoutLeakingDependencyFailures() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("internal database address"));

        assertThat(new DatabaseReadiness(dataSource).isReady()).isFalse();
    }

    @Test
    void recoversOnTheFirstProbeAfterTheDatabaseRecovers() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection unavailable = mock(Connection.class);
        Connection recovered = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(unavailable, recovered);
        when(unavailable.isValid(1)).thenReturn(false);
        when(recovered.isValid(1)).thenReturn(true);

        DatabaseReadiness readiness = new DatabaseReadiness(dataSource);

        assertThat(readiness.isReady()).isFalse();
        assertThat(readiness.isReady()).isTrue();
    }
}
