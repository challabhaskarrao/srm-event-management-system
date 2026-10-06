package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class JdbcUserDAO implements UserDAO {

    private final DataSource dataSource;

    @Autowired
    public JdbcUserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRoleId(rs.getLong("role_id"));
        user.setRoleName(rs.getString("role_name"));
        user.setActive(rs.getBoolean("is_active"));
        user.setSessionVersion(rs.getLong("session_version"));
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            user.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            user.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }
        
        Timestamp lastLoginAtTs = rs.getTimestamp("last_login_at");
        if (lastLoginAtTs != null) {
            user.setLastLoginAt(lastLoginAtTs.toLocalDateTime());
        }
        
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT u.*, r.role_name FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE LOWER(u.email) = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long userId) {
        String sql = "SELECT u.*, r.role_name FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, password_hash, role_id, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setLong(3, user.getRoleId());
            ps.setBoolean(4, user.isActive());
            
            LocalDateTime now = LocalDateTime.now();
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(now);
            }
            if (user.getUpdatedAt() == null) {
                user.setUpdatedAt(now);
            }
            
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(user.getUpdatedAt()));
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void updateLastLogin(Long userId) {
        String sql = "UPDATE users SET last_login_at = ? WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, userId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating last login for user", e);
        }
    }

    @Override
    public boolean updatePasswordAndIncrementSession(Long userId, String expectedPasswordHash, String newPasswordHash) {
        String updateUser = "UPDATE users SET password_hash = ?, session_version = session_version + 1, " +
                "updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND password_hash = ?";
        String invalidateResets = "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ? AND used_at IS NULL";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement = connection.prepareStatement(updateUser);
                 PreparedStatement resetStatement = connection.prepareStatement(invalidateResets)) {
                userStatement.setString(1, newPasswordHash);
                userStatement.setLong(2, userId);
                userStatement.setString(3, expectedPasswordHash);
                if (userStatement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                resetStatement.setLong(1, userId);
                resetStatement.executeUpdate();
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update password", exception);
        }
    }
}
