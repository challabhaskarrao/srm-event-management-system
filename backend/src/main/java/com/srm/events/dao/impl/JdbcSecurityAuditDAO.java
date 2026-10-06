package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.SecurityAuditDAO;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class JdbcSecurityAuditDAO implements SecurityAuditDAO {
    private final DataSource dataSource;

    public JdbcSecurityAuditDAO(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void recordUserEvent(Long userId, String action, String remarks) {
        String sql = "INSERT INTO audit_logs (user_id, entity_type, entity_id, action, remarks) VALUES (?, 'USER', ?, ?, ?)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setString(3, action);
            statement.setString(4, remarks);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to record security audit event", exception);
        }
    }
}
