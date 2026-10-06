package com.srm.events.dao.interfaces;

import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.dto.TeamMemberDTO;
import com.srm.events.dto.ApprovalActionDTO;
import java.util.List;
import java.util.Optional;

public interface ApplicationDAO {
    Application create(Application app, String teamName, List<Long> teamMemberIds);
    Optional<String> findTeamNameByApplicationId(Long applicationId);
    List<TeamMemberDTO> findTeamMembersByApplicationId(Long applicationId);
    Optional<Application> findByCode(String code);
    List<Application> findByStudentId(Long studentId);
    boolean existsByStudentAndEvent(Long studentId, Long eventId);
    List<Long> findActiveMentorIdsForStudent(Long studentId);
    List<Application> findAll();
    Optional<Application> findById(Long id);
    List<Application> findMentorReviewQueue(Long facultyId);
    List<Application> findHodReviewQueue(Long departmentId);
    boolean isMentorAssignedToApplication(Long applicationId, Long facultyId);
    boolean isApplicationInDepartment(Long applicationId, Long departmentId);
    boolean updateMentorReview(Long applicationId, ApplicationStatus expectedStatus,
                               ApplicationStatus newStatus, String action, String remarks,
                               Long facultyId, Long approverUserId);
    boolean updateHodReview(Long applicationId, ApplicationStatus expectedStatus,
                            ApplicationStatus newStatus, String action, String remarks,
                            Long facultyId, Long approverUserId);
    List<ApprovalActionDTO> findApprovalHistoryByApplicationId(Long applicationId);
    List<Application> findPostEventPendingForMentor(Long facultyId);
    List<Application> findPostEventPendingForHod(Long departmentId);
}
