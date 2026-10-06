package com.srm.events.service.impl;

import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.Event;

import java.time.LocalDate;

/** Pure, shared post-activity state rules. It never mutates application status. */
public final class PostEventLifecycle {
    private PostEventLifecycle() { }

    public static String derive(Application application, Event event, boolean resultPresent) {
        if (application == null || event == null) return "NOT_DUE";
        return derive(application.getCurrentStatus(), event.getEndDate(), application.getParticipationStatus(), application.getOutcomeNotes(), resultPresent);
    }

    public static String derive(ApplicationStatus currentStatus, LocalDate endDate, String participationStatus,
                                String outcomeNotes, boolean resultPresent) {
        if (currentStatus != ApplicationStatus.APPROVED || endDate == null || !LocalDate.now().isAfter(endDate)) {
            return "NOT_DUE";
        }
        String participation = participationStatus;
        if ("REPORTED".equalsIgnoreCase(participation)) return "COMPLETE";
        if ("NO".equalsIgnoreCase(participation)) {
            return blank(outcomeNotes) ? "SUBMITTED" : "COMPLETE";
        }
        if ("YES".equalsIgnoreCase(participation)) return resultPresent ? "COMPLETE" : "SUBMITTED";
        return "PENDING";
    }

    public static boolean blank(String value) { return value == null || value.isBlank(); }
}
