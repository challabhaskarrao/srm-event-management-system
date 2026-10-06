package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.*;
import com.srm.events.dto.AchievementVerificationRequestDTO;
import com.srm.events.dto.StudentAchievementRequestDTO;
import com.srm.events.dto.StudentAchievementResponseDTO;
import com.srm.events.dto.TeamMemberDTO;
import com.srm.events.model.EventTypeDefinition;
import com.srm.events.model.ParticipationType;
import com.srm.events.model.Student;
import com.srm.events.model.StudentAchievement;
import com.srm.events.service.interfaces.StudentAchievementService;
import com.srm.events.service.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class StudentAchievementServiceImpl implements StudentAchievementService {
    private final StudentAchievementDAO achievementDAO;
    private final StudentDAO studentDAO;
    private final FacultyDAO facultyDAO;
    private final ApplicationDAO applicationDAO;
    private final EventTypeDAO eventTypeDAO;
    private final NotificationService notificationService;
    private final NotificationRecipientDAO notificationRecipients;

    @Autowired
    public StudentAchievementServiceImpl(StudentAchievementDAO achievementDAO, StudentDAO studentDAO,
                                         FacultyDAO facultyDAO, ApplicationDAO applicationDAO, EventTypeDAO eventTypeDAO,
                                         NotificationService notificationService, NotificationRecipientDAO notificationRecipients) {
        this.achievementDAO = achievementDAO;
        this.studentDAO = studentDAO;
        this.facultyDAO = facultyDAO;
        this.applicationDAO = applicationDAO;
        this.eventTypeDAO = eventTypeDAO;
        this.notificationService = notificationService;
        this.notificationRecipients = notificationRecipients;
    }

    public StudentAchievementServiceImpl(StudentAchievementDAO achievementDAO, StudentDAO studentDAO,
                                         FacultyDAO facultyDAO, ApplicationDAO applicationDAO, EventTypeDAO eventTypeDAO) {
        this(achievementDAO, studentDAO, facultyDAO, applicationDAO, eventTypeDAO, null, null);
    }

    @Override public StudentAchievementResponseDTO create(StudentAchievementRequestDTO request, Long userId, String role) {
        requireRole(role, "STUDENT");
        Student student = studentDAO.findByUserId(userId).orElseThrow(() -> forbidden("Student profile required"));
        EventTypeDefinition type = eventTypeDAO.findActiveByCode(normalize(request.getEventTypeCode()))
                .orElseThrow(() -> badRequest("Achievement type is not active"));
        if (!"ACHIEVEMENT".equalsIgnoreCase(type.workflowCategory())) throw badRequest("Selected type belongs to the activity workflow");
        if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw badRequest("End date cannot be before start date");
        }
        Map<String, String> details = normalizeDetails(request.getTypeSpecificDetails());
        validateTypeSpecificFields(type, details);
        ParticipationType participationType = request.getParticipationType() == null ? ParticipationType.INDIVIDUAL : request.getParticipationType();
        if (participationType == ParticipationType.TEAM && !booleanConfig(type, "team")) throw badRequest("This achievement type does not support teams");
        if (participationType == ParticipationType.INDIVIDUAL && ((request.getTeamName() != null && !request.getTeamName().isBlank()) || hasValues(request.getTeamMemberRegistrationNumbers()))) {
            throw badRequest("Individual achievements cannot contain team information");
        }

        List<Long> memberIds = new ArrayList<>();
        if (participationType == ParticipationType.TEAM) {
            if (request.getTeamName() == null || request.getTeamName().isBlank()) throw badRequest("Team name is required");
            List<String> registrations = request.getTeamMemberRegistrationNumbers() == null ? List.of() : request.getTeamMemberRegistrationNumbers();
            if (registrations.isEmpty() || registrations.size() > 9) throw badRequest("A team must contain 2 to 10 students");
            Set<String> seen = new HashSet<>();
            seen.add(student.getRegisterNumber().trim().toUpperCase(Locale.ROOT));
            memberIds.add(student.getStudentId());
            for (String raw : registrations) {
                String registration = raw == null ? "" : raw.trim();
                if (registration.isBlank() || !seen.add(registration.toUpperCase(Locale.ROOT))) throw badRequest("Team member registration numbers must be unique and valid");
                Student member = studentDAO.findByRegistrationNumber(registration).orElseThrow(() -> badRequest("Team member is not an existing Student: " + registration));
                memberIds.add(member.getStudentId());
            }
        }

        List<Long> mentorIds = applicationDAO.findActiveMentorIdsForStudent(student.getStudentId());
        if (mentorIds.size() != 1) throw new ResponseStatusException(HttpStatus.CONFLICT,
                mentorIds.isEmpty() ? "No active Mentor assignment exists for this Student" : "Multiple active Mentor assignments exist for this Student");

        StudentAchievement value = new StudentAchievement();
        value.setStudentId(student.getStudentId());
        value.setEventTypeId(type.eventTypeId());
        value.setEventTypeCode(type.code());
        value.setEventTypeName(type.name());
        value.setWorkflowCategory(type.workflowCategory());
        value.setAcademicYearId(student.getAcademicYearId());
        value.setAchievementSemester(student.getSemester());
        value.setAchievementStudentYear(student.getStudentYear());
        value.setAssignedMentorId(mentorIds.getFirst());
        value.setTitle(trimRequired(request.getTitle(), "Title"));
        value.setOrganization(trimRequired(request.getOrganization(), "Organization / institution"));
        value.setDescription(trim(request.getDescription()));
        value.setStartDate(request.getStartDate());
        value.setEndDate(request.getEndDate());
        value.setOfficialUrl(trim(request.getOfficialUrl()));
        value.setParticipationType(participationType);
        value.setTypeSpecificDetails(details);
        String teamName = participationType == ParticipationType.TEAM ? trimRequired(request.getTeamName(), "Team name") : null;
        StudentAchievementResponseDTO created = StudentAchievementResponseDTO.from(achievementDAO.create(value, teamName, memberIds),
                participationType == ParticipationType.TEAM ? achievementDAO.findTeamMembers(value.getAchievementId()) : List.of());
        return created;
    }

    @Override public StudentAchievementResponseDTO submitForVerification(Long id, Long userId, String role) {
        requireRole(role, "STUDENT");
        StudentAchievement value = find(id);
        Student student = studentDAO.findByUserId(userId).orElseThrow(() -> forbidden("Student profile required"));
        if (!value.getStudentId().equals(student.getStudentId())) throw forbidden("Achievement access denied");
        if (!"PENDING_MENTOR_VERIFICATION".equals(value.getVerificationStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Achievement is not awaiting Mentor verification");
        }
        notifySubmission(value);
        return get(id, userId, role);
    }

    @Override public List<StudentAchievementResponseDTO> listMine(Long userId, String role) {
        requireRole(role, "STUDENT");
        Student student = studentDAO.findByUserId(userId).orElseThrow(() -> forbidden("Student profile required"));
        return achievementDAO.findByStudentId(student.getStudentId()).stream().map(value -> StudentAchievementResponseDTO.from(value, List.of())).toList();
    }

    @Override public StudentAchievementResponseDTO get(Long id, Long userId, String role) {
        StudentAchievement value = find(id);
        authorize(value, userId, role);
        return StudentAchievementResponseDTO.from(value, achievementDAO.findTeamMembers(id));
    }

    @Override public List<StudentAchievementResponseDTO> reviewQueue(Long userId, String role) {
        if (!"MENTOR".equalsIgnoreCase(role) && !"HOD".equalsIgnoreCase(role)) throw forbidden("Mentor or HOD access required");
        List<StudentAchievement> values;
        if ("MENTOR".equalsIgnoreCase(role)) {
            Long facultyId = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> forbidden("Active faculty profile required")).getFacultyId();
            values = achievementDAO.findMentorQueue(facultyId);
        } else {
            Long departmentId = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> forbidden("Active faculty profile required")).getDepartmentId();
            values = achievementDAO.findHodQueue(departmentId);
        }
        return values.stream().map(value -> StudentAchievementResponseDTO.from(value, List.of())).toList();
    }

    @Override public StudentAchievementResponseDTO review(Long id, AchievementVerificationRequestDTO request, Long userId, String role) {
        StudentAchievement value = find(id);
        String action = normalize(request.getStatus());
        String remarks = trim(request.getRemarks());
        if (!Set.of("APPROVE", "REJECT").contains(action)) throw badRequest("Verification status must be APPROVE or REJECT");
        if ("REJECT".equals(action) && (remarks == null || remarks.isBlank())) throw badRequest("Remarks are required when rejecting an achievement");
        String next = "REJECT".equals(action) ? "REJECTED" : null;
        boolean updated;
        if ("MENTOR".equalsIgnoreCase(role)) {
            Long facultyId = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> forbidden("Active faculty profile required")).getFacultyId();
            if (!achievementDAO.isMentorAssigned(id, facultyId)) throw forbidden("Achievement is outside your Mentor assignment");
            next = next == null ? "PENDING_HOD_VERIFICATION" : next;
            updated = achievementDAO.updateMentorReview(id, "PENDING_MENTOR_VERIFICATION", next, remarks, userId);
        } else if ("HOD".equalsIgnoreCase(role)) {
            Long departmentId = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> forbidden("Active faculty profile required")).getDepartmentId();
            if (!achievementDAO.isInDepartment(id, departmentId)) throw forbidden("Achievement is outside your department");
            next = next == null ? "VERIFIED" : next;
            updated = achievementDAO.updateHodReview(id, "PENDING_HOD_VERIFICATION", next, remarks, userId);
        } else {
            throw forbidden("Only Mentor and HOD users can verify achievements");
        }
        if (!updated) throw new ResponseStatusException(HttpStatus.CONFLICT, "Achievement status changed; refresh the queue");
        notifyReview(value, next, role);
        return get(id, userId, role);
    }

    private void notifyReview(StudentAchievement value, String next, String role) {
        if (notificationService == null || notificationRecipients == null) return;
        Student student = studentDAO.findById(value.getStudentId()).orElse(null); if (student == null) return;
        String studentPath="/student/achievements/"+value.getAchievementId();
        if ("MENTOR".equalsIgnoreCase(role) && "PENDING_HOD_VERIFICATION".equals(next)) notificationRecipients.findHodUserIdByDepartmentId(student.getDepartmentId()).ifPresent(userId -> safeNotify(userId,"ACHIEVEMENT_RECOMMENDED_TO_HOD","Achievement awaiting HOD verification","An achievement record is awaiting HOD verification.","ACHIEVEMENT",value.getAchievementId(),"/staff/hod","ACHIEVEMENT_RECOMMENDED_TO_HOD:"+value.getAchievementId()));
        else if ("REJECTED".equals(next)) safeNotify(student.getUserId(),"ACHIEVEMENT_REJECTED","Achievement record rejected","Your achievement record was rejected; review the remarks and resubmit if needed.","ACHIEVEMENT",value.getAchievementId(),studentPath,"ACHIEVEMENT_REJECTED:"+value.getAchievementId()+":"+role);
        else if ("VERIFIED".equals(next)) safeNotify(student.getUserId(),"ACHIEVEMENT_VERIFIED","Achievement record verified","Your achievement record has been verified.","ACHIEVEMENT",value.getAchievementId(),studentPath,"ACHIEVEMENT_VERIFIED:"+value.getAchievementId());
    }
    private void notifySubmission(StudentAchievement value) {
        if (notificationService == null || notificationRecipients == null) return;
        notificationRecipients.findUserIdByFacultyId(value.getAssignedMentorId()).ifPresent(recipientUserId ->
                safeNotify(recipientUserId, "ACHIEVEMENT_SUBMITTED", "New achievement verification request",
                        "New achievement record requires your verification.", "ACHIEVEMENT", value.getAchievementId(),
                        "/staff/mentor", "ACHIEVEMENT_SUBMITTED:" + value.getAchievementId()));
    }
    private void safeNotify(Long userId,String type,String title,String message,String recordType,Long recordId,String path,String key){try{notificationService.notifyOnce(userId,type,title,message,recordType,recordId,path,key);}catch(RuntimeException ignored){}}

    private void authorize(StudentAchievement value, Long userId, String role) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            Student student = studentDAO.findByUserId(userId).orElseThrow(() -> forbidden("Student profile required"));
            if (value.getStudentId().equals(student.getStudentId())) return;
        } else if ("MENTOR".equalsIgnoreCase(role)) {
            Long facultyId = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> forbidden("Active faculty profile required")).getFacultyId();
            if (achievementDAO.isMentorAssigned(value.getAchievementId(), facultyId)) return;
        } else if ("HOD".equalsIgnoreCase(role)) {
            Long departmentId = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> forbidden("Active faculty profile required")).getDepartmentId();
            if (achievementDAO.isInDepartment(value.getAchievementId(), departmentId)) return;
        }
        throw forbidden("Achievement access denied");
    }

    private StudentAchievement find(Long id) { return achievementDAO.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Achievement not found")); }
    private void requireRole(String actual, String expected) { if (!expected.equalsIgnoreCase(actual)) throw forbidden(expected + " access required"); }
    private ResponseStatusException forbidden(String message) { return new ResponseStatusException(HttpStatus.FORBIDDEN, message); }
    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private boolean booleanConfig(EventTypeDefinition type, String key) { return Boolean.TRUE.equals(type.config().get(key)); }
    private boolean hasValues(List<String> values) { return values != null && values.stream().anyMatch(value -> value != null && !value.isBlank()); }
    private Map<String, String> normalizeDetails(Map<String, String> values) {
        if (values == null) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        values.forEach((key, value) -> { if (key != null && value != null && !value.isBlank()) result.put(key.trim(), value.trim()); });
        if (result.size() > 30) throw badRequest("Too many type-specific fields");
        return result;
    }
    private void validateTypeSpecificFields(EventTypeDefinition type, Map<String, String> details) {
        String kind = String.valueOf(type.config().getOrDefault("kind", ""));
        if ("PATENT".equals(kind)) requireDetail(details, "patentTitle");
        if ("PUBLICATION".equals(kind)) { requireDetail(details, "paperTitle"); requireDetail(details, "conferenceName"); requireDetail(details, "publicationScope"); }
        if ("COURSE".equals(kind)) { requireDetail(details, "provider"); requireDetail(details, "durationHours"); }
        if ("INTERNSHIP".equals(kind)) { requireDetail(details, "organizationName"); requireDetail(details, "internshipRole"); if ("OFFLINE".equals(type.config().get("mode"))) requireDetail(details, "location"); }
        if ("DUAL_DEGREE".equals(kind)) { requireDetail(details, "university"); requireDetail(details, "program"); }
        if ("STARTUP".equals(kind)) requireDetail(details, "ventureName");
    }
    private void requireDetail(Map<String, String> details, String key) { if (!details.containsKey(key) || details.get(key).isBlank()) throw badRequest("Missing type-specific field: " + key); }
    private String normalize(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT); }
    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String trimRequired(String value, String label) { String result = trim(value); if (result == null) throw badRequest(label + " is required"); return result; }
}
