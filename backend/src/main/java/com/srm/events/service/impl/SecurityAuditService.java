package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.SecurityAuditDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SecurityAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditService.class);
    private final SecurityAuditDAO auditDAO;

    public SecurityAuditService(SecurityAuditDAO auditDAO) { this.auditDAO = auditDAO; }

    public void record(Long userId, String action, String remarks) {
        try {
            auditDAO.recordUserEvent(userId, action, remarks);
        } catch (RuntimeException exception) {
            LOGGER.error("Security audit persistence failed for action={} userId={}", action, userId);
        }
    }
}
