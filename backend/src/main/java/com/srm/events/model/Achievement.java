package com.srm.events.model;

import java.time.LocalDateTime;

public class Achievement {
    private Long achievementId;
    private Long applicationId;
    private String resultType;
    private String position;
    private String prizeDetails;
    private String verificationStatus;
    private LocalDateTime submittedAt;
    private Long verifiedByUserId;
    private LocalDateTime verifiedAt;
    private String remarks;

    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPrizeDetails() { return prizeDetails; }
    public void setPrizeDetails(String prizeDetails) { this.prizeDetails = prizeDetails; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Long getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(Long verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
