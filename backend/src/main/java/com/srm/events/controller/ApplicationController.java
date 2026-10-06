package com.srm.events.controller;

import com.srm.events.dto.ApplicationResponseDTO;
import com.srm.events.dto.RegisterApplicationRequestDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public ApplicationController(ApplicationService applicationService, JwtTokenProvider jwtTokenProvider) {
        this.applicationService = applicationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private Long getUserIdFromHeader(String authHeader) {
        String token = extractToken(authHeader);
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    private String getRoleFromHeader(String authHeader) {
        String token = extractToken(authHeader);
        return jwtTokenProvider.getRoleFromToken(token);
    }

    private void requireRole(String authHeader, String expectedRole) {
        if (!expectedRole.equalsIgnoreCase(getRoleFromHeader(authHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, expectedRole + " access required");
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        }
        String token = authHeader.substring(7).trim();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<ApplicationResponseDTO> registerApplication(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody RegisterApplicationRequestDTO request) {
        requireRole(authHeader, "STUDENT");
        Long userId = getUserIdFromHeader(authHeader);
        ApplicationResponseDTO response = applicationService.registerApplication(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponseDTO>> getMyApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireRole(authHeader, "STUDENT");
        Long userId = getUserIdFromHeader(authHeader);
        List<ApplicationResponseDTO> responses = applicationService.getMyApplications(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationByCode(
            @PathVariable String code,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        requireRole(authHeader, "STUDENT");
        Long userId = getUserIdFromHeader(authHeader);
        ApplicationResponseDTO response = applicationService.getApplicationByCode(code, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post-event/pending")
    public ResponseEntity<List<ApplicationResponseDTO>> getPostEventPending(
            @RequestParam String role,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        String tokenRole = getRoleFromHeader(authHeader);
        if (!role.equalsIgnoreCase(tokenRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role mismatch");
        }
        return ResponseEntity.ok(applicationService.getPostEventPending(userId, tokenRole));
    }

    @GetMapping("/review-queue")
    public ResponseEntity<List<ApplicationResponseDTO>> getReviewQueue(
            @RequestParam String role,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        String tokenRole = getRoleFromHeader(authHeader);
        if (!role.equalsIgnoreCase(tokenRole)) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role mismatch");
        }
        return ResponseEntity.ok(applicationService.getReviewQueue(userId, tokenRole));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApplicationResponseDTO> reviewApplication(
            @PathVariable Long id,
            @Valid @RequestBody com.srm.events.dto.ReviewApplicationRequestDTO review,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        String role = getRoleFromHeader(authHeader);
        ApplicationResponseDTO response = applicationService.reviewApplication(id, review, userId, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRoleFromHeader(authHeader);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return ResponseEntity.ok(applicationService.getAllApplications());
    }
}
