package com.srm.events.controller;

import com.srm.events.dto.MentorFollowUpResponseDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.impl.MentorFollowUpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/mentor/follow-ups")
public class MentorFollowUpController {
    private final MentorFollowUpService service;
    private final JwtTokenProvider jwt;

    public MentorFollowUpController(MentorFollowUpService service, JwtTokenProvider jwt) { this.service = service; this.jwt = jwt; }

    @GetMapping
    public ResponseEntity<MentorFollowUpResponseDTO> get(
            @RequestParam(required = false) String followUpType,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Integer studentYear,
            @RequestParam(required = false) String recordCategory,
            @RequestParam(required = false) Long minimumDaysPending,
            @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        if (!"MENTOR".equalsIgnoreCase(jwt.getRoleFromToken(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor access required");
        }
        return ResponseEntity.ok(service.get(jwt.getUserIdFromToken(token), followUpType, section, studentYear, recordCategory, minimumDaysPending));
    }

    private String token(String header) {
        if (header == null || !header.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        String token = header.substring(7).trim();
        if (!jwt.validateToken(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        return token;
    }
}
