package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.NotificationDAO;
import com.srm.events.dto.NotificationResponseDTO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class JdbcNotificationDAO implements NotificationDAO {
    private final JdbcTemplate jdbc;
    public JdbcNotificationDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public boolean createIfAbsent(Long recipientUserId, String notificationType, String title, String message,
                                  String recordType, Long recordId, String actionPath, String dedupKey) {
        try {
            return jdbc.update("INSERT INTO notifications (recipient_user_id,notification_type,title,message,record_type,record_id,action_path,dedup_key) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE notification_id=notification_id", recipientUserId, notificationType, title, message, recordType, recordId, actionPath, dedupKey) == 1;
        } catch (DuplicateKeyException ignored) { return false; }
    }

    @Override
    public List<NotificationResponseDTO> findForUser(Long userId, int limit) {
        return jdbc.query("SELECT notification_id,notification_type,title,message,record_type,record_id,action_path,is_read,created_at,read_at FROM notifications WHERE recipient_user_id=? ORDER BY created_at DESC, notification_id DESC LIMIT ?", (rs, row) -> new NotificationResponseDTO(rs.getLong("notification_id"), rs.getString("notification_type"), rs.getString("title"), rs.getString("message"), rs.getString("record_type"), nullableLong(rs.getLong("record_id"), rs.wasNull()), rs.getString("action_path"), rs.getBoolean("is_read"), toLocal(rs.getTimestamp("created_at")), toLocal(rs.getTimestamp("read_at"))), userId, limit);
    }

    @Override public long countUnread(Long userId) { Long value = jdbc.queryForObject("SELECT COUNT(*) FROM notifications WHERE recipient_user_id=? AND is_read=FALSE", Long.class, userId); return value == null ? 0 : value; }
    @Override public boolean markRead(Long userId, Long notificationId) { return jdbc.update("UPDATE notifications SET is_read=TRUE,read_at=COALESCE(read_at,CURRENT_TIMESTAMP) WHERE notification_id=? AND recipient_user_id=?", notificationId, userId) == 1; }
    @Override public int markAllRead(Long userId) { return jdbc.update("UPDATE notifications SET is_read=TRUE,read_at=COALESCE(read_at,CURRENT_TIMESTAMP) WHERE recipient_user_id=? AND is_read=FALSE", userId); }
    private Long nullableLong(long value, boolean wasNull) { return wasNull ? null : value; }
    private java.time.LocalDateTime toLocal(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }
}
