package com.srm.events.controller;

import com.srm.events.dto.StudentProfileDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {
    @Mock private StudentService studentService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentController(studentService, jwtTokenProvider)).build();
    }

    @Test
    void myProfile_shouldUseAuthenticatedStudentIdentity() throws Exception {
        StudentProfileDTO profile = new StudentProfileDTO();
        profile.setRegistrationNumber("RA001");
        profile.setName("Student One");
        when(jwtTokenProvider.validateToken("student-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("student-token")).thenReturn("STUDENT");
        when(jwtTokenProvider.getUserIdFromToken("student-token")).thenReturn(2L);
        when(studentService.getMyProfile(2L)).thenReturn(profile);

        mockMvc.perform(get("/api/students/me").header("Authorization", "Bearer student-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("RA001"));

        verify(studentService).getMyProfile(2L);
    }

    @Test
    void myProfile_shouldRejectStaffRole() throws Exception {
        when(jwtTokenProvider.validateToken("mentor-token")).thenReturn(true);
        when(jwtTokenProvider.getRoleFromToken("mentor-token")).thenReturn("MENTOR");

        mockMvc.perform(get("/api/students/me").header("Authorization", "Bearer mentor-token"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getMyProfile(2L);
    }
}
