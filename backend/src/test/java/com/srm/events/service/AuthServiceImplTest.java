package com.srm.events.service;

import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.dao.interfaces.PasswordResetTokenDAO;
import com.srm.events.dto.AuthResponseDTO;
import com.srm.events.dto.ChangePasswordRequestDTO;
import com.srm.events.dto.LoginRequestDTO;
import com.srm.events.model.User;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.security.LoginAttemptGuard;
import com.srm.events.security.PasswordPolicy;
import com.srm.events.service.impl.AuthServiceImpl;
import com.srm.events.service.impl.SecurityAuditService;
import com.srm.events.service.interfaces.PasswordRecoveryNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock private LoginAttemptGuard loginAttemptGuard;
    @Mock private PasswordPolicy passwordPolicy;
    @Mock private PasswordResetTokenDAO resetTokenDAO;
    @Mock private PasswordRecoveryNotifier recoveryNotifier;
    @Mock private SecurityAuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private LoginRequestDTO validLoginRequest;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUserId(1L);
        activeUser.setEmail("test@example.com");
        activeUser.setPasswordHash("hashed_password");
        activeUser.setRoleName("ROLE_USER");
        activeUser.setActive(true);

        validLoginRequest = new LoginRequestDTO("test@example.com", "password123");
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        // Arrange
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "test@example.com", "ROLE_USER", 0L)).thenReturn("mocked.jwt.token");

        // Act
        AuthResponseDTO response = authService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("ROLE_USER", response.getRole());

        verify(userDAO, times(1)).updateLastLogin(1L);
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordIsInvalid() {
        // Arrange
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login(validLoginRequest));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(userDAO, never()).updateLastLogin(anyLong());
        verify(jwtTokenProvider, never()).generateToken(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void login_shouldThrowUnauthorized_whenUserIsInactive() {
        // Arrange
        activeUser.setActive(false);
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login(validLoginRequest));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(passwordEncoder).matches("password123", "hashed_password");
    }

    @Test
    void login_shouldThrowUnauthorized_whenUserNotFound() {
        // Arrange
        when(userDAO.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        LoginRequestDTO unknownLogin = new LoginRequestDTO("unknown@example.com", "pass");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login(unknownLogin));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void getCurrentUser_shouldReturnAuthResponse_whenTokenIsValid() {
        // Arrange
        String token = "Bearer valid.token.here";
        String rawToken = "valid.token.here";
        
        when(jwtTokenProvider.validateToken(rawToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(rawToken)).thenReturn(1L);
        when(userDAO.findById(1L)).thenReturn(Optional.of(activeUser));

        // Act
        AuthResponseDTO response = authService.getCurrentUser(token);

        // Assert
        assertNotNull(response);
        assertNull(response.getToken());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void getCurrentUser_shouldThrowUnauthorized_whenTokenIsInvalid() {
        // Arrange
        String token = "Bearer invalid.token";
        when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.getCurrentUser(token));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid or expired token", exception.getReason());
    }

    @Test
    void changePasswordVerifiesCurrentPasswordAndIncrementsSession() {
        activeUser.setPasswordHash("old-hash");
        when(jwtTokenProvider.validateToken("valid.token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid.token")).thenReturn(1L);
        when(userDAO.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("CurrentPassword1", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");
        when(userDAO.updatePasswordAndIncrementSession(1L, "old-hash", "new-hash")).thenReturn(true);
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("CurrentPassword1"); request.setNewPassword("NewPassword123");

        authService.changePassword("Bearer valid.token", request);

        verify(passwordPolicy).validate("NewPassword123");
        verify(userDAO).updatePasswordAndIncrementSession(1L, "old-hash", "new-hash");
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        activeUser.setPasswordHash("old-hash");
        when(jwtTokenProvider.validateToken("valid.token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid.token")).thenReturn(1L);
        when(userDAO.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongPassword1", "old-hash")).thenReturn(false);
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("WrongPassword1"); request.setNewPassword("NewPassword123");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.changePassword("Bearer valid.token", request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(userDAO, never()).updatePasswordAndIncrementSession(anyLong(), anyString(), anyString());
    }
}
