package com.srm.events.service;

import com.srm.events.dao.interfaces.ApplicationDAO;
import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dto.ApplicationResponseDTO;
import com.srm.events.dto.RegisterApplicationRequestDTO;
import com.srm.events.model.Application;
import com.srm.events.model.Event;
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

        Application createdApp = new Application();
        createdApp.setApplicationCode("APP-1234");
        when(applicationDAO.create(any(Application.class), any())).thenReturn(createdApp);

        // Act
        ApplicationResponseDTO response = applicationService.registerApplication(validRequest, 2L);

        // Assert
        assertNotNull(response);
        assertEquals("APP-1234", response.getApplicationCode());
        assertEquals("Hackathon", response.getEventName());
        assertEquals("John Doe", response.getStudentName());
        
        verify(applicationDAO).create(any(Application.class), any());
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
        verify(applicationDAO, never()).create(any(Application.class), any());
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
        verify(applicationDAO, never()).create(any(Application.class), any());
    }
}
