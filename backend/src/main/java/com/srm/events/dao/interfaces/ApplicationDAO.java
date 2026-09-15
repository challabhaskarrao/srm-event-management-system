package com.srm.events.dao.interfaces;

import com.srm.events.model.Application;
import java.util.List;
import java.util.Optional;

public interface ApplicationDAO {
    Application create(Application app, String teamName);
    Optional<Application> findByCode(String code);
    List<Application> findByStudentId(Long studentId);
    boolean existsByStudentAndEvent(Long studentId, Long eventId);
}
