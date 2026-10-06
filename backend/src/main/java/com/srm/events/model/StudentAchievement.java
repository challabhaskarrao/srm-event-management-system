package com.srm.events.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class StudentAchievement {
    private Long achievementId;
    private Long studentId;
    private Long eventTypeId;
    private String eventTypeCode;
    private String eventTypeName;
    private String workflowCategory;
    private Long academicYearId;
    private Integer achievementSemester;
    private Integer achievementStudentYear;
    private Long assignedMentorId;
    private String title;
    private String organization;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String officialUrl;
    private ParticipationType participationType;
    private Map<String, String> typeSpecificDetails;
    private String verificationStatus;
    private String mentorRemarks;
    private String hodRemarks;
    private LocalDateTime submittedAt;
    private LocalDateTime mentorReviewedAt;
    private LocalDateTime hodReviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String studentName;
    private String registrationNumber;
    private String departmentName;
    private String sectionName;
    private Integer studentYear;

    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long value) { achievementId = value; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long value) { studentId = value; }
    public Long getEventTypeId() { return eventTypeId; }
    public void setEventTypeId(Long value) { eventTypeId = value; }
    public String getEventTypeCode() { return eventTypeCode; }
    public void setEventTypeCode(String value) { eventTypeCode = value; }
    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String value) { eventTypeName = value; }
    public String getWorkflowCategory() { return workflowCategory; }
    public void setWorkflowCategory(String value) { workflowCategory = value; }
    public Long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Long value) { academicYearId = value; }
    public Integer getAchievementSemester() { return achievementSemester; }
    public void setAchievementSemester(Integer value) { achievementSemester = value; }
    public Integer getAchievementStudentYear() { return achievementStudentYear; }
    public void setAchievementStudentYear(Integer value) { achievementStudentYear = value; }
    public Long getAssignedMentorId() { return assignedMentorId; }
    public void setAssignedMentorId(Long value) { assignedMentorId = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getOrganization() { return organization; }
    public void setOrganization(String value) { organization = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate value) { startDate = value; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate value) { endDate = value; }
    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String value) { officialUrl = value; }
    public ParticipationType getParticipationType() { return participationType; }
    public void setParticipationType(ParticipationType value) { participationType = value; }
    public Map<String, String> getTypeSpecificDetails() { return typeSpecificDetails; }
    public void setTypeSpecificDetails(Map<String, String> value) { typeSpecificDetails = value; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String value) { verificationStatus = value; }
    public String getMentorRemarks() { return mentorRemarks; }
    public void setMentorRemarks(String value) { mentorRemarks = value; }
    public String getHodRemarks() { return hodRemarks; }
    public void setHodRemarks(String value) { hodRemarks = value; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime value) { submittedAt = value; }
    public LocalDateTime getMentorReviewedAt() { return mentorReviewedAt; }
    public void setMentorReviewedAt(LocalDateTime value) { mentorReviewedAt = value; }
    public LocalDateTime getHodReviewedAt() { return hodReviewedAt; }
    public void setHodReviewedAt(LocalDateTime value) { hodReviewedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String value) { studentName = value; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String value) { registrationNumber = value; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String value) { departmentName = value; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String value) { sectionName = value; }
    public Integer getStudentYear() { return studentYear; }
    public void setStudentYear(Integer value) { studentYear = value; }
}
