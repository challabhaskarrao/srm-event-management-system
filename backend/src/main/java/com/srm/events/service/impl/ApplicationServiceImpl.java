package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.ApplicationDAO;
import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dao.interfaces.AchievementDAO;
import com.srm.events.dao.interfaces.NotificationRecipientDAO;
import com.srm.events.dto.ApplicationResponseDTO;
import com.srm.events.dto.RegisterApplicationRequestDTO;
import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.Event;
import com.srm.events.model.Faculty;
import com.srm.events.model.ParticipationType;
import com.srm.events.model.Student;
import com.srm.events.service.interfaces.ApplicationService;
import com.srm.events.service.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final StudentDAO studentDAO;
    private final EventDAO eventDAO;
    private final FacultyDAO facultyDAO;
    private final AchievementDAO achievementDAO;
    private final NotificationService notificationService;
    private final NotificationRecipientDAO notificationRecipients;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, StudentDAO studentDAO,
                                  EventDAO eventDAO, FacultyDAO facultyDAO, AchievementDAO achievementDAO,
                                  NotificationService notificationService, NotificationRecipientDAO notificationRecipients) {
        this.applicationDAO = applicationDAO;
        this.studentDAO = studentDAO;
        this.eventDAO = eventDAO;
        this.facultyDAO = facultyDAO;
        this.achievementDAO = achievementDAO;
        this.notificationService = notificationService;
        this.notificationRecipients = notificationRecipients;
    }

    /** Compatibility constructor retained for focused unit tests that do not exercise notifications. */
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, StudentDAO studentDAO,
                                  EventDAO eventDAO, FacultyDAO facultyDAO, AchievementDAO achievementDAO) {
        this(applicationDAO, studentDAO, eventDAO, facultyDAO, achievementDAO, null, null);
    }

    private Student getStudentByUserId(Long userId) {
        return studentDAO.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found for user"));
    }

    private ApplicationResponseDTO mapToResponse(Application app, Event event, Student student, String teamName) {
        ApplicationResponseDTO response = new ApplicationResponseDTO();
        response.setApplicationId(app.getApplicationId());
        response.setApplicationCode(app.getApplicationCode());
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setOrganizer(event.getOrganizer());
        response.setVenue(event.getVenue());
        response.setEventMode(event.getMode() == null ? null : event.getMode().name());
        response.setEventUrl(event.getEventUrl());
        response.setOtherCategory(event.getOtherCategory());
        response.setCity(event.getCity());
        response.setState(event.getState());
        response.setCountry(event.getCountry());
        response.setDescription(event.getDescription());
        response.setRegistrationFeeRequired(event.isRegistrationFeeRequired());
        response.setRegistrationFeeAmount(event.getRegistrationFeeAmount());
        response.setStudentId(student.getStudentId());
        response.setStudentName(student.getName());
        response.setRegistrationNumber(student.getRegisterNumber());
        response.setEventType(event.getEventTypeName() != null ? event.getEventTypeName() : event.getCategoryName());
        response.setEventTypeCode(event.getEventTypeCode());
        response.setTypeSpecificDetails(event.getTypeSpecificDetails());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setParticipationType(app.getParticipationType());
        response.setOutsideState(app.isOutsideState());
        response.setTravelRequired(app.isTravelRequired());
        response.setDestinationCity(app.getDestinationCity());
        response.setDestinationState(app.getDestinationState());
        response.setDestinationCountry(app.getDestinationCountry());
        response.setVenueAddress(app.getVenueAddress());
        response.setModeOfTravel(app.getModeOfTravel());
        response.setDepartureAt(app.getDepartureAt());
        response.setReturnAt(app.getReturnAt());
        response.setAccommodationRequired(app.isAccommodationRequired());
        response.setAccommodationName(app.getAccommodationName());
        response.setAccommodationAddress(app.getAccommodationAddress());
        response.setCheckInDate(app.getCheckInDate());
        response.setCheckOutDate(app.getCheckOutDate());
        response.setEmergencyContactName(app.getEmergencyContactName());
        response.setEmergencyContactNumber(app.getEmergencyContactNumber());
        response.setGuardianContactNumber(app.getGuardianContactNumber());
        response.setTravelRemarks(app.getTravelRemarks());
        response.setTeamName(app.getParticipationType() == ParticipationType.TEAM
                ? applicationDAO.findTeamNameByApplicationId(app.getApplicationId()).orElse(teamName)
                : null);
        response.setTeamMembers(app.getParticipationType() == ParticipationType.TEAM
                ? applicationDAO.findTeamMembersByApplicationId(app.getApplicationId())
                : List.of());
        response.setStatus(app.getCurrentStatus());
        response.setSubmittedAt(app.getSubmittedAt());
        response.setMentorRemarks(app.getMentorRemarks());
        response.setHodRemarks(app.getHodRemarks());
        response.setAssignedMentorId(app.getAssignedMentorId());
        response.setHodId(app.getHodId());
        response.setMentorReviewedAt(app.getMentorReviewedAt());
        response.setHodReviewedAt(app.getHodReviewedAt());
        response.setApprovalHistory(applicationDAO.findApprovalHistoryByApplicationId(app.getApplicationId()));
        response.setParticipationStatus(app.getParticipationStatus());
        response.setPostEventReportedAt(app.getPostEventReportedAt());
        response.setOutcomeNotes(app.getOutcomeNotes());
        response.setResultUrl(app.getResultUrl());
        var achievement = achievementDAO == null ? null : achievementDAO.findByApplicationId(app.getApplicationId())
                .map(com.srm.events.dto.AchievementResponseDTO::new).orElse(null);
        response.setAchievement(achievement);
        response.setPostEventStatus(PostEventLifecycle.derive(app, event, achievement != null));
        if ("PENDING".equals(response.getPostEventStatus()) || "SUBMITTED".equals(response.getPostEventStatus())) {
            response.setPostEventDaysPending(Math.max(0, LocalDate.now().toEpochDay() - event.getEndDate().toEpochDay()));
        }
        return response;
    }

    @Override
    public ApplicationResponseDTO registerApplication(RegisterApplicationRequestDTO request, Long userId) {
        Student student = getStudentByUserId(userId);
        
        Event event = eventDAO.findById(request.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
                
        if (event.getStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot register for an event that has already started or ended");
        }
        
        if (applicationDAO.existsByStudentAndEvent(student.getStudentId(), event.getEventId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is already registered for this event");
        }
        
        if (request.getParticipationType() == ParticipationType.TEAM && (request.getTeamName() == null || request.getTeamName().trim().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name is required for TEAM participation");
        }

        List<String> requestedMembers = request.getTeamMemberRegistrationNumbers();
        if (request.getParticipationType() == ParticipationType.INDIVIDUAL
                && (request.getTeamName() != null && !request.getTeamName().isBlank()
                || requestedMembers != null && !requestedMembers.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Individual applications cannot contain team information");
        }

        List<Long> teamMemberIds = new ArrayList<>();
        if (request.getParticipationType() == ParticipationType.TEAM) {
            teamMemberIds.add(student.getStudentId());
            Set<String> registrations = new HashSet<>();
            registrations.add(student.getRegisterNumber().toUpperCase(Locale.ROOT));
            for (String rawRegistration : requestedMembers == null ? List.<String>of() : requestedMembers) {
                String registration = rawRegistration == null ? "" : rawRegistration.trim();
                if (registration.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Team member registration numbers cannot be blank");
                }
                if (!registrations.add(registration.toUpperCase(Locale.ROOT))) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Duplicate team member registration number: " + registration);
                }
                Student member = studentDAO.findByRegistrationNumber(registration)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Team member is not an existing Student: " + registration));
                teamMemberIds.add(member.getStudentId());
            }
        }

        List<Long> mentorIds = applicationDAO.findActiveMentorIdsForStudent(student.getStudentId());
        if (mentorIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No active Mentor assignment exists for this student's section and academic year");
        }
        if (mentorIds.size() > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Multiple active Mentor assignments exist for this student's section and academic year");
        }

        Application application = new Application();
        application.setEventId(event.getEventId());
        application.setCreatedByStudentId(student.getStudentId());
        application.setAssignedMentorId(mentorIds.getFirst());
        application.setAcademicYearId(student.getAcademicYearId());
        application.setApplicationSemester(student.getSemester());
        application.setApplicationStudentYear(student.getStudentYear());
        application.setParticipationType(request.getParticipationType());
        application.setTravelRequired(request.isTravelRequired());
        application.setOutsideState(request.isOutsideState());

        Application createdApp = applicationDAO.create(application,
                request.getTeamName() == null ? null : request.getTeamName().trim(), teamMemberIds);
        notifyMentor(createdApp, "APPLICATION_SUBMITTED", "New activity application", "New activity application from " + student.getName() + " requires your review.", "APPLICATION_SUBMITTED:" + createdApp.getApplicationId() + ":" + createdApp.getSubmittedAt());
        return mapToResponse(createdApp, event, student, request.getTeamName());
    }

    @Override
    public List<ApplicationResponseDTO> getMyApplications(Long userId) {
        Student student = getStudentByUserId(userId);
        List<Application> apps = applicationDAO.findByStudentId(student.getStudentId());
        
        return apps.stream().map(app -> {
            Event event = eventDAO.findById(app.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));
            return mapToResponse(app, event, student, null);
        }).collect(Collectors.toList());
    }

    @Override
    public ApplicationResponseDTO getApplicationByCode(String code, Long userId) {
        Student student = getStudentByUserId(userId);
        Application app = applicationDAO.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
                
        if (!app.getCreatedByStudentId().equals(student.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this application");
        }
        
        Event event = eventDAO.findById(app.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));
                
        return mapToResponse(app, event, student, null);
    }

    @Override
    public List<ApplicationResponseDTO> getReviewQueue(Long userId, String role) {
        if (!"MENTOR".equalsIgnoreCase(role) && !"HOD".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not authorized for review queue");
        }
        Faculty faculty = getActiveFaculty(userId);
        List<Application> applications;
        if ("MENTOR".equalsIgnoreCase(role)) {
            applications = applicationDAO.findMentorReviewQueue(faculty.getFacultyId());
        } else if ("HOD".equalsIgnoreCase(role)) {
            applications = applicationDAO.findHodReviewQueue(faculty.getDepartmentId());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not authorized for review queue");
        }

        return applications.stream().map(app -> {
            Student student = studentDAO.findById(app.getCreatedByStudentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student data missing"));
            Event event = eventDAO.findById(app.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));
            return mapToResponse(app, event, student, null);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponseDTO> getPostEventPending(Long userId, String role) {
        if (!"MENTOR".equalsIgnoreCase(role) && !"HOD".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not authorized for post-event follow-up");
        }
        Faculty faculty = getActiveFaculty(userId);
        List<Application> applications = "MENTOR".equalsIgnoreCase(role)
                ? applicationDAO.findPostEventPendingForMentor(faculty.getFacultyId())
                : applicationDAO.findPostEventPendingForHod(faculty.getDepartmentId());
        return applications.stream().map(app -> {
            Student student = studentDAO.findById(app.getCreatedByStudentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student data missing"));
            Event event = eventDAO.findById(app.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));
            return mapToResponse(app, event, student, null);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponseDTO> getAllApplications() {
        return applicationDAO.findAll().stream().map(app -> {
            Student student = studentDAO.findById(app.getCreatedByStudentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student data missing"));
            Event event = eventDAO.findById(app.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));
            return mapToResponse(app, event, student, null);
        }).collect(Collectors.toList());
    }

    @Override
    public ApplicationResponseDTO reviewApplication(Long applicationId, com.srm.events.dto.ReviewApplicationRequestDTO review, Long reviewerId, String role) {
        if (!"MENTOR".equalsIgnoreCase(role) && !"HOD".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not authorized to review");
        }
        Application app = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        Faculty faculty = getActiveFaculty(reviewerId);
        String action = review.getStatus().trim().toUpperCase(java.util.Locale.ROOT);
        String remarks = review.getRemarks() == null ? null : review.getRemarks().trim();
        boolean updated;

        if ("MENTOR".equalsIgnoreCase(role)) {
            if (!applicationDAO.isMentorAssignedToApplication(applicationId, faculty.getFacultyId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Application is not assigned to this Mentor");
            }
            if (app.getCurrentStatus() != ApplicationStatus.SUBMITTED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application is not in SUBMITTED state");
            }
            ApplicationStatus newStatus;
            if ("RECOMMENDED".equals(action)) {
                newStatus = ApplicationStatus.PENDING_HOD_APPROVAL;
            } else if ("RETURNED_BY_MENTOR".equals(action) || "RETURN_FOR_CORRECTION".equals(action)) {
                requireCorrectionRemarks(remarks);
                newStatus = ApplicationStatus.RETURNED_BY_MENTOR;
            } else if ("REJECTED".equals(action)) {
                requireRejectionRemarks(remarks);
                newStatus = ApplicationStatus.REJECTED_BY_MENTOR;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor action must be RECOMMENDED, RETURNED_BY_MENTOR, or REJECTED");
            }
            updated = applicationDAO.updateMentorReview(applicationId, ApplicationStatus.SUBMITTED,
                    newStatus, action, remarks, faculty.getFacultyId(), reviewerId);
        } else if ("HOD".equalsIgnoreCase(role)) {
            if (!applicationDAO.isApplicationInDepartment(applicationId, faculty.getDepartmentId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Application is outside this HOD's department");
            }
            if (app.getCurrentStatus() != ApplicationStatus.PENDING_HOD_APPROVAL) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application is not in PENDING_HOD_APPROVAL state");
            }
            ApplicationStatus newStatus;
            if ("APPROVED".equals(action)) {
                newStatus = ApplicationStatus.APPROVED;
            } else if ("REJECTED".equals(action)) {
                requireRejectionRemarks(remarks);
                newStatus = ApplicationStatus.REJECTED_BY_HOD;
            } else if ("RETURNED_BY_HOD".equals(action) || "RETURN_FOR_CORRECTION".equals(action)) {
                requireCorrectionRemarks(remarks);
                newStatus = ApplicationStatus.RETURNED_BY_HOD;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HOD action must be APPROVED, RETURNED_BY_HOD, or REJECTED");
            }
            updated = applicationDAO.updateHodReview(applicationId, ApplicationStatus.PENDING_HOD_APPROVAL,
                    newStatus, action, remarks, faculty.getFacultyId(), reviewerId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not authorized to review");
        }

        if (!updated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application state changed before the review was saved");
        }

        // Reload application
        Application updatedApp = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load updated application"));

        Student student = studentDAO.findById(updatedApp.getCreatedByStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student data missing"));
        Event event = eventDAO.findById(updatedApp.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));

        notifyReviewTransition(updatedApp, student, faculty, role, action);

        return mapToResponse(updatedApp, event, student, null);
    }

    private void notifyMentor(Application application, String type, String title, String message, String dedupKey) {
        if (notificationService == null || notificationRecipients == null || application.getAssignedMentorId() == null) return;
        notificationRecipients.findUserIdByFacultyId(application.getAssignedMentorId()).ifPresent(userId -> safeNotify(userId, type, title, message, "APPLICATION", application.getApplicationId(), "/staff/mentor", dedupKey));
    }

    private void notifyReviewTransition(Application application, Student student, Faculty faculty, String role, String action) {
        if (notificationService == null || notificationRecipients == null) return;
        String path = "/student/applications/" + application.getApplicationCode();
        if ("MENTOR".equalsIgnoreCase(role)) {
            if ("RECOMMENDED".equals(action)) notificationRecipients.findHodUserIdByDepartmentId(student.getDepartmentId()).ifPresent(userId -> safeNotify(userId, "APPLICATION_RECOMMENDED_TO_HOD", "Activity application recommended", "An activity application has been recommended for HOD review.", "APPLICATION", application.getApplicationId(), "/staff/hod", "APPLICATION_RECOMMENDED_TO_HOD:" + application.getApplicationId()));
            else if ("RETURNED_BY_MENTOR".equals(application.getCurrentStatus().name())) safeNotify(student.getUserId(), "APPLICATION_RETURNED_BY_MENTOR", "Activity application returned", "Your activity application was returned for correction. Mentor remarks are available.", "APPLICATION", application.getApplicationId(), path, "APPLICATION_RETURNED_BY_MENTOR:" + application.getApplicationId() + ":" + application.getUpdatedAt());
            else if (application.getCurrentStatus().name().equals("REJECTED_BY_MENTOR")) safeNotify(student.getUserId(), "APPLICATION_REJECTED_BY_MENTOR", "Activity application rejected", "Your activity application was rejected by Mentor.", "APPLICATION", application.getApplicationId(), path, "APPLICATION_REJECTED_BY_MENTOR:" + application.getApplicationId() + ":" + application.getUpdatedAt());
        } else if ("HOD".equalsIgnoreCase(role)) {
            if ("APPROVED".equals(application.getCurrentStatus().name())) safeNotify(student.getUserId(), "APPLICATION_APPROVED", "Activity application approved", "Your activity application has been approved.", "APPLICATION", application.getApplicationId(), path, "APPLICATION_APPROVED:" + application.getApplicationId() + ":" + application.getUpdatedAt());
            else if ("RETURNED_BY_HOD".equals(application.getCurrentStatus().name())) safeNotify(student.getUserId(), "APPLICATION_RETURNED_BY_HOD", "Activity application returned", "Your activity application was returned for correction by HOD.", "APPLICATION", application.getApplicationId(), path, "APPLICATION_RETURNED_BY_HOD:" + application.getApplicationId() + ":" + application.getUpdatedAt());
            else if ("REJECTED_BY_HOD".equals(application.getCurrentStatus().name())) safeNotify(student.getUserId(), "APPLICATION_REJECTED_BY_HOD", "Activity application rejected", "Your activity application was rejected by HOD.", "APPLICATION", application.getApplicationId(), path, "APPLICATION_REJECTED_BY_HOD:" + application.getApplicationId() + ":" + application.getUpdatedAt());
        }
    }

    private void safeNotify(Long userId, String type, String title, String message, String recordType, Long recordId, String path, String key) {
        try { notificationService.notifyOnce(userId, type, title, message, recordType, recordId, path, key); }
        catch (RuntimeException ignored) { /* Workflow state is already committed; notification retry is scheduler/operational work. */ }
    }

    private Faculty getActiveFaculty(Long userId) {
        return facultyDAO.findActiveByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active faculty profile required"));
    }

    private void requireRejectionRemarks(String remarks) {
        if (remarks == null || remarks.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remarks are required for rejection");
        }
    }

    private void requireCorrectionRemarks(String remarks) {
        if (remarks == null || remarks.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remarks are required when returning an application for correction");
        }
    }
}
