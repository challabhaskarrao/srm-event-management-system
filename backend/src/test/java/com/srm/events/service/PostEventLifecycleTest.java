package com.srm.events.service;

import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.Event;
import com.srm.events.service.impl.PostEventLifecycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostEventLifecycleTest {
    @Test
    void approvedActivityBeforeEndDateIsNotDue() {
        assertEquals("NOT_DUE", PostEventLifecycle.derive(application(ApplicationStatus.APPROVED, null, null), event(LocalDate.now()), false));
    }

    @Test
    void approvedActivityAfterEndDateNeedsReport() {
        assertEquals("PENDING", PostEventLifecycle.derive(application(ApplicationStatus.APPROVED, "NOT_YET_OCCURRED", null), event(LocalDate.now().minusDays(1)), false));
    }

    @Test
    void yesWithoutResultIsSubmittedAndWithResultIsComplete() {
        Application application = application(ApplicationStatus.APPROVED, "YES", null);
        Event event = event(LocalDate.now().minusDays(1));
        assertEquals("SUBMITTED", PostEventLifecycle.derive(application, event, false));
        assertEquals("COMPLETE", PostEventLifecycle.derive(application, event, true));
    }

    @Test
    void noParticipationNeedsReasonBeforeComplete() {
        Application application = application(ApplicationStatus.APPROVED, "NO", null);
        Event event = event(LocalDate.now().minusDays(1));
        assertEquals("SUBMITTED", PostEventLifecycle.derive(application, event, false));
        application.setOutcomeNotes("Travel was cancelled");
        assertEquals("COMPLETE", PostEventLifecycle.derive(application, event, false));
    }

    private Application application(ApplicationStatus status, String participation, String notes) {
        Application value = new Application();
        value.setCurrentStatus(status);
        value.setParticipationStatus(participation);
        value.setOutcomeNotes(notes);
        return value;
    }

    private Event event(LocalDate endDate) {
        Event value = new Event();
        value.setEndDate(endDate);
        return value;
    }
}
