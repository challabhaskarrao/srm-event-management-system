package com.srm.events.service;

import com.srm.events.dao.interfaces.PasswordResetTokenDAO;
import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.dto.ChangePasswordRequestDTO;
import com.srm.events.dto.LoginRequestDTO;
import com.srm.events.model.User;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.security.LoginAttemptGuard;
import com.srm.events.security.PasswordPolicy;
import com.srm.events.service.impl.AuthServiceImpl;
import com.srm.events.service.impl.SecurityAuditService;
import com.srm.events.service.interfaces.PasswordRecoveryNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordChangeLoginRegressionTest {
    @Test
    void newPasswordLogsInAndOldPasswordIsRejectedAfterChange() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        User user = new User(); user.setUserId(11L); user.setEmail("student@srmist.edu.in");
        user.setRoleName("STUDENT"); user.setActive(true); user.setPasswordHash(encoder.encode("OldPassword123"));
        UserDAO users = mock(UserDAO.class);
        when(users.findById(11L)).thenReturn(Optional.of(user));
        when(users.findByEmail("student@srmist.edu.in")).thenReturn(Optional.of(user));
        when(users.updatePasswordAndIncrementSession(eq(11L), anyString(), anyString())).thenAnswer(invocation -> {
            user.setPasswordHash(invocation.getArgument(2)); user.setSessionVersion(user.getSessionVersion() + 1); return true;
        });
        JwtTokenProvider jwt = mock(JwtTokenProvider.class);
        when(jwt.validateToken("valid-token")).thenReturn(true);
        when(jwt.getUserIdFromToken("valid-token")).thenReturn(11L);
        when(jwt.generateToken(eq(11L), anyString(), eq("STUDENT"), anyLong())).thenReturn("new-token");
        LoginAttemptGuard guard = mock(LoginAttemptGuard.class);
        AuthServiceImpl service = new AuthServiceImpl(users, encoder, jwt, guard, new PasswordPolicy(),
                mock(PasswordResetTokenDAO.class), mock(PasswordRecoveryNotifier.class), mock(SecurityAuditService.class));
        ChangePasswordRequestDTO change = new ChangePasswordRequestDTO();
        change.setCurrentPassword("OldPassword123"); change.setNewPassword("NewPassword456");

        service.changePassword("Bearer valid-token", change);

        ResponseStatusException oldPassword = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequestDTO("student@srmist.edu.in", "OldPassword123")));
        assertEquals(HttpStatus.UNAUTHORIZED, oldPassword.getStatusCode());
        assertEquals("new-token", service.login(new LoginRequestDTO("student@srmist.edu.in", "NewPassword456")).getToken());
    }
}
