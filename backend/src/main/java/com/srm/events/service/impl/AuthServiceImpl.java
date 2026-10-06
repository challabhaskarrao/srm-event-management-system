package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.PasswordResetTokenDAO;
import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.dto.*;
import com.srm.events.model.User;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.security.LoginAttemptGuard;
import com.srm.events.security.PasswordPolicy;
import com.srm.events.service.interfaces.AuthService;
import com.srm.events.service.interfaces.PasswordRecoveryNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String GENERIC_RECOVERY_MESSAGE =
            "If an eligible account exists, recovery instructions will be issued when delivery is configured.";
    private static final String DUMMY_BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptGuard loginAttemptGuard;
    private final PasswordPolicy passwordPolicy;
    private final PasswordResetTokenDAO resetTokenDAO;
    private final PasswordRecoveryNotifier recoveryNotifier;
    private final SecurityAuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.password-reset.lifetime-seconds:1800}")
    private long resetLifetimeSeconds = 1800;

    public AuthServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                           LoginAttemptGuard loginAttemptGuard, PasswordPolicy passwordPolicy,
                           PasswordResetTokenDAO resetTokenDAO, PasswordRecoveryNotifier recoveryNotifier,
                           SecurityAuditService auditService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptGuard = loginAttemptGuard;
        this.passwordPolicy = passwordPolicy;
        this.resetTokenDAO = resetTokenDAO;
        this.recoveryNotifier = recoveryNotifier;
        this.auditService = auditService;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        return login(request, "unknown");
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request, String clientAddress) {
        String email = normalizeEmail(request == null ? null : request.getEmail());
        String password = request == null ? null : request.getPassword();
        if (email == null || password == null || password.isBlank()) throw invalidCredentials();
        if (loginAttemptGuard.isBlocked(email, clientAddress)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Try again later");
        }

        User user = userDAO.findByEmail(email).orElse(null);
        String passwordHash = user != null && user.getPasswordHash() != null ? user.getPasswordHash() : DUMMY_BCRYPT_HASH;
        boolean passwordMatches = passwordEncoder.matches(password, passwordHash);
        if (user == null || !user.isActive() || user.getPasswordHash() == null || !passwordMatches) {
            loginAttemptGuard.recordFailure(email, clientAddress);
            if (user != null) auditService.record(user.getUserId(), "LOGIN_FAILURE", "Invalid login attempt");
            throw invalidCredentials();
        }

        loginAttemptGuard.recordSuccess(email, clientAddress);
        userDAO.updateLastLogin(user.getUserId());
        auditService.record(user.getUserId(), "LOGIN_SUCCESS", "Authenticated web login");
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRoleName(), user.getSessionVersion());
        return new AuthResponseDTO(token, user.getUserId(), user.getEmail(), user.getRoleName());
    }

    @Override
    public AuthResponseDTO getCurrentUser(String authorizationHeader) {
        String token = authenticatedToken(authorizationHeader);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userDAO.findById(userId).filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
        return new AuthResponseDTO(null, user.getUserId(), user.getEmail(), user.getRoleName());
    }

    @Override
    public MessageResponseDTO changePassword(String authorizationHeader, ChangePasswordRequestDTO request) {
        String token = authenticatedToken(authorizationHeader);
        User user = userDAO.findById(jwtTokenProvider.getUserIdFromToken(token))
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
        passwordPolicy.validate(request.getNewPassword());
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must differ from the current password");
        }
        String newHash = passwordEncoder.encode(request.getNewPassword());
        if (!userDAO.updatePasswordAndIncrementSession(user.getUserId(), user.getPasswordHash(), newHash)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Credentials changed concurrently; sign in again");
        }
        auditService.record(user.getUserId(), "PASSWORD_CHANGED", "Authenticated password change completed");
        return new MessageResponseDTO("Password changed. Sign in again with the new password.");
    }

    @Override
    public MessageResponseDTO requestPasswordReset(ForgotPasswordRequestDTO request) {
        String email = normalizeEmail(request == null ? null : request.getEmail());
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = digest(rawToken);
        if (email != null) {
            userDAO.findByEmail(email).filter(User::isActive).ifPresent(user -> {
                Duration lifetime = Duration.ofSeconds(resetLifetimeSeconds);
                resetTokenDAO.replaceActiveToken(user.getUserId(), tokenHash, LocalDateTime.now().plus(lifetime));
                auditService.record(user.getUserId(), "PASSWORD_RESET_REQUESTED", "Password reset requested");
                try {
                    recoveryNotifier.sendPasswordReset(user, rawToken, lifetime);
                } catch (RuntimeException exception) {
                    LOGGER.error("Password recovery notification failed for userId={}", user.getUserId());
                }
            });
        }
        return new MessageResponseDTO(GENERIC_RECOVERY_MESSAGE, recoveryNotifier.isDeliveryConfigured());
    }

    @Override
    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        passwordPolicy.validate(request.getNewPassword());
        String rawToken = request.getToken() == null ? "" : request.getToken().trim();
        if (rawToken.length() < 32 || rawToken.length() > 200) throw invalidResetToken();
        String newHash = passwordEncoder.encode(request.getNewPassword());
        Long userId = resetTokenDAO.consumeAndResetPassword(digest(rawToken), newHash, LocalDateTime.now())
                .orElseThrow(this::invalidResetToken);
        auditService.record(userId, "PASSWORD_RESET_COMPLETED", "Password reset completed");
        return new MessageResponseDTO("Password reset completed. Sign in with the new password.");
    }

    private String authenticatedToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        }
        String token = authorizationHeader.substring(7).trim();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return token;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    private ResponseStatusException invalidResetToken() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token is invalid or expired");
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
