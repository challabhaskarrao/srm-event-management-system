package com.srm.events.dao.interfaces;

import com.srm.events.dto.TeamMemberDTO;
import com.srm.events.model.StudentAchievement;

import java.util.List;
import java.util.Optional;

public interface StudentAchievementDAO {
    StudentAchievement create(StudentAchievement achievement, String teamName, List<Long> teamMemberIds);
    Optional<StudentAchievement> findById(Long achievementId);
    List<StudentAchievement> findByStudentId(Long studentId);
    List<StudentAchievement> findMentorQueue(Long facultyId);
    List<StudentAchievement> findHodQueue(Long departmentId);
    List<TeamMemberDTO> findTeamMembers(Long achievementId);
    boolean isMentorAssigned(Long achievementId, Long facultyId);
    boolean isInDepartment(Long achievementId, Long departmentId);
    boolean updateMentorReview(Long achievementId, String expectedStatus, String newStatus, String remarks, Long reviewerUserId);
    boolean updateHodReview(Long achievementId, String expectedStatus, String newStatus, String remarks, Long reviewerUserId);
}
