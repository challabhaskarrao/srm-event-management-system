package com.srm.events.dao.impl;

import com.srm.events.config.DatabaseConnectionManager;
import com.srm.events.exception.DatabaseConnectionException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcDatabaseHealthDAOTest {

    @Test
    void executesRawJdbcHealthQuery() throws SQLException {
        DatabaseConnectionManager connectionManager = mock(DatabaseConnectionManager.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        JdbcDatabaseHealthDAO dao = new JdbcDatabaseHealthDAO(connectionManager);

        assertTrue(dao.isDatabaseAvailable());
        verify(connection).prepareStatement("SELECT 1");
        verify(statement).executeQuery();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void wrapsSqlFailureWithoutLosingCause() throws SQLException {
        DatabaseConnectionManager connectionManager = mock(DatabaseConnectionManager.class);
        Connection connection = mock(Connection.class);
        SQLException cause = new SQLException("query failed");

        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenThrow(cause);

        JdbcDatabaseHealthDAO dao = new JdbcDatabaseHealthDAO(connectionManager);

        DatabaseConnectionException exception = assertThrows(
                DatabaseConnectionException.class,
                dao::isDatabaseAvailable
        );
        assertSame(cause, exception.getCause());
    }
}
