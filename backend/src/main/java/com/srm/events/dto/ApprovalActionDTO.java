package com.srm.events.dto;

import java.time.LocalDateTime;

public record ApprovalActionDTO(
        Long approvalActionId,
        String approvalLevel,
        String action,
        String previousStatus,
        String newStatus,
        String remarks,
        LocalDateTime actionAt
) {
}
