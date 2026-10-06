package com.srm.events.controller;

import com.srm.events.dto.NotificationResponseDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service; private final JwtTokenProvider jwt;
    public NotificationController(NotificationService service, JwtTokenProvider jwt) { this.service = service; this.jwt = jwt; }
    @GetMapping public List<NotificationResponseDTO> list(@RequestParam(defaultValue="30") int limit, @RequestHeader(value="Authorization", required=false) String header) { return service.getForUser(user(header), limit); }
    @GetMapping("/unread-count") public Map<String, Long> unread(@RequestHeader(value="Authorization", required=false) String header) { return Map.of("count", service.unreadCount(user(header))); }
    @PatchMapping("/{id}/read") public ResponseEntity<Void> read(@PathVariable Long id, @RequestHeader(value="Authorization", required=false) String header) { service.markRead(user(header), id); return ResponseEntity.noContent().build(); }
    @PostMapping("/read-all") public Map<String, Integer> readAll(@RequestHeader(value="Authorization", required=false) String header) { return Map.of("markedRead", service.markAllRead(user(header))); }
    private Long user(String header) { if (header == null || !header.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); String token=header.substring(7).trim(); if(!jwt.validateToken(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid or expired token"); return jwt.getUserIdFromToken(token); }
}
