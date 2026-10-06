package com.srm.events.service.interfaces;

import com.srm.events.dto.AchievementVerificationRequestDTO;
import com.srm.events.dto.StudentAchievementRequestDTO;
import com.srm.events.dto.StudentAchievementResponseDTO;

import java.util.List;

public interface StudentAchievementService {
    StudentAchievementResponseDTO create(StudentAchievementRequestDTO request, Long userId, String role);
    StudentAchievementResponseDTO submitForVerification(Long achievementId, Long userId, String role);
    List<StudentAchievementResponseDTO> listMine(Long userId, String role);
    StudentAchievementResponseDTO get(Long achievementId, Long userId, String role);
    List<StudentAchievementResponseDTO> reviewQueue(Long userId, String role);
    StudentAchievementResponseDTO review(Long achievementId, AchievementVerificationRequestDTO request, Long userId, String role);
}
