package com.srm.events.service;

import com.srm.events.dao.interfaces.ApplicationDAO;
import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dto.ApplicationResponseDTO;
import com.srm.events.dto.RegisterApplicationRequestDTO;
import com.srm.events.dto.ReviewApplicationRequestDTO;
import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.Event;
import com.srm.events.model.Faculty;
import com.srm.events.model.ParticipationType;
import com.srm.events.model.Student;
import com.srm.events.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private EventDAO eventDAO;

    @Mock
    private FacultyDAO facultyDAO;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Student mockStudent;
    private Event mockEvent;
    private RegisterApplicationRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        mockStudent = new Student();
        mockStudent.setStudentId(100L);
        mockStudent.setUserId(2L);
        mockStudent.setName("John Doe");
        mockStudent.setRegisterNumber("RA001");
        mockStudent.setDepartmentId(1L);

        mockEvent = new Event();
        mockEvent.setEventId(50L);
        mockEvent.setEventName("Hackathon");
        mockEvent.setStartDate(LocalDate.now().plusDays(10));
        mockEvent.setEndDate(LocalDate.now().plusDays(12));

        validRequest = new RegisterApplicationRequestDTO();
        validRequest.setEventId(50L);
        validRequest.setParticipationType(ParticipationType.INDIVIDUAL);
    }

    @Test
    void registerApplication_shouldSucceed_whenValid() {
        // Arrange
        when(studentDAO.findByUserId(2L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));
        when(applicationDAO.existsByStudentAndEvent(100L, 50L)).thenReturn(false);
        when(applicationDAO.findActiveMentorIdsForStudent(100L)).thenReturn(List.of(20L));

        Application createdApp = new Application();
        createdApp.setApplicationCode("APP-1234");
        when(applicationDAO.create(any(Application.class), any(), anyList())).thenReturn(createdApp);

        // Act
        ApplicationResponseDTO response = applicationService.registerApplication(validRequest, 2L);

        // Assert
        assertNotNull(response);
        assertEquals("APP-1234", response.getApplicationCode());
        assertEquals("Hackathon", response.getEventName());
        assertEquals("John Doe", response.getStudentName());
        
        verify(applicationDAO).create(argThat(application -> application.getAssignedMentorId().equals(20L)), any(), anyList());
    }

    @Test
    void registerApplication_shouldRejectMissingMentorAssignment() {
        when(studentDAO.findByUserId(2L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));
        when(applicationDAO.existsByStudentAndEvent(100L, 50L)).thenReturn(false);
        when(applicationDAO.findActiveMentorIdsForStudent(100L)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.registerApplication(validRequest, 2L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(applicationDAO, never()).create(any(), any(), anyList());
    }

    @Test
    void registerApplication_shouldRejectAmbiguousMentorAssignment() {
        when(studentDAO.findByUserId(2L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));
        when(applicationDAO.existsByStudentAndEvent(100L, 50L)).thenReturn(false);
        when(applicationDAO.findActiveMentorIdsForStudent(100L)).thenReturn(List.of(20L, 21L));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.registerApplication(validRequest, 2L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(applicationDAO, never()).create(any(), any(), anyList());
    }

    @Test
    void registerApplication_shouldThrowConflict_whenAlreadyRegistered() {
        // Arrange
        when(studentDAO.findByUserId(2L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));
        when(applicationDAO.existsByStudentAndEvent(100L, 50L)).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            applicationService.registerApplication(validRequest, 2L)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(applicationDAO, never()).create(any(Application.class), any(), anyList());
    }

    @Test
    void registerApplication_shouldThrowBadRequest_whenEventAlreadyStarted() {
        // Arrange
        mockEvent.setStartDate(LocalDate.now().minusDays(1)); // Past event
        when(studentDAO.findByUserId(2L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            applicationService.registerApplication(validRequest, 2L)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(applicationDAO, never()).create(any(Application.class), any(), anyList());
    }

    @Test
    void mentorReview_shouldUseFacultyIdAndAdvanceToHodQueue() {
        Application submitted = application(10L, ApplicationStatus.SUBMITTED);
        Application advanced = application(10L, ApplicationStatus.PENDING_HOD_APPROVAL);
        Faculty mentor = faculty(20L, 1L);
        ReviewApplicationRequestDTO review = review("RECOMMENDED", "Complete documents");

        when(applicationDAO.findById(10L)).thenReturn(Optional.of(submitted), Optional.of(advanced));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(mentor));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(true);
        when(applicationDAO.updateMentorReview(10L, ApplicationStatus.SUBMITTED,
                ApplicationStatus.PENDING_HOD_APPROVAL, "RECOMMENDED", "Complete documents", 20L, 200L)).thenReturn(true);
        when(studentDAO.findById(100L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));

        ApplicationResponseDTO response = applicationService.reviewApplication(10L, review, 200L, "MENTOR");

        assertEquals(ApplicationStatus.PENDING_HOD_APPROVAL, response.getStatus());
        verify(applicationDAO).updateMentorReview(10L, ApplicationStatus.SUBMITTED,
                ApplicationStatus.PENDING_HOD_APPROVAL, "RECOMMENDED", "Complete documents", 20L, 200L);
    }

    @Test
    void mentorReview_shouldRejectUnassignedApplication() {
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(application(10L, ApplicationStatus.SUBMITTED)));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(faculty(20L, 1L)));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(10L, review("RECOMMENDED", null), 200L, "MENTOR"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(applicationDAO, never()).updateMentorReview(anyLong(), any(), any(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void mentorReview_shouldRejectUnknownActionInsteadOfTreatingItAsRejection() {
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(application(10L, ApplicationStatus.SUBMITTED)));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(faculty(20L, 1L)));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(10L, review("APPROVED", null), 200L, "MENTOR"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(applicationDAO, never()).updateMentorReview(anyLong(), any(), any(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void rejection_shouldRequireRemarks() {
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(application(10L, ApplicationStatus.SUBMITTED)));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(faculty(20L, 1L)));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(10L, review("REJECTED", "  "), 200L, "MENTOR"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void mentorRejection_shouldPersistDedicatedStatusAndRemarks() {
        Application submitted = application(10L, ApplicationStatus.SUBMITTED);
        Application rejected = application(10L, ApplicationStatus.REJECTED_BY_MENTOR);
        rejected.setMentorRemarks("Missing proof");
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(submitted), Optional.of(rejected));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(faculty(20L, 1L)));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(true);
        when(applicationDAO.updateMentorReview(10L, ApplicationStatus.SUBMITTED,
                ApplicationStatus.REJECTED_BY_MENTOR, "REJECTED", "Missing proof", 20L, 200L)).thenReturn(true);
        when(studentDAO.findById(100L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));

        ApplicationResponseDTO response = applicationService.reviewApplication(
                10L, review("REJECTED", "Missing proof"), 200L, "MENTOR");

        assertEquals(ApplicationStatus.REJECTED_BY_MENTOR, response.getStatus());
        assertEquals("Missing proof", response.getMentorRemarks());
    }

    @Test
    void mentorReturn_shouldRequireRemarksAndUseCorrectionStatus() {
        Application submitted = application(10L, ApplicationStatus.SUBMITTED);
        Application returned = application(10L, ApplicationStatus.RETURNED_BY_MENTOR);
        returned.setMentorRemarks("Please correct the event date.");
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(submitted), Optional.of(returned));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(faculty(20L, 1L)));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(true);
        when(applicationDAO.updateMentorReview(10L, ApplicationStatus.SUBMITTED,
                ApplicationStatus.RETURNED_BY_MENTOR, "RETURNED_BY_MENTOR",
                "Please correct the event date.", 20L, 200L)).thenReturn(true);
        when(studentDAO.findById(100L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));

        ApplicationResponseDTO response = applicationService.reviewApplication(
                10L, review("RETURNED_BY_MENTOR", "Please correct the event date."), 200L, "MENTOR");

        assertEquals(ApplicationStatus.RETURNED_BY_MENTOR, response.getStatus());
        assertEquals("Please correct the event date.", response.getMentorRemarks());
        verify(applicationDAO).updateMentorReview(10L, ApplicationStatus.SUBMITTED,
                ApplicationStatus.RETURNED_BY_MENTOR, "RETURNED_BY_MENTOR",
                "Please correct the event date.", 20L, 200L);
    }

    @Test
    void mentorReturn_shouldRejectMissingRemarks() {
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(application(10L, ApplicationStatus.SUBMITTED)));
        when(facultyDAO.findActiveByUserId(200L)).thenReturn(Optional.of(faculty(20L, 1L)));
        when(applicationDAO.isMentorAssignedToApplication(10L, 20L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(
                        10L, review("RETURNED_BY_MENTOR", "  "), 200L, "MENTOR"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(applicationDAO, never()).updateMentorReview(anyLong(), any(), any(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void hodReview_shouldEnforceDepartmentAndApprove() {
        Application pending = application(10L, ApplicationStatus.PENDING_HOD_APPROVAL);
        Application approved = application(10L, ApplicationStatus.APPROVED);
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(pending), Optional.of(approved));
        when(facultyDAO.findActiveByUserId(300L)).thenReturn(Optional.of(faculty(30L, 1L)));
        when(applicationDAO.isApplicationInDepartment(10L, 1L)).thenReturn(true);
        when(applicationDAO.updateHodReview(10L, ApplicationStatus.PENDING_HOD_APPROVAL,
                ApplicationStatus.APPROVED, "APPROVED", "Approved", 30L, 300L)).thenReturn(true);
        when(studentDAO.findById(100L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));

        ApplicationResponseDTO response = applicationService.reviewApplication(
                10L, review("APPROVED", "Approved"), 300L, "HOD");

        assertEquals(ApplicationStatus.APPROVED, response.getStatus());
    }

    @Test
    void hodReturn_shouldUseCorrectionStatusAndRemainDepartmentScoped() {
        Application pending = application(10L, ApplicationStatus.PENDING_HOD_APPROVAL);
        Application returned = application(10L, ApplicationStatus.RETURNED_BY_HOD);
        returned.setHodRemarks("Please attach the official confirmation.");
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(pending), Optional.of(returned));
        when(facultyDAO.findActiveByUserId(300L)).thenReturn(Optional.of(faculty(30L, 1L)));
        when(applicationDAO.isApplicationInDepartment(10L, 1L)).thenReturn(true);
        when(applicationDAO.updateHodReview(10L, ApplicationStatus.PENDING_HOD_APPROVAL,
                ApplicationStatus.RETURNED_BY_HOD, "RETURNED_BY_HOD",
                "Please attach the official confirmation.", 30L, 300L)).thenReturn(true);
        when(studentDAO.findById(100L)).thenReturn(Optional.of(mockStudent));
        when(eventDAO.findById(50L)).thenReturn(Optional.of(mockEvent));

        ApplicationResponseDTO response = applicationService.reviewApplication(
                10L, review("RETURNED_BY_HOD", "Please attach the official confirmation."), 300L, "HOD");

        assertEquals(ApplicationStatus.RETURNED_BY_HOD, response.getStatus());
        assertEquals("Please attach the official confirmation.", response.getHodRemarks());
        verify(applicationDAO).updateHodReview(10L, ApplicationStatus.PENDING_HOD_APPROVAL,
                ApplicationStatus.RETURNED_BY_HOD, "RETURNED_BY_HOD",
                "Please attach the official confirmation.", 30L, 300L);
    }

    @Test
    void hodReview_shouldRejectApplicationOutsideDepartment() {
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(
                application(10L, ApplicationStatus.PENDING_HOD_APPROVAL)));
        when(facultyDAO.findActiveByUserId(300L)).thenReturn(Optional.of(faculty(30L, 2L)));
        when(applicationDAO.isApplicationInDepartment(10L, 2L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(10L, review("APPROVED", null), 300L, "HOD"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(applicationDAO, never()).updateHodReview(anyLong(), any(), any(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void duplicateReview_shouldBeRejectedAsInvalidTransition() {
        when(applicationDAO.findById(10L)).thenReturn(Optional.of(application(10L, ApplicationStatus.APPROVED)));
        when(facultyDAO.findActiveByUserId(300L)).thenReturn(Optional.of(faculty(30L, 1L)));
        when(applicationDAO.isApplicationInDepartment(10L, 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(10L, review("APPROVED", null), 300L, "HOD"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(applicationDAO, never()).updateHodReview(anyLong(), any(), any(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void review_shouldReturnNotFoundForUnknownApplication() {
        when(applicationDAO.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(999L, review("APPROVED", null), 300L, "HOD"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void adminReview_shouldRemainForbiddenWithoutLookingUpFaculty() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.reviewApplication(10L, review("APPROVED", null), 1L, "ADMIN"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(facultyDAO);
        verify(applicationDAO, never()).findById(anyLong());
    }

    @Test
    void getApplicationByCode_shouldRejectAnotherStudent() {
        Student otherStudent = new Student();
        otherStudent.setStudentId(999L);
        otherStudent.setUserId(3L);
        when(studentDAO.findByUserId(3L)).thenReturn(Optional.of(otherStudent));
        when(applicationDAO.findByCode("APP-TEST")).thenReturn(Optional.of(application(10L, ApplicationStatus.SUBMITTED)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> applicationService.getApplicationByCode("APP-TEST", 3L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(eventDAO, never()).findById(anyLong());
    }

    private Application application(Long id, ApplicationStatus status) {
        Application application = new Application();
        application.setApplicationId(id);
        application.setApplicationCode("APP-TEST");
        application.setCreatedByStudentId(100L);
        application.setEventId(50L);
        application.setParticipationType(ParticipationType.INDIVIDUAL);
        application.setCurrentStatus(status);
        return application;
    }

    private Faculty faculty(Long id, Long departmentId) {
        Faculty faculty = new Faculty();
        faculty.setFacultyId(id);
        faculty.setDepartmentId(departmentId);
        faculty.setActive(true);
        return faculty;
    }

    private ReviewApplicationRequestDTO review(String status, String remarks) {
        ReviewApplicationRequestDTO review = new ReviewApplicationRequestDTO();
        review.setStatus(status);
        review.setRemarks(remarks);
        return review;
    }
}
