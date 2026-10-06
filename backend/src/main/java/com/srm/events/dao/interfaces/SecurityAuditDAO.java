package com.srm.events.dao.interfaces;

public interface SecurityAuditDAO {
    void recordUserEvent(Long userId, String action, String remarks);
}
