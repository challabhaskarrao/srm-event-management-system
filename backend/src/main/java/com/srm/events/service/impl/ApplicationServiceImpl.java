package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.ApplicationDAO;
import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dto.ApplicationResponseDTO;
import com.srm.events.dto.RegisterApplicationRequestDTO;
import com.srm.events.model.Application;
import com.srm.events.model.Event;
import com.srm.events.model.ParticipationType;
import com.srm.events.model.Student;
import com.srm.events.service.interfaces.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final StudentDAO studentDAO;
    private final EventDAO eventDAO;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, StudentDAO studentDAO, EventDAO eventDAO) {
        this.applicationDAO = applicationDAO;
        this.studentDAO = studentDAO;
        this.eventDAO = eventDAO;
    }

    private Student getStudentByUserId(Long userId) {
        return studentDAO.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found for user"));
    }

    private ApplicationResponseDTO mapToResponse(Application app, Event event, Student student, String teamName) {
        ApplicationResponseDTO response = new ApplicationResponseDTO();
        response.setApplicationCode(app.getApplicationCode());
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setStudentId(student.getStudentId());
        response.setStudentName(student.getName());
        response.setParticipationType(app.getParticipationType());
        response.setTeamName(teamName);
        response.setStatus(app.getCurrentStatus());
        response.setSubmittedAt(app.getSubmittedAt());
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

        Application application = new Application();
        application.setEventId(event.getEventId());
        application.setCreatedByStudentId(student.getStudentId());
        application.setAcademicYearId(student.getAcademicYearId());
        application.setParticipationType(request.getParticipationType());
        application.setTravelRequired(request.isTravelRequired());
        application.setOutsideState(request.isOutsideState());

        Application createdApp = applicationDAO.create(application, request.getTeamName());
        
        return mapToResponse(createdApp, event, student, request.getTeamName());
    }

    @Override
    public List<ApplicationResponseDTO> getMyApplications(Long userId) {
        Student student = getStudentByUserId(userId);
        List<Application> apps = applicationDAO.findByStudentId(student.getStudentId());
        
        return apps.stream().map(app -> {
            Event event = eventDAO.findById(app.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Event data missing"));
            // For simplicity, teamName is not fetched here unless we query the teams table.
            // The prompt doesn't strictly mandate joining teams for the list view, but it would be good.
            // Assuming teamName as null for now in the list view.
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
}
