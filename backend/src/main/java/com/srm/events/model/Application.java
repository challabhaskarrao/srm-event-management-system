package com.srm.events.model;

import java.time.LocalDateTime;

public class Application {

    private Long applicationId;
    private String applicationCode;
    private Long eventId;
    private Long createdByStudentId;
    private Long assignedMentorId;
    private Long academicYearId;
    private ParticipationType participationType;
    private boolean travelRequired;
    private boolean outsideState;
    private ApplicationStatus currentStatus;
    private String correctionReturnStage;
    private LocalDateTime submittedAt;
    private LocalDateTime mentorReviewedAt;
    private LocalDateTime hodReviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Application() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getCreatedByStudentId() {
        return createdByStudentId;
    }

    public void setCreatedByStudentId(Long createdByStudentId) {
        this.createdByStudentId = createdByStudentId;
    }

    public Long getAssignedMentorId() {
        return assignedMentorId;
    }

    public void setAssignedMentorId(Long assignedMentorId) {
        this.assignedMentorId = assignedMentorId;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }

    public void setParticipationType(ParticipationType participationType) {
        this.participationType = participationType;
    }

    public boolean isTravelRequired() {
        return travelRequired;
    }

    public void setTravelRequired(boolean travelRequired) {
        this.travelRequired = travelRequired;
    }

    public boolean isOutsideState() {
        return outsideState;
    }

    public void setOutsideState(boolean outsideState) {
        this.outsideState = outsideState;
    }

    public ApplicationStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ApplicationStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCorrectionReturnStage() {
        return correctionReturnStage;
    }

    public void setCorrectionReturnStage(String correctionReturnStage) {
        this.correctionReturnStage = correctionReturnStage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getMentorReviewedAt() {
        return mentorReviewedAt;
    }

    public void setMentorReviewedAt(LocalDateTime mentorReviewedAt) {
        this.mentorReviewedAt = mentorReviewedAt;
    }

    public LocalDateTime getHodReviewedAt() {
        return hodReviewedAt;
    }

    public void setHodReviewedAt(LocalDateTime hodReviewedAt) {
        this.hodReviewedAt = hodReviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
