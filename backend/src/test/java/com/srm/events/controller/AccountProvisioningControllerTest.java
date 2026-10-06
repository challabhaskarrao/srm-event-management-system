package com.srm.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.events.dto.StudentSignupRequestDTO;
import com.srm.events.service.impl.AccountProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountProvisioningControllerTest {
    private MockMvc mockMvc;
    @Mock private AccountProvisioningService service;

    @BeforeEach
    void setUp() { mockMvc = MockMvcBuilders.standaloneSetup(new AccountProvisioningController(service)).build(); }

    @Test
    void publicFacultySignupRouteIsUnavailable() throws Exception {
        mockMvc.perform(post("/api/provisioning/faculty/signup").contentType("application/json")
                .content("{\"facultyCode\":\"F-1\",\"email\":\"f@example.invalid\",\"password\":\"SecurePassword1\",\"confirmPassword\":\"SecurePassword1\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentSignupRouteRemainsMapped() throws Exception {
        StudentSignupRequestDTO request = new StudentSignupRequestDTO("S-1", "A", "s@example.invalid", "SecurePassword1", "SecurePassword1");
        mockMvc.perform(post("/api/provisioning/student/signup").contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void facultyOtpVerificationIsRejectedByTheStudentOnlyFlow() throws Exception {
        mockMvc.perform(post("/api/provisioning/verify-otp").contentType("application/json")
                .content("{\"registryType\":\"FACULTY\",\"identifier\":\"F-1\",\"otp\":\"123456\"}"))
                .andExpect(status().isBadRequest());
    }
}
