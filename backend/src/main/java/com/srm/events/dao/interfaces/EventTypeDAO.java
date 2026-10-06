package com.srm.events.dao.interfaces;

import com.srm.events.model.EventTypeDefinition;
import java.util.List;
import java.util.Optional;

public interface EventTypeDAO {
    List<EventTypeDefinition> findActive();
    Optional<EventTypeDefinition> findActiveByCode(String code);
}
