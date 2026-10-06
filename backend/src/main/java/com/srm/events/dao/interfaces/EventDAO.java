package com.srm.events.dao.interfaces;

import com.srm.events.model.Event;
import com.srm.events.dto.EventCategoryDTO;
import java.util.List;
import java.util.Optional;

public interface EventDAO {
    Event create(Event event);
    Event update(Event event);
    boolean categoryExists(Long categoryId);
    List<EventCategoryDTO> findActiveCategories();
    Optional<Event> findById(Long id);
    List<Event> findAll(int limit, int offset);
    List<Event> findAvailable(int limit, int offset);
    List<Event> findByCategory(Long categoryId);
}
