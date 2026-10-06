package com.srm.events.model;

public enum ApplicationStatus {
    SUBMITTED,
    PENDING_MENTOR_APPROVAL,
    PENDING_HOD_APPROVAL,
    RETURNED_BY_MENTOR,
    RETURNED_BY_HOD,
    APPROVED,
    REJECTED,
    REJECTED_BY_MENTOR,
    REJECTED_BY_HOD,
    CANCELLED
}
