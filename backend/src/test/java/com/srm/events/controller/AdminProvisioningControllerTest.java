package com.srm.events.controller;

import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.impl.AccountProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminProvisioningControllerTest {
    private MockMvc mockMvc;
    @Mock private AccountProvisioningService service;
    @Mock private JwtTokenProvider jwt;

    @BeforeEach
    void setUp() { mockMvc = MockMvcBuilders.standaloneSetup(new AdminProvisioningController(service, jwt)).build(); }

    @Test
    void nonAdminCannotListProvisionableFaculty() throws Exception {
        when(jwt.validateToken("mentor-token")).thenReturn(true);
        when(jwt.getRoleFromToken("mentor-token")).thenReturn("MENTOR");
        mockMvc.perform(get("/api/admin/provisioning/faculty").header("Authorization", "Bearer mentor-token"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void adminCanListOnlyServiceProvidedRegistryOptions() throws Exception {
        when(jwt.validateToken("admin-token")).thenReturn(true);
        when(jwt.getRoleFromToken("admin-token")).thenReturn("ADMIN");
        when(service.findProvisionableFaculty()).thenReturn(List.of(Map.of("registryId", 7L, "facultyCode", "F-7", "role", "MENTOR")));
        mockMvc.perform(get("/api/admin/provisioning/faculty").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
        verify(service).findProvisionableFaculty();
    }
}
