package com.srm.events.dao.interfaces;

import com.srm.events.model.Faculty;

import java.util.Optional;

public interface FacultyDAO {
    Optional<Faculty> findActiveByUserId(Long userId);
}
