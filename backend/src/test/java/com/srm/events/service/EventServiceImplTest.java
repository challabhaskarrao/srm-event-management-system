package com.srm.events.service;

import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.dto.CreateEventRequestDTO;
import com.srm.events.dto.EventResponseDTO;
import com.srm.events.model.Event;
import com.srm.events.model.EventMode;
import com.srm.events.service.impl.EventServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventDAO eventDAO;

    @InjectMocks
    private EventServiceImpl eventService;

    private CreateEventRequestDTO validRequest;
    private Event mockEvent;

    @BeforeEach
    void setUp() {
        lenient().when(eventDAO.categoryExists(1L)).thenReturn(true);
        validRequest = new CreateEventRequestDTO();
        validRequest.setName("Tech Conference");
        validRequest.setCategoryId(1L);
        validRequest.setOrganizer("SRM Tech Club");
        validRequest.setStartDate(LocalDate.now());
        validRequest.setEndDate(LocalDate.now().plusDays(2));
        validRequest.setMode(EventMode.HYBRID);
        validRequest.setVenue("Auditorium");
        validRequest.setEventUrl("http://techconf.com");

        mockEvent = new Event();
        mockEvent.setEventId(10L);
        mockEvent.setEventName("Tech Conference");
        mockEvent.setCategoryId(1L);
        mockEvent.setCategoryName("Technology");
        mockEvent.setOrganizer("SRM Tech Club");
        mockEvent.setStartDate(LocalDate.now());
        mockEvent.setEndDate(LocalDate.now().plusDays(2));
        mockEvent.setMode(EventMode.HYBRID);
        mockEvent.setCreatedByUserId(5L);
        mockEvent.setCreatedByEmail("creator@example.com");
    }

    @Test
    void createEvent_shouldSucceedWithValidDates() {
        // Arrange
        when(eventDAO.create(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setEventId(10L);
            return event;
        });
        when(eventDAO.findById(10L)).thenReturn(Optional.of(mockEvent));

        // Act
        EventResponseDTO response = eventService.createEvent(validRequest, 5L);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getEventId());
        assertEquals("Technology", response.getCategoryName());
        verify(eventDAO).create(any(Event.class));
    }

    @Test
    void createEvent_shouldThrowBadRequest_whenEndDateIsBeforeStartDate() {
        // Arrange
        validRequest.setEndDate(LocalDate.now().minusDays(1)); // Invalid end date

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            eventService.createEvent(validRequest, 5L)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(eventDAO, never()).create(any(Event.class));
    }

    @Test
    void getEventById_shouldReturnEvent_whenFound() {
        // Arrange
        when(eventDAO.findById(10L)).thenReturn(Optional.of(mockEvent));

        // Act
        EventResponseDTO response = eventService.getEventById(10L);

        // Assert
        assertNotNull(response);
        assertEquals("Tech Conference", response.getEventName());
    }

    @Test
    void getEventById_shouldThrowNotFound_whenNotFound() {
        // Arrange
        when(eventDAO.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            eventService.getEventById(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateEvent_shouldValidateAndPersistSupportedFields() {
        when(eventDAO.findById(10L)).thenReturn(Optional.of(mockEvent), Optional.of(mockEvent));
        when(eventDAO.update(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        EventResponseDTO response = eventService.updateEvent(10L, validRequest);
        assertEquals("Tech Conference", response.getEventName());
        verify(eventDAO).update(mockEvent);
    }

    @Test
    void createEvent_shouldRejectUnsafeUrl() {
        validRequest.setEventUrl("javascript:alert(1)");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> eventService.createEvent(validRequest, 5L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getAvailableEvents_shouldUseAvailableEventQuery() {
        when(eventDAO.findAvailable(25, 0)).thenReturn(List.of(mockEvent));

        assertEquals(1, eventService.getAvailableEvents(25, 0).size());
        verify(eventDAO).findAvailable(25, 0);
        verify(eventDAO, never()).findAll(anyInt(), anyInt());
    }
}
