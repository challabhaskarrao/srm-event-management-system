package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.dto.CreateEventRequestDTO;
import com.srm.events.dto.EventResponseDTO;
import com.srm.events.dto.EventCategoryDTO;
import com.srm.events.model.Event;
import com.srm.events.service.interfaces.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventDAO eventDAO;

    @Autowired
    public EventServiceImpl(EventDAO eventDAO) {
        this.eventDAO = eventDAO;
    }

    @Override
    public EventResponseDTO createEvent(CreateEventRequestDTO request, Long createdByUserId) {
        validateRequest(request);

        Event event = new Event();
        event.setEventName(request.getName().trim());
        event.setCategoryId(request.getCategoryId());
        event.setOrganizer(request.getOrganizer().trim());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setMode(request.getMode());
        event.setVenue(trimToNull(request.getVenue()));
        event.setEventUrl(trimToNull(request.getEventUrl()));
        event.setCreatedByUserId(createdByUserId);

        Event createdEvent = eventDAO.create(event);
        
        // Fetch the fully populated event with joined category details
        return getEventById(createdEvent.getEventId());
    }

    @Override
    public EventResponseDTO updateEvent(Long eventId, CreateEventRequestDTO request) {
        validateRequest(request);
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        event.setEventName(request.getName().trim());
        event.setCategoryId(request.getCategoryId());
        event.setOrganizer(request.getOrganizer().trim());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setMode(request.getMode());
        event.setVenue(trimToNull(request.getVenue()));
        event.setEventUrl(trimToNull(request.getEventUrl()));
        eventDAO.update(event);
        return getEventById(eventId);
    }

    private void validateRequest(CreateEventRequestDTO request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        if (!eventDAO.categoryExists(request.getCategoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event category is not active or does not exist");
        }
        String url = trimToNull(request.getEventUrl());
        if (url != null) {
            try {
                URI uri = new URI(url);
                if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null) {
                    throw new URISyntaxException(url, "Only absolute HTTP(S) URLs are allowed");
                }
            } catch (URISyntaxException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event URL must be a valid HTTP(S) URL");
            }
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public EventResponseDTO getEventById(Long eventId) {
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return new EventResponseDTO(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents(int limit, int offset) {
        List<Event> events = eventDAO.findAll(limit, offset);
        return events.stream().map(EventResponseDTO::new).collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getAvailableEvents(int limit, int offset) {
        return eventDAO.findAvailable(limit, offset).stream()
                .map(EventResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getEventsByCategory(Long categoryId) {
        List<Event> events = eventDAO.findByCategory(categoryId);
        return events.stream().map(EventResponseDTO::new).collect(Collectors.toList());
    }

    @Override
    public List<EventCategoryDTO> getActiveCategories() {
        return eventDAO.findActiveCategories();
    }
}
