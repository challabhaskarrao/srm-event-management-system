package com.srm.events.service.interfaces;

import com.srm.events.dto.*;
import java.util.Optional;

public interface AchievementService {
    AchievementResponseDTO submit(Long applicationId, AchievementRequestDTO request, Long userId, String role);
    Optional<AchievementResponseDTO> get(Long applicationId, Long userId, String role);
    AchievementResponseDTO review(Long applicationId, AchievementReviewDTO review, Long userId, String role);
}
