package com.srm.events.controller;

import com.srm.events.dto.AchievementVerificationRequestDTO;
import com.srm.events.dto.StudentAchievementRequestDTO;
import com.srm.events.dto.StudentAchievementResponseDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.StudentAchievementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class StudentAchievementController {
    private final StudentAchievementService service;
    private final JwtTokenProvider jwt;
    public StudentAchievementController(StudentAchievementService service, JwtTokenProvider jwt) { this.service = service; this.jwt = jwt; }

    @PostMapping
    public ResponseEntity<StudentAchievementResponseDTO> create(@Valid @RequestBody StudentAchievementRequestDTO request,
                                                                 @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, jwt.getUserIdFromToken(token), jwt.getRoleFromToken(token)));
    }

    @PostMapping("/{achievementId}/submit")
    public StudentAchievementResponseDTO submit(@PathVariable Long achievementId,
                                                @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return service.submitForVerification(achievementId, jwt.getUserIdFromToken(token), jwt.getRoleFromToken(token));
    }

    @GetMapping("/my")
    public List<StudentAchievementResponseDTO> mine(@RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return service.listMine(jwt.getUserIdFromToken(token), jwt.getRoleFromToken(token));
    }

    @GetMapping("/review-queue")
    public List<StudentAchievementResponseDTO> queue(@RequestParam String role,
                                                     @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        String actualRole = jwt.getRoleFromToken(token);
        if (!role.equalsIgnoreCase(actualRole)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role mismatch");
        return service.reviewQueue(jwt.getUserIdFromToken(token), actualRole);
    }

    @GetMapping("/{achievementId}")
    public StudentAchievementResponseDTO get(@PathVariable Long achievementId,
                                             @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return service.get(achievementId, jwt.getUserIdFromToken(token), jwt.getRoleFromToken(token));
    }

    @PutMapping("/{achievementId}/verification")
    public StudentAchievementResponseDTO review(@PathVariable Long achievementId,
                                                @Valid @RequestBody AchievementVerificationRequestDTO request,
                                                @RequestHeader(value = "Authorization", required = false) String header) {
        String token = token(header);
        return service.review(achievementId, request, jwt.getUserIdFromToken(token), jwt.getRoleFromToken(token));
    }

    private String token(String header) {
        if (header == null || !header.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        String token = header.substring(7).trim();
        if (!jwt.validateToken(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        return token;
    }
}
