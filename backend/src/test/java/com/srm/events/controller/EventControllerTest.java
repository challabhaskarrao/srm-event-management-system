package com.srm.events.controller;

import com.srm.events.dto.EventResponseDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {
    @Mock private EventService eventService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventService, jwtTokenProvider)).build();
    }

    @Test
    void createEvent_shouldRejectStudent() throws Exception {
        when(jwtTokenProvider.validateToken("student-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("student-token")).thenReturn("STUDENT");

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer student-token")
                        .contentType("application/json")
                        .content(validEventJson()))
                .andExpect(status().isForbidden());

        verify(eventService, never()).createEvent(any(), any());
    }

    @Test
    void createEvent_shouldAllowAdmin() throws Exception {
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("admin-token")).thenReturn("ADMIN");
        when(jwtTokenProvider.getUserIdFromToken("admin-token")).thenReturn(1L);
        when(eventService.createEvent(any(), any())).thenReturn(new EventResponseDTO());

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer admin-token")
                        .contentType("application/json")
                        .content(validEventJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void availableEvents_shouldBePubliclyReadable() throws Exception {
        when(eventService.getAvailableEvents(50, 0)).thenReturn(List.of());
        mockMvc.perform(get("/api/events/available")).andExpect(status().isOk());
    }

    @Test
    void updateEvent_shouldRejectNonAdmin() throws Exception {
        when(jwtTokenProvider.validateToken("mentor-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("mentor-token")).thenReturn("MENTOR");
        mockMvc.perform(put("/api/events/1").header("Authorization", "Bearer mentor-token")
                        .contentType("application/json").content(validEventJson()))
                .andExpect(status().isForbidden());
        verify(eventService, never()).updateEvent(any(), any());
    }

    private String validEventJson() {
        return """
                {"name":"Future Event","categoryId":1,"organizer":"SRM",\
                "startDate":"2030-01-10","endDate":"2030-01-11","mode":"OFFLINE"}
                """;
    }
}
