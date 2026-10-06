package com.srm.events.dto;

import java.time.LocalDateTime;

public record NotificationResponseDTO(Long notificationId, String notificationType, String title, String message,
                                      String recordType, Long recordId, String actionPath, boolean read,
                                      LocalDateTime createdAt, LocalDateTime readAt) { }
