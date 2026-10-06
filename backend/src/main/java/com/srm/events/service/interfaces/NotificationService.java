package com.srm.events.service.interfaces;

import com.srm.events.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    boolean notifyOnce(Long recipientUserId, String notificationType, String title, String message,
                       String recordType, Long recordId, String actionPath, String dedupKey);
    List<NotificationResponseDTO> getForUser(Long userId, int limit);
    long unreadCount(Long userId);
    void markRead(Long userId, Long notificationId);
    int markAllRead(Long userId);
}
