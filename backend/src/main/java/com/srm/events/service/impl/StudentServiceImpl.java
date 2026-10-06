package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dto.StudentProfileDTO;
import com.srm.events.service.interfaces.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentDAO studentDAO;

    public StudentServiceImpl(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    @Override
    public StudentProfileDTO getMyProfile(Long userId) {
        return studentDAO.findProfileByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student profile not found for authenticated user"));
    }
}
