package com.srm.events.config;

import com.srm.events.exception.DatabaseConnectionException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseConnectionManagerTest {

    @Test
    void returnsConnectionFromConfiguredDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        DatabaseConnectionManager manager = new DatabaseConnectionManager(dataSource);

        assertSame(connection, manager.getConnection());
    }

    @Test
    void wrapsSqlExceptionWithoutLosingCause() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        SQLException cause = new SQLException("connection refused");
        when(dataSource.getConnection()).thenThrow(cause);

        DatabaseConnectionManager manager = new DatabaseConnectionManager(dataSource);

        DatabaseConnectionException exception = assertThrows(
                DatabaseConnectionException.class,
                manager::getConnection
        );
        assertEquals("Unable to obtain a database connection", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
