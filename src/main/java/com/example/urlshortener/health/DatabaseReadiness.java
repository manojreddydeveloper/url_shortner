package com.example.urlshortener.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
public class DatabaseReadiness {
    private static final int VALIDATION_TIMEOUT_SECONDS = 1;

    private final Supplier<DataSource> dataSource;

    @Autowired
    public DatabaseReadiness(ObjectProvider<DataSource> dataSource) {
        this(dataSource::getIfAvailable);
    }

    DatabaseReadiness(DataSource dataSource) {
        this(() -> dataSource);
    }

    private DatabaseReadiness(Supplier<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isReady() {
        try {
            DataSource availableDataSource = dataSource.get();
            if (availableDataSource == null) return false;
            try (Connection connection = availableDataSource.getConnection()) {
                return connection.isValid(VALIDATION_TIMEOUT_SECONDS);
            }
        } catch (SQLException | RuntimeException exception) {
            return false;
        }
    }
}
