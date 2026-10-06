package com.srm.events.controller;

import com.srm.events.dto.EventTypeDTO;
import com.srm.events.dao.interfaces.EventTypeDAO;
import com.srm.events.model.EventTypeDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event-types")
public class EventTypeController {
    private final EventTypeDAO eventTypeDAO;

    public EventTypeController(EventTypeDAO eventTypeDAO) {
        this.eventTypeDAO = eventTypeDAO;
    }

    @GetMapping
    public ResponseEntity<List<EventTypeDTO>> getActiveEventTypes() {
        List<EventTypeDTO> types = eventTypeDAO.findActive().stream().map(this::toDto).toList();
        return ResponseEntity.ok(types);
    }

    private EventTypeDTO toDto(EventTypeDefinition type) {
        return new EventTypeDTO(type.code(), type.name(), type.sortOrder(), type.workflowCategory(), type.config());
    }
}
