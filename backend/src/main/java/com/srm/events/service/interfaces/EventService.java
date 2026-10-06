package com.srm.events.service.interfaces;

import com.srm.events.dto.CreateEventRequestDTO;
import com.srm.events.dto.EventResponseDTO;
import com.srm.events.dto.EventCategoryDTO;
import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(CreateEventRequestDTO request, Long createdByUserId);
    EventResponseDTO updateEvent(Long eventId, CreateEventRequestDTO request);
    EventResponseDTO getEventById(Long eventId);
    List<EventResponseDTO> getAllEvents(int limit, int offset);
    List<EventResponseDTO> getAvailableEvents(int limit, int offset);
    List<EventResponseDTO> getEventsByCategory(Long categoryId);
    List<EventCategoryDTO> getActiveCategories();
}
