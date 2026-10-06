package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.NotificationDAO;
import com.srm.events.dto.NotificationResponseDTO;
import com.srm.events.service.interfaces.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationDAO notificationDAO;
    public NotificationServiceImpl(NotificationDAO notificationDAO) { this.notificationDAO = notificationDAO; }

    @Override public boolean notifyOnce(Long recipientUserId, String notificationType, String title, String message, String recordType, Long recordId, String actionPath, String dedupKey) {
        if (recipientUserId == null || dedupKey == null || dedupKey.isBlank()) return false;
        if (actionPath != null && (!actionPath.startsWith("/") || actionPath.startsWith("//") || actionPath.contains("\\") || actionPath.contains(".."))) throw new IllegalArgumentException("Notification action must be an internal path");
        return notificationDAO.createIfAbsent(recipientUserId, notificationType, title, message, recordType, recordId, actionPath, dedupKey);
    }
    @Override public List<NotificationResponseDTO> getForUser(Long userId, int limit) { return notificationDAO.findForUser(userId, Math.max(1, Math.min(limit, 100))); }
    @Override public long unreadCount(Long userId) { return notificationDAO.countUnread(userId); }
    @Override public void markRead(Long userId, Long notificationId) { if (!notificationDAO.markRead(userId, notificationId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"); }
    @Override public int markAllRead(Long userId) { return notificationDAO.markAllRead(userId); }
}
