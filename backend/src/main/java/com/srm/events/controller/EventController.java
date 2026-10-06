package com.srm.events.controller;

import com.srm.events.dto.CreateEventRequestDTO;
import com.srm.events.dto.EventResponseDTO;
import com.srm.events.dto.EventCategoryDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public EventController(EventService eventService, JwtTokenProvider jwtTokenProvider) {
        this.eventService = eventService;
        this.jwtTokenProvider = jwtTokenProvider;
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

    private Long requireAdmin(String authHeader) {
        String token = extractToken(authHeader);
        if (!"ADMIN".equalsIgnoreCase(jwtTokenProvider.getRoleFromToken(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required to manage events");
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateEventRequestDTO request) {
        Long userId = requireAdmin(authHeader);
        EventResponseDTO createdEvent = eventService.createEvent(request, userId);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateEventRequestDTO request) {
        requireAdmin(authHeader);
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(eventService.getAvailableEvents(limit, offset));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<EventCategoryDTO>> getCategories() {
        return ResponseEntity.ok(eventService.getActiveCategories());
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) Long categoryId) {
        
        List<EventResponseDTO> events;
        if (categoryId != null) {
            events = eventService.getEventsByCategory(categoryId);
        } else {
            events = eventService.getAllEvents(limit, offset);
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}
