package com.srm.events.dto;

import com.srm.events.model.Achievement;
import java.time.LocalDateTime;

public class AchievementResponseDTO {
    private Long achievementId;
    private Long applicationId;
    private String resultType;
    private String position;
    private String prizeDetails;
    private String verificationStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String remarks;
    public AchievementResponseDTO() {}
    public AchievementResponseDTO(Achievement value) {
        achievementId=value.getAchievementId(); applicationId=value.getApplicationId(); resultType=value.getResultType();
        position=value.getPosition(); prizeDetails=value.getPrizeDetails(); verificationStatus=value.getVerificationStatus();
        submittedAt=value.getSubmittedAt(); verifiedAt=value.getVerifiedAt(); remarks=value.getRemarks();
    }
    public Long getAchievementId() { return achievementId; }
    public Long getApplicationId() { return applicationId; }
    public String getResultType() { return resultType; }
    public String getPosition() { return position; }
    public String getPrizeDetails() { return prizeDetails; }
    public String getVerificationStatus() { return verificationStatus; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public String getRemarks() { return remarks; }
}
