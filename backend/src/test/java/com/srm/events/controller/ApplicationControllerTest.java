package com.srm.events.controller;

import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ApplicationController(applicationService, jwtTokenProvider)).build();
    }

    @Test
    void allApplications_shouldRejectMissingToken() throws Exception {
        mockMvc.perform(get("/api/applications/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allApplications_shouldRejectMentorToken() throws Exception {
        when(jwtTokenProvider.validateToken("mentor-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("mentor-token")).thenReturn("MENTOR");

        mockMvc.perform(get("/api/applications/all")
                        .header("Authorization", "Bearer mentor-token"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getAllApplications();
    }

    @Test
    void allApplications_shouldAllowAdminToken() throws Exception {
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("admin-token")).thenReturn("ADMIN");
        when(applicationService.getAllApplications()).thenReturn(List.of());

        mockMvc.perform(get("/api/applications/all")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void reviewQueue_shouldRejectCrossRoleRequest() throws Exception {
        when(jwtTokenProvider.validateToken("mentor-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("mentor-token")).thenReturn(4L);
        when(jwtTokenProvider.getRoleFromToken("mentor-token")).thenReturn("MENTOR");

        mockMvc.perform(get("/api/applications/review-queue")
                        .queryParam("role", "HOD")
                        .header("Authorization", "Bearer mentor-token"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getReviewQueue(4L, "MENTOR");
    }

    @Test
    void myApplications_shouldRejectStaffRole() throws Exception {
        when(jwtTokenProvider.validateToken("mentor-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("mentor-token")).thenReturn("MENTOR");

        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", "Bearer mentor-token"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getMyApplications(4L);
    }

    @Test
    void allApplications_shouldRejectStudentToken() throws Exception {
        when(jwtTokenProvider.validateToken("student-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("student-token")).thenReturn("STUDENT");

        mockMvc.perform(get("/api/applications/all")
                        .header("Authorization", "Bearer student-token"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getAllApplications();
    }
}
