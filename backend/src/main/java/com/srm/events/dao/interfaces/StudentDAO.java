package com.srm.events.dao.interfaces;

import com.srm.events.model.Student;
import com.srm.events.dto.StudentProfileDTO;
import java.util.Optional;

public interface StudentDAO {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findById(Long studentId);
    Optional<Student> findByRegistrationNumber(String registrationNumber);
    Optional<StudentProfileDTO> findProfileByUserId(Long userId);
}
