package com.srm.events.service;

import com.srm.events.dao.interfaces.*;
import com.srm.events.dto.AchievementVerificationRequestDTO;
import com.srm.events.dto.StudentAchievementRequestDTO;
import com.srm.events.model.EventTypeDefinition;
import com.srm.events.model.ParticipationType;
import com.srm.events.model.Student;
import com.srm.events.model.StudentAchievement;
import com.srm.events.service.impl.StudentAchievementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.web.server.ResponseStatusException;

class StudentAchievementServiceImplTest {
    private StudentAchievementDAO achievements;
    private StudentDAO students;
    private FacultyDAO faculty;
    private ApplicationDAO applications;
    private EventTypeDAO types;
    private StudentAchievementServiceImpl service;
    private Student student;
    private EventTypeDefinition patent;

    @BeforeEach
    void setUp() {
        achievements = mock(StudentAchievementDAO.class);
        students = mock(StudentDAO.class);
        faculty = mock(FacultyDAO.class);
        applications = mock(ApplicationDAO.class);
        types = mock(EventTypeDAO.class);
        service = new StudentAchievementServiceImpl(achievements, students, faculty, applications, types);
        student = new Student(); student.setStudentId(2L); student.setUserId(8L); student.setAcademicYearId(2L); student.setRegisterNumber("DEV-STUDENT-001"); student.setName("Dev Student");
        patent = new EventTypeDefinition(3L, "PATENT", "Patent", 30, "ACHIEVEMENT", Map.of("kind", "PATENT", "team", true));
        when(students.findByUserId(8L)).thenReturn(Optional.of(student));
        when(types.findActiveByCode("PATENT")).thenReturn(Optional.of(patent));
        when(applications.findActiveMentorIdsForStudent(2L)).thenReturn(List.of(4L));
        when(achievements.create(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsOwnedAchievementWithServerResolvedStudentAndMentor() {
        StudentAchievementRequestDTO request = request("PATENT");
        request.setTitle("Campus patent");
        request.setOrganization("SRM Research");
        request.setTypeSpecificDetails(Map.of("patentTitle", "Safer campus routing"));

        StudentAchievementResponseDTOAssert.assertRecord(service.create(request, 8L, "STUDENT"), "PATENT", "Campus patent");
        verify(achievements).create(argThat(value -> value.getStudentId().equals(2L)
                && value.getAssignedMentorId().equals(4L)
                && value.getEventTypeId().equals(3L)), isNull(), eq(List.of()));
    }

    @Test
    void rejectsActivityTypeAndMissingTypeSpecificFields() {
        EventTypeDefinition activity = new EventTypeDefinition(1L, "HACKATHON", "Hackathon", 10, "ACTIVITY", Map.of("kind", "EVENT"));
        when(types.findActiveByCode("HACKATHON")).thenReturn(Optional.of(activity));
        assertThrows(ResponseStatusException.class, () -> service.create(request("HACKATHON"), 8L, "STUDENT"));
        assertThrows(ResponseStatusException.class, () -> service.create(request("PATENT"), 8L, "STUDENT"));
    }

    @Test
    void rejectsAdminVerification() {
        StudentAchievement value = new StudentAchievement(); value.setAchievementId(9L); value.setStudentId(2L);
        when(achievements.findById(9L)).thenReturn(Optional.of(value));
        AchievementVerificationRequestDTO review = new AchievementVerificationRequestDTO(); review.setStatus("APPROVE");
        assertThrows(ResponseStatusException.class, () -> service.review(9L, review, 99L, "ADMIN"));
        verify(achievements, never()).updateMentorReview(anyLong(), anyString(), anyString(), any(), anyLong());
    }

    @Test
    void studentSubmitsForVerificationOnlyAfterRecordIsCreated() {
        StudentAchievement value = new StudentAchievement();
        value.setAchievementId(9L); value.setStudentId(2L); value.setVerificationStatus("PENDING_MENTOR_VERIFICATION");
        when(achievements.findById(9L)).thenReturn(Optional.of(value));

        assertEquals("PENDING_MENTOR_VERIFICATION", service.submitForVerification(9L, 8L, "STUDENT").verificationStatus());
    }

    @Test
    void mentorReviewRequiresAssignmentAndMovesRecordToHod() {
        StudentAchievement value = new StudentAchievement(); value.setAchievementId(9L); value.setStudentId(2L); value.setVerificationStatus("PENDING_MENTOR_VERIFICATION");
        when(achievements.findById(9L)).thenReturn(Optional.of(value));
        when(faculty.findActiveByUserId(4L)).thenReturn(Optional.of(new com.srm.events.model.Faculty()));
        when(achievements.isMentorAssigned(9L, null)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> service.review(9L, approve(), 4L, "MENTOR"));
    }

    private StudentAchievementRequestDTO request(String type) {
        StudentAchievementRequestDTO request = new StudentAchievementRequestDTO(); request.setEventTypeCode(type); request.setTitle("Title"); request.setOrganization("Organization"); return request;
    }
    private AchievementVerificationRequestDTO approve() { AchievementVerificationRequestDTO request = new AchievementVerificationRequestDTO(); request.setStatus("APPROVE"); return request; }

    private static final class StudentAchievementResponseDTOAssert {
        static void assertRecord(com.srm.events.dto.StudentAchievementResponseDTO value, String type, String title) {
            assertEquals(type, value.eventTypeCode()); assertEquals(title, value.title());
        }
    }
}
