package com.srm.events.dao.interfaces;

import com.srm.events.model.Student;
import java.util.Optional;

public interface StudentDAO {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findById(Long studentId);
}
