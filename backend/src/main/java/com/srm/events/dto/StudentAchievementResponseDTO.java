package com.srm.events.dto;

import com.srm.events.model.ParticipationType;
import com.srm.events.model.StudentAchievement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record StudentAchievementResponseDTO(
        Long achievementId, Long studentId, String studentName, String registrationNumber,
        String departmentName, String sectionName, Integer studentYear,
        String eventTypeCode, String eventTypeName, String workflowCategory,
        String title, String organization, String description, LocalDate startDate,
        LocalDate endDate, String officialUrl, ParticipationType participationType,
        Map<String, String> typeSpecificDetails, String verificationStatus,
        String mentorRemarks, String hodRemarks, LocalDateTime submittedAt,
        LocalDateTime mentorReviewedAt, LocalDateTime hodReviewedAt,
        LocalDateTime createdAt, LocalDateTime updatedAt, List<TeamMemberDTO> teamMembers) {
    public static StudentAchievementResponseDTO from(StudentAchievement value, List<TeamMemberDTO> members) {
        return new StudentAchievementResponseDTO(value.getAchievementId(), value.getStudentId(), value.getStudentName(),
                value.getRegistrationNumber(), value.getDepartmentName(), value.getSectionName(), value.getStudentYear(),
                value.getEventTypeCode(), value.getEventTypeName(), value.getWorkflowCategory(), value.getTitle(),
                value.getOrganization(), value.getDescription(), value.getStartDate(), value.getEndDate(),
                value.getOfficialUrl(), value.getParticipationType(), value.getTypeSpecificDetails(),
                value.getVerificationStatus(), value.getMentorRemarks(), value.getHodRemarks(), value.getSubmittedAt(),
                value.getMentorReviewedAt(), value.getHodReviewedAt(), value.getCreatedAt(), value.getUpdatedAt(),
                members == null ? List.of() : members);
    }
}
