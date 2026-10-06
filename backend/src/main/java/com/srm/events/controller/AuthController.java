package com.srm.events.controller;

import com.srm.events.dto.*;
import com.srm.events.service.impl.AccountProvisioningService;
import com.srm.events.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountProvisioningService provisioningService;

    @Autowired
    public AuthController(AuthService authService, AccountProvisioningService provisioningService) {
        this.authService = authService;
        this.provisioningService = provisioningService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                                  HttpServletRequest request) {
        AuthResponseDTO response = authService.login(loginRequest, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        AuthResponseDTO response = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        return ResponseEntity.ok(authService.changePassword(authHeader, request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        return ResponseEntity.accepted().body(authService.requestPasswordReset(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/forgot-password-otp")
    public ResponseEntity<?> forgotPasswordOtp(@Valid @RequestBody PasswordRecoveryOtpStartRequestDTO request) {
        return ResponseEntity.accepted().body(provisioningService.startPasswordRecovery(request));
    }

    @PostMapping("/reset-password-otp")
    public ResponseEntity<?> resetPasswordOtp(@Valid @RequestBody PasswordRecoveryOtpResetRequestDTO request) {
        return ResponseEntity.ok(provisioningService.resetPasswordWithOtp(request));
    }
}
