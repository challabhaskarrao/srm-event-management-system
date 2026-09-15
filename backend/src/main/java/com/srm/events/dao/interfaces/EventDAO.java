package com.srm.events.dao.interfaces;

import com.srm.events.model.Event;
import java.util.List;
import java.util.Optional;

public interface EventDAO {
    Event create(Event event);
    Optional<Event> findById(Long id);
    List<Event> findAll(int limit, int offset);
    List<Event> findByCategory(Long categoryId);
}
