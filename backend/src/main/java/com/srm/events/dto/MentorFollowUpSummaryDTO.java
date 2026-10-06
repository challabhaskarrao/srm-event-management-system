package com.srm.events.dto;

public class MentorFollowUpSummaryDTO {
    private long needsMentorReview;
    private long studentCorrectionPending;
    private long postEventPending;
    private long evidenceIncomplete;
    private long achievementVerificationPending;
    private long totalAttentionRequired;

    public long getNeedsMentorReview() { return needsMentorReview; }
    public void setNeedsMentorReview(long value) { needsMentorReview = value; }
    public long getStudentCorrectionPending() { return studentCorrectionPending; }
    public void setStudentCorrectionPending(long value) { studentCorrectionPending = value; }
    public long getPostEventPending() { return postEventPending; }
    public void setPostEventPending(long value) { postEventPending = value; }
    public long getEvidenceIncomplete() { return evidenceIncomplete; }
    public void setEvidenceIncomplete(long value) { evidenceIncomplete = value; }
    public long getAchievementVerificationPending() { return achievementVerificationPending; }
    public void setAchievementVerificationPending(long value) { achievementVerificationPending = value; }
    public long getTotalAttentionRequired() { return totalAttentionRequired; }
    public void setTotalAttentionRequired(long value) { totalAttentionRequired = value; }
}
