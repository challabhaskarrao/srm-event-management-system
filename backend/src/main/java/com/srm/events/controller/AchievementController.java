package com.srm.events.controller;

import com.srm.events.dto.*;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.AchievementService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/applications/{applicationId}/achievement")
public class AchievementController {
    private final AchievementService service; private final JwtTokenProvider jwt;
    public AchievementController(AchievementService service,JwtTokenProvider jwt){this.service=service;this.jwt=jwt;}
    @PostMapping public ResponseEntity<AchievementResponseDTO> submit(@PathVariable Long applicationId,@Valid @RequestBody AchievementRequestDTO request,@RequestHeader(value="Authorization",required=false)String header){String token=token(header);return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(applicationId,request,jwt.getUserIdFromToken(token),jwt.getRoleFromToken(token)));}
    @GetMapping public ResponseEntity<AchievementResponseDTO> get(@PathVariable Long applicationId,@RequestHeader(value="Authorization",required=false)String header){String token=token(header);return service.get(applicationId,jwt.getUserIdFromToken(token),jwt.getRoleFromToken(token)).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.noContent().build());}
    @PutMapping public ResponseEntity<AchievementResponseDTO> review(@PathVariable Long applicationId,@Valid @RequestBody AchievementReviewDTO request,@RequestHeader(value="Authorization",required=false)String header){String token=token(header);return ResponseEntity.ok(service.review(applicationId,request,jwt.getUserIdFromToken(token),jwt.getRoleFromToken(token)));}
    private String token(String header){if(header==null||!header.startsWith("Bearer "))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Missing or invalid authorization token");String token=header.substring(7).trim();if(!jwt.validateToken(token))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid or expired token");return token;}
}
