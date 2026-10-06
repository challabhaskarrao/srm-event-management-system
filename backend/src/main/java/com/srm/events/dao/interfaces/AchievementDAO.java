package com.srm.events.dao.interfaces;

import com.srm.events.model.Achievement;
import java.util.Optional;

public interface AchievementDAO {
    Achievement create(Achievement achievement);
    Optional<Achievement> findByApplicationId(Long applicationId);
    Achievement review(Long achievementId, String status, String remarks, Long reviewerUserId);
}
