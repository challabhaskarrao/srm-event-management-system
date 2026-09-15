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
@CrossOrigin
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public ApplicationController(ApplicationService applicationService, JwtTokenProvider jwtTokenProvider) {
        this.applicationService = applicationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        }
        String token = authHeader.substring(7).trim();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @PostMapping("/register")
    public ResponseEntity<ApplicationResponseDTO> registerApplication(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RegisterApplicationRequestDTO request) {
        Long userId = getUserIdFromHeader(authHeader);
        ApplicationResponseDTO response = applicationService.registerApplication(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponseDTO>> getMyApplications(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        List<ApplicationResponseDTO> responses = applicationService.getMyApplications(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationByCode(
            @PathVariable String code,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        ApplicationResponseDTO response = applicationService.getApplicationByCode(code, userId);
        return ResponseEntity.ok(response);
    }
}
