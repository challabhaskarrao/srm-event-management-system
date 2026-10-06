package com.srm.events.controller;

import com.srm.events.dto.FacultyProvisionRequestDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.impl.AccountProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/provisioning")
public class AdminProvisioningController {
    private final AccountProvisioningService service;
    private final JwtTokenProvider jwt;

    public AdminProvisioningController(AccountProvisioningService service, JwtTokenProvider jwt) {
        this.service = service; this.jwt = jwt;
    }

    @GetMapping("/faculty")
    public ResponseEntity<?> facultyOptions(@RequestHeader(value = "Authorization", required = false) String header) {
        requireAdmin(header);
        return ResponseEntity.ok(service.findProvisionableFaculty());
    }

    @PostMapping("/faculty")
    public ResponseEntity<?> provisionFaculty(@RequestHeader(value = "Authorization", required = false) String header,
                                              @Valid @RequestBody FacultyProvisionRequestDTO request) {
        requireAdmin(header);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.provisionFaculty(request));
    }

    private void requireAdmin(String header) {
        if (header == null || !header.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        String token = header.substring(7).trim();
        if (!jwt.validateToken(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        if (!"ADMIN".equalsIgnoreCase(jwt.getRoleFromToken(token))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }
}
