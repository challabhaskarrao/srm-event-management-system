package com.srm.events.controller;

import com.srm.events.dto.StudentProfileDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    private final JwtTokenProvider jwtTokenProvider;

    public StudentController(StudentService studentService, JwtTokenProvider jwtTokenProvider) {
        this.studentService = studentService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<StudentProfileDTO> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (!"STUDENT".equalsIgnoreCase(jwtTokenProvider.getRoleFromToken(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }
        return ResponseEntity.ok(studentService.getMyProfile(jwtTokenProvider.getUserIdFromToken(token)));
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
}
