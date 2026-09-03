package com.srm.events.config;

import com.srm.events.exception.DatabaseConnectionException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnectionManager {

    private final DataSource dataSource;

    public DatabaseConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Opens a caller-owned JDBC connection. Later multi-step services can disable
     * auto-commit and explicitly commit or roll back on this connection.
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            throw new DatabaseConnectionException("Unable to obtain a database connection", exception);
        }
    }
}
