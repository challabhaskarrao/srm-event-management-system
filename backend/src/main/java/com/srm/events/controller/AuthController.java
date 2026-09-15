package com.srm.events.controller;

import com.srm.events.dto.AuthResponseDTO;
import com.srm.events.dto.LoginRequestDTO;
import com.srm.events.service.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        AuthResponseDTO response = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(response);
    }
}
