package com.srm.events.service;

import com.srm.events.dao.interfaces.MentorFollowUpDAO;
import com.srm.events.dto.MentorFollowUpItemDTO;
import com.srm.events.service.impl.NotificationGenerationService;
import com.srm.events.service.interfaces.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationGenerationServiceTest {
    @Mock JdbcTemplate jdbc;
    @Mock MentorFollowUpDAO followUpDAO;
    @Mock NotificationService notifications;
    private NotificationGenerationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationGenerationService(jdbc, followUpDAO, notifications);
    }

    @Test
    void weeklyNotificationUsesFollowUpCentreAndIsoWeekDedupKey() {
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("faculty_id", 41L, "user_id", 7L)));
        when(followUpDAO.findForMentor(41L)).thenReturn(List.of(item("NEEDS_MENTOR_REVIEW"), item("POST_EVENT_PENDING")));
        when(notifications.notifyOnce(eq(7L), eq("MENTOR_WEEKLY_FOLLOWUP"), anyString(), contains("2 students"),
                eq("MENTOR_FOLLOW_UP"), eq(41L), eq("/staff/mentor"), eq("MENTOR_WEEKLY_FOLLOWUP:7:2026-W41")))
                .thenReturn(true);

        assertEquals(1, service.generateWeeklyMentorFollowUps(LocalDate.of(2026, 10, 5)));

        verify(followUpDAO).findForMentor(41L);
        verify(notifications).notifyOnce(eq(7L), eq("MENTOR_WEEKLY_FOLLOWUP"), anyString(), contains("needs mentor review"),
                eq("MENTOR_FOLLOW_UP"), eq(41L), eq("/staff/mentor"), eq("MENTOR_WEEKLY_FOLLOWUP:7:2026-W41"));
    }

    @Test
    void zeroFollowUpsDoNotCreateWeeklyNotification() {
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("faculty_id", 41L, "user_id", 7L)));
        when(followUpDAO.findForMentor(41L)).thenReturn(List.of());

        assertEquals(0, service.generateWeeklyMentorFollowUps(LocalDate.of(2026, 10, 5)));

        verifyNoInteractions(notifications);
    }

    @Test
    void duplicateWeeklyNotificationIsCountedAsNotGenerated() {
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("faculty_id", 41L, "user_id", 7L)));
        when(followUpDAO.findForMentor(41L)).thenReturn(List.of(item("ACHIEVEMENT_VERIFICATION_PENDING")));
        when(notifications.notifyOnce(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(false);

        assertEquals(0, service.generateWeeklyMentorFollowUps(LocalDate.of(2026, 10, 5)));
        verify(notifications).notifyOnce(eq(7L), eq("MENTOR_WEEKLY_FOLLOWUP"), anyString(), anyString(),
                eq("MENTOR_FOLLOW_UP"), eq(41L), eq("/staff/mentor"), eq("MENTOR_WEEKLY_FOLLOWUP:7:2026-W41"));
    }

    @Test
    void postEventScanCreatesDueAndVersionedEvidenceNotifications() {
        Map<String, Object> due = Map.of("application_id", 11L, "application_code", "PAR-001", "user_id", 7L,
                "event_name", "Hackathon");
        Map<String, Object> incomplete = Map.of("application_id", 12L, "application_code", "PAR-002", "user_id", 8L,
                "event_name", "Workshop", "updated_at", Timestamp.valueOf("2026-10-04 10:15:00"));
        when(jdbc.queryForList(anyString())).thenReturn(List.of(due), List.of(incomplete));
        when(notifications.notifyOnce(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(true);

        assertEquals(2, service.generatePostEventReminders());

        verify(notifications).notifyOnce(7L, "POST_EVENT_DUE", "Post-activity report is due",
                "Post-activity report is now due for Hackathon.", "APPLICATION", 11L,
                "/student/applications/PAR-001", "POST_EVENT_DUE:11");
        verify(notifications).notifyOnce(8L, "POST_EVENT_EVIDENCE_INCOMPLETE", "Additional post-activity evidence is required",
                "Additional post-activity evidence is required for Workshop.", "APPLICATION", 12L,
                "/student/applications/PAR-002", "POST_EVENT_EVIDENCE_INCOMPLETE:12:2026-10-04 10:15:00.0");
    }

    private MentorFollowUpItemDTO item(String type) {
        MentorFollowUpItemDTO item = new MentorFollowUpItemDTO();
        item.setFollowUpType(type);
        return item;
    }
}
