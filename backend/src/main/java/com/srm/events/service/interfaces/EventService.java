package com.srm.events.service.interfaces;

import com.srm.events.dto.CreateEventRequestDTO;
import com.srm.events.dto.EventResponseDTO;
import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(CreateEventRequestDTO request, Long createdByUserId);
    EventResponseDTO getEventById(Long eventId);
    List<EventResponseDTO> getAllEvents(int limit, int offset);
    List<EventResponseDTO> getEventsByCategory(Long categoryId);
}
