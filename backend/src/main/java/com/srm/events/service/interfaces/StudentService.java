package com.srm.events.service.interfaces;

import com.srm.events.dto.StudentProfileDTO;

public interface StudentService {
    StudentProfileDTO getMyProfile(Long userId);
}
