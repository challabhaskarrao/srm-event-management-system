package com.srm.events.controller;

import com.srm.events.dto.CreateEventRequestDTO;
import com.srm.events.dto.EventResponseDTO;
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
@CrossOrigin
public class EventController {

    private final EventService eventService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public EventController(EventService eventService, JwtTokenProvider jwtTokenProvider) {
        this.eventService = eventService;
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

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateEventRequestDTO request) {
        Long userId = getUserIdFromHeader(authHeader);
        EventResponseDTO createdEvent = eventService.createEvent(request, userId);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
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
