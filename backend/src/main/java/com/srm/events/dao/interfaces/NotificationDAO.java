package com.srm.events.dao.interfaces;

import com.srm.events.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationDAO {
    boolean createIfAbsent(Long recipientUserId, String notificationType, String title, String message,
                           String recordType, Long recordId, String actionPath, String dedupKey);
    List<NotificationResponseDTO> findForUser(Long userId, int limit);
    long countUnread(Long userId);
    boolean markRead(Long userId, Long notificationId);
    int markAllRead(Long userId);
}
