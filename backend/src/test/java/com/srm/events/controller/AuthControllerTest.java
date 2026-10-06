package com.srm.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.events.dto.AuthResponseDTO;
import com.srm.events.dto.LoginRequestDTO;
import com.srm.events.service.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_shouldReturn200AndAuthResponse_whenSuccessful() throws Exception {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO("test@example.com", "password");
        AuthResponseDTO responseDTO = new AuthResponseDTO("valid.token", 1L, "test@example.com", "ROLE_USER");
        
        when(authService.login(any(LoginRequestDTO.class), eq("127.0.0.1"))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("valid.token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void getCurrentUser_shouldReturn200_withBearerToken() throws Exception {
        // Arrange
        AuthResponseDTO responseDTO = new AuthResponseDTO(null, 1L, "test@example.com", "ROLE_USER");
        when(authService.getCurrentUser(eq("Bearer valid.token"))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
