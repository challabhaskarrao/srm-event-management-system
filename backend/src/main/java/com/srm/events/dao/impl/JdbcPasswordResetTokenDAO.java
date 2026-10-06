package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.PasswordResetTokenDAO;
import com.srm.events.model.PasswordResetToken;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class JdbcPasswordResetTokenDAO implements PasswordResetTokenDAO {
    private final DataSource dataSource;

    public JdbcPasswordResetTokenDAO(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void replaceActiveToken(Long userId, String tokenHash, LocalDateTime expiresAt) {
        String invalidate = "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ? AND used_at IS NULL";
        String insert = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement invalidateStatement = connection.prepareStatement(invalidate);
                 PreparedStatement insertStatement = connection.prepareStatement(insert)) {
                invalidateStatement.setLong(1, userId);
                invalidateStatement.executeUpdate();
                insertStatement.setLong(1, userId);
                insertStatement.setString(2, tokenHash);
                insertStatement.setTimestamp(3, Timestamp.valueOf(expiresAt));
                insertStatement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create password reset token", exception);
        }
    }

    @Override
    public Optional<PasswordResetToken> findByHash(String tokenHash) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token_hash = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tokenHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load password reset token", exception);
        }
    }

    @Override
    public Optional<Long> consumeAndResetPassword(String tokenHash, String passwordHash, LocalDateTime now) {
        String select = "SELECT token_id, user_id, expires_at, used_at FROM password_reset_tokens " +
                "WHERE token_hash = ? FOR UPDATE";
        String updateUser = "UPDATE users SET password_hash = ?, session_version = session_version + 1, " +
                "updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND is_active = TRUE";
        String consumeAll = "UPDATE password_reset_tokens SET used_at = ? WHERE user_id = ? AND used_at IS NULL";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long userId;
                try (PreparedStatement selectStatement = connection.prepareStatement(select)) {
                    selectStatement.setString(1, tokenHash);
                    try (ResultSet resultSet = selectStatement.executeQuery()) {
                        if (!resultSet.next() || resultSet.getTimestamp("used_at") != null
                                || !resultSet.getTimestamp("expires_at").toLocalDateTime().isAfter(now)) {
                            connection.rollback();
                            return Optional.empty();
                        }
                        userId = resultSet.getLong("user_id");
                    }
                }
                try (PreparedStatement userStatement = connection.prepareStatement(updateUser)) {
                    userStatement.setString(1, passwordHash);
                    userStatement.setLong(2, userId);
                    if (userStatement.executeUpdate() != 1) {
                        connection.rollback();
                        return Optional.empty();
                    }
                }
                try (PreparedStatement consumeStatement = connection.prepareStatement(consumeAll)) {
                    consumeStatement.setTimestamp(1, Timestamp.valueOf(now));
                    consumeStatement.setLong(2, userId);
                    consumeStatement.executeUpdate();
                }
                connection.commit();
                return Optional.of(userId);
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to consume password reset token", exception);
        }
    }

    private PasswordResetToken map(ResultSet resultSet) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenId(resultSet.getLong("token_id"));
        token.setUserId(resultSet.getLong("user_id"));
        token.setTokenHash(resultSet.getString("token_hash"));
        token.setExpiresAt(resultSet.getTimestamp("expires_at").toLocalDateTime());
        Timestamp usedAt = resultSet.getTimestamp("used_at");
        if (usedAt != null) token.setUsedAt(usedAt.toLocalDateTime());
        token.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return token;
    }
}
