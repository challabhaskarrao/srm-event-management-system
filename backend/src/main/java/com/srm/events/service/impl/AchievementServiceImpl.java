package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.*;
import com.srm.events.dto.*;
import com.srm.events.model.*;
import com.srm.events.service.interfaces.AchievementService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class AchievementServiceImpl implements AchievementService {
    private static final Set<String> RESULTS=Set.of("PARTICIPANT","FINALIST","WINNER","RUNNER_UP","POSITION","OTHER");
    private final AchievementDAO achievementDAO; private final ApplicationDAO applicationDAO; private final StudentDAO studentDAO;
    private final FacultyDAO facultyDAO; private final EventDAO eventDAO;
    public AchievementServiceImpl(AchievementDAO achievementDAO,ApplicationDAO applicationDAO,StudentDAO studentDAO,FacultyDAO facultyDAO,EventDAO eventDAO){
        this.achievementDAO=achievementDAO;this.applicationDAO=applicationDAO;this.studentDAO=studentDAO;this.facultyDAO=facultyDAO;this.eventDAO=eventDAO;
    }
    @Override public AchievementResponseDTO submit(Long appId,AchievementRequestDTO request,Long userId,String role){
        if(!"STUDENT".equalsIgnoreCase(role))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Student access required");
        Application app=getApp(appId);Student student=studentDAO.findByUserId(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Student profile required"));
        if(!app.getCreatedByStudentId().equals(student.getStudentId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Cannot submit another Student's result");
        Event event=eventDAO.findById(app.getEventId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
        if(app.getCurrentStatus()!=ApplicationStatus.APPROVED||!LocalDate.now().isAfter(event.getEndDate()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Results require an approved application and a completed event");
        if(achievementDAO.findByApplicationId(appId).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"A result already exists for this application");
        String result=request.getResultType().trim().toUpperCase(Locale.ROOT);if(!RESULTS.contains(result))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported result type");
        if("POSITION".equals(result)&&(request.getPosition()==null||request.getPosition().isBlank()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Position is required for POSITION results");
        Achievement value=new Achievement();value.setApplicationId(appId);value.setResultType(result);value.setPosition(trim(request.getPosition()));value.setPrizeDetails(trim(request.getPrizeDetails()));
        return new AchievementResponseDTO(achievementDAO.create(value));
    }
    @Override public Optional<AchievementResponseDTO> get(Long appId,Long userId,String role){Application app=getApp(appId);authorize(app,userId,role);return achievementDAO.findByApplicationId(appId).map(AchievementResponseDTO::new);}
    @Override public AchievementResponseDTO review(Long appId,AchievementReviewDTO review,Long userId,String role){
        if(!"ADMIN".equalsIgnoreCase(role))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Admin access required");
        Achievement value=achievementDAO.findByApplicationId(appId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Result not found"));
        String status=review.getStatus().trim().toUpperCase(Locale.ROOT);if(!Set.of("VERIFIED","REJECTED","RESUBMISSION_REQUIRED").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported verification status");
        if(!"VERIFIED".equals(status)&&(review.getRemarks()==null||review.getRemarks().isBlank()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Remarks are required when a result is not verified");
        return new AchievementResponseDTO(achievementDAO.review(value.getAchievementId(),status,trim(review.getRemarks()),userId));
    }
    private Application getApp(Long id){return applicationDAO.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Application not found"));}
    private void authorize(Application app,Long userId,String role){
        if("ADMIN".equalsIgnoreCase(role))return;
        if("STUDENT".equalsIgnoreCase(role)){Student s=studentDAO.findByUserId(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN));if(app.getCreatedByStudentId().equals(s.getStudentId()))return;}
        if("MENTOR".equalsIgnoreCase(role)){Faculty f=facultyDAO.findActiveByUserId(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN));if(applicationDAO.isMentorAssignedToApplication(app.getApplicationId(),f.getFacultyId()))return;}
        if("HOD".equalsIgnoreCase(role)){Faculty f=facultyDAO.findActiveByUserId(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN));if(applicationDAO.isApplicationInDepartment(app.getApplicationId(),f.getDepartmentId()))return;}
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Result access denied");
    }
    private String trim(String value){return value==null||value.isBlank()?null:value.trim();}
}
