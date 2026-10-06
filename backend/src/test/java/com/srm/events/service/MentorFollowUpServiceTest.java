package com.srm.events.service;

import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.MentorFollowUpDAO;
import com.srm.events.dto.MentorFollowUpItemDTO;
import com.srm.events.dto.MentorFollowUpResponseDTO;
import com.srm.events.model.Faculty;
import com.srm.events.service.impl.MentorFollowUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorFollowUpServiceTest {
    @Mock FacultyDAO facultyDAO;
    @Mock MentorFollowUpDAO followUpDAO;
    private MentorFollowUpService service;

    @BeforeEach
    void setUp() { service = new MentorFollowUpService(facultyDAO, followUpDAO); }

    @Test
    void summaryIsDerivedFromAssignmentScopedItems() {
        Faculty faculty = new Faculty(); faculty.setFacultyId(41L);
        when(facultyDAO.findActiveByUserId(7L)).thenReturn(Optional.of(faculty));
        when(followUpDAO.findForMentor(41L)).thenReturn(List.of(item("NEEDS_MENTOR_REVIEW", 4), item("POST_EVENT_PENDING", 2), item("ACHIEVEMENT_VERIFICATION_PENDING", 1)));

        MentorFollowUpResponseDTO response = service.get(7L, null, null, null, null, null);

        assertEquals(1, response.getSummary().getNeedsMentorReview());
        assertEquals(1, response.getSummary().getPostEventPending());
        assertEquals(1, response.getSummary().getAchievementVerificationPending());
        assertEquals(3, response.getSummary().getTotalAttentionRequired());
        verify(followUpDAO).findForMentor(41L);
    }

    @Test
    void filtersDoNotChangeTheAssignmentIdentity() {
        Faculty faculty = new Faculty(); faculty.setFacultyId(41L);
        when(facultyDAO.findActiveByUserId(7L)).thenReturn(Optional.of(faculty));
        MentorFollowUpItemDTO item = item("POST_EVENT_PENDING", 7); item.setSection("A"); item.setStudentYear(3); item.setRecordCategory("Workshop");
        when(followUpDAO.findForMentor(41L)).thenReturn(List.of(item));

        MentorFollowUpResponseDTO response = service.get(7L, "POST_EVENT_PENDING", "A", 3, "Workshop", 7L);

        assertEquals(1, response.getItems().size());
        assertEquals(7L, response.getItems().getFirst().getDaysPending());
        verify(followUpDAO).findForMentor(41L);
    }

    @Test
    void minimumDaysPendingFiltersOlderThanRequestedThreshold() {
        Faculty faculty = new Faculty(); faculty.setFacultyId(41L);
        when(facultyDAO.findActiveByUserId(7L)).thenReturn(Optional.of(faculty));
        when(followUpDAO.findForMentor(41L)).thenReturn(List.of(item("POST_EVENT_PENDING", 3), item("POST_EVENT_PENDING", 10)));

        MentorFollowUpResponseDTO response = service.get(7L, null, null, null, null, 7L);

        assertEquals(1, response.getItems().size());
        assertEquals(10L, response.getItems().getFirst().getDaysPending());
    }

    @Test
    void missingActiveFacultyIsForbidden() {
        when(facultyDAO.findActiveByUserId(7L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.get(7L, null, null, null, null, null));
        verifyNoInteractions(followUpDAO);
    }

    private MentorFollowUpItemDTO item(String type, long days) {
        MentorFollowUpItemDTO value = new MentorFollowUpItemDTO();
        value.setFollowUpType(type); value.setDaysPending(days); value.setStudentName("Student");
        return value;
    }
}
