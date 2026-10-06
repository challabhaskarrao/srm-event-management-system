package com.srm.events.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MentorFollowUpItemDTO {
    private String followUpType;
    private String recordType;
    private Long studentId;
    private String studentName;
    private String registrationNumber;
    private String department;
    private String section;
    private Integer studentYear;
    private Integer semester;
    private Long applicationId;
    private Long achievementId;
    private String applicationCode;
    private String title;
    private String recordCategory;
    private LocalDate relevantDate;
    private Long daysPending;
    private String currentStatus;
    private LocalDateTime lastActionAt;
    private String remarks;

    public String getFollowUpType() { return followUpType; }
    public void setFollowUpType(String value) { followUpType = value; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String value) { recordType = value; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long value) { studentId = value; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String value) { studentName = value; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String value) { registrationNumber = value; }
    public String getDepartment() { return department; }
    public void setDepartment(String value) { department = value; }
    public String getSection() { return section; }
    public void setSection(String value) { section = value; }
    public Integer getStudentYear() { return studentYear; }
    public void setStudentYear(Integer value) { studentYear = value; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer value) { semester = value; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long value) { applicationId = value; }
    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long value) { achievementId = value; }
    public String getApplicationCode() { return applicationCode; }
    public void setApplicationCode(String value) { applicationCode = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getRecordCategory() { return recordCategory; }
    public void setRecordCategory(String value) { recordCategory = value; }
    public LocalDate getRelevantDate() { return relevantDate; }
    public void setRelevantDate(LocalDate value) { relevantDate = value; }
    public Long getDaysPending() { return daysPending; }
    public void setDaysPending(Long value) { daysPending = value; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String value) { currentStatus = value; }
    public LocalDateTime getLastActionAt() { return lastActionAt; }
    public void setLastActionAt(LocalDateTime value) { lastActionAt = value; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String value) { remarks = value; }
}
