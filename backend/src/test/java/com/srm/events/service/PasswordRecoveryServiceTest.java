package com.srm.events.service;

import com.srm.events.dao.interfaces.PasswordResetTokenDAO;
import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.dto.ForgotPasswordRequestDTO;
import com.srm.events.dto.MessageResponseDTO;
import com.srm.events.dto.ResetPasswordRequestDTO;
import com.srm.events.model.User;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.security.LoginAttemptGuard;
import com.srm.events.security.PasswordPolicy;
import com.srm.events.service.impl.AuthServiceImpl;
import com.srm.events.service.impl.SecurityAuditService;
import com.srm.events.service.interfaces.PasswordRecoveryNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordRecoveryServiceTest {
    private UserDAO users;
    private PasswordResetTokenDAO tokens;
    private PasswordRecoveryNotifier notifier;
    private AuthServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        users = mock(UserDAO.class);
        tokens = mock(PasswordResetTokenDAO.class);
        notifier = mock(PasswordRecoveryNotifier.class);
        user = new User(); user.setUserId(7L); user.setEmail("student@srmist.edu.in"); user.setActive(true);
        service = new AuthServiceImpl(users, new BCryptPasswordEncoder(4), mock(JwtTokenProvider.class),
                mock(LoginAttemptGuard.class), new PasswordPolicy(), tokens, notifier, mock(SecurityAuditService.class));
    }

    @Test
    void existingAccountStoresOnlyDigestAndNotifiesWithOpaqueToken() throws Exception {
        when(users.findByEmail("student@srmist.edu.in")).thenReturn(Optional.of(user));
        ForgotPasswordRequestDTO request = forgot("  STUDENT@srmist.edu.in ");

        service.requestPasswordReset(request);

        ArgumentCaptor<String> storedHash = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rawToken = ArgumentCaptor.forClass(String.class);
        verify(tokens).replaceActiveToken(eq(7L), storedHash.capture(), any(LocalDateTime.class));
        verify(notifier).sendPasswordReset(eq(user), rawToken.capture(), any(Duration.class));
        assertNotEquals(rawToken.getValue(), storedHash.getValue());
        String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getValue().getBytes(StandardCharsets.UTF_8)));
        assertEquals(expectedHash, storedHash.getValue());
    }

    @Test
    void existingAndMissingAccountsReceiveEquivalentPublicResponse() {
        when(users.findByEmail("student@srmist.edu.in")).thenReturn(Optional.of(user));
        when(users.findByEmail("missing@srmist.edu.in")).thenReturn(Optional.empty());
        MessageResponseDTO existing = service.requestPasswordReset(forgot("student@srmist.edu.in"));
        MessageResponseDTO missing = service.requestPasswordReset(forgot("missing@srmist.edu.in"));
        assertEquals(existing.message(), missing.message());
        assertEquals(existing.deliveryConfigured(), missing.deliveryConfigured());
    }

    @Test
    void validTokenResetsPasswordAndSecondUseIsRejected() {
        String raw = "a".repeat(43);
        when(tokens.consumeAndResetPassword(anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(7L), Optional.empty());
        service.resetPassword(reset(raw));
        ResponseStatusException second = assertThrows(ResponseStatusException.class,
                () -> service.resetPassword(reset(raw)));
        assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
    }

    @Test void expiredTokenIsRejected() {
        when(tokens.consumeAndResetPassword(anyString(), anyString(), any(LocalDateTime.class))).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.resetPassword(reset("b".repeat(43)))).getStatusCode());
    }

    @Test void malformedTokenIsRejectedBeforeDatabaseLookup() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.resetPassword(reset("short"))).getStatusCode());
        verifyNoInteractions(tokens);
    }

    private ForgotPasswordRequestDTO forgot(String email) {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO(); request.setEmail(email); return request;
    }

    private ResetPasswordRequestDTO reset(String token) {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(); request.setToken(token);
        request.setNewPassword("SecurePassword1"); return request;
    }
}
