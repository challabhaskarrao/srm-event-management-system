package com.srm.events.dao.impl;

import com.srm.events.config.DatabaseConnectionManager;
import com.srm.events.dao.interfaces.DatabaseHealthDAO;
import com.srm.events.exception.DatabaseConnectionException;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class JdbcDatabaseHealthDAO implements DatabaseHealthDAO {

    private static final String HEALTH_QUERY = "SELECT 1";

    private final DatabaseConnectionManager connectionManager;

    public JdbcDatabaseHealthDAO(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean isDatabaseAvailable() {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(HEALTH_QUERY);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) == 1;
        } catch (SQLException exception) {
            throw new DatabaseConnectionException("Database health query failed", exception);
        }
    }
}
