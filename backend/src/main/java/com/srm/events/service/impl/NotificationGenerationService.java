package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.MentorFollowUpDAO;
import com.srm.events.dto.MentorFollowUpItemDTO;
import com.srm.events.service.interfaces.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationGenerationService {
    private final JdbcTemplate jdbc; private final MentorFollowUpDAO followUpDAO; private final NotificationService notifications;
    public NotificationGenerationService(JdbcTemplate jdbc, MentorFollowUpDAO followUpDAO, NotificationService notifications) { this.jdbc=jdbc; this.followUpDAO=followUpDAO; this.notifications=notifications; }

    /** Uses the existing Mentor Follow-Up Centre data source; the scheduler only summarizes it. */
    public int generateWeeklyMentorFollowUps(LocalDate reportingDate) {
        WeekFields week = WeekFields.ISO; int weekNumber=reportingDate.get(week.weekOfWeekBasedYear()); int weekYear=reportingDate.get(week.weekBasedYear()); int generated=0;
        List<Map<String,Object>> mentors = jdbc.queryForList("SELECT f.faculty_id,f.user_id FROM faculty f JOIN users u ON u.user_id=f.user_id JOIN roles r ON r.role_id=u.role_id WHERE f.is_active=TRUE AND u.is_active=TRUE AND r.role_name='MENTOR'");
        for (Map<String,Object> mentor : mentors) {
            Long facultyId=((Number)mentor.get("faculty_id")).longValue(); Long userId=((Number)mentor.get("user_id")).longValue(); List<MentorFollowUpItemDTO> items=followUpDAO.findForMentor(facultyId); if(items.isEmpty()) continue;
            Map<String,Integer> counts=new LinkedHashMap<>(); for(MentorFollowUpItemDTO item:items) counts.merge(item.getFollowUpType(),1,Integer::sum);
            String message=summary(counts,items.size()); String key="MENTOR_WEEKLY_FOLLOWUP:"+userId+":"+weekYear+"-W"+String.format("%02d",weekNumber);
            if(notifications.notifyOnce(userId,"MENTOR_WEEKLY_FOLLOWUP","Weekly student follow-up",message,"MENTOR_FOLLOW_UP",facultyId,"/staff/mentor",key)) generated++;
        }
        return generated;
    }

    public int generatePostEventReminders() {
        int generated=0;
        List<Map<String,Object>> due=jdbc.queryForList("SELECT a.application_id,a.application_code,s.user_id,e.event_name FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id WHERE a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND (a.participation_status IS NULL OR a.participation_status='NOT_YET_OCCURRED')");
        for(Map<String,Object> row:due){Long id=((Number)row.get("application_id")).longValue();String code=String.valueOf(row.get("application_code"));String key="POST_EVENT_DUE:"+id;if(notifications.notifyOnce(((Number)row.get("user_id")).longValue(),"POST_EVENT_DUE","Post-activity report is due","Post-activity report is now due for "+row.get("event_name")+".","APPLICATION",id,"/student/applications/"+code,key))generated++;}
        List<Map<String,Object>> incomplete=jdbc.queryForList("SELECT a.application_id,a.application_code,a.updated_at,s.user_id,e.event_name FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id WHERE a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND ((a.participation_status='YES' AND NOT EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NULL))");
        for(Map<String,Object> row:incomplete){Long id=((Number)row.get("application_id")).longValue();String version=String.valueOf(row.get("updated_at"));String key="POST_EVENT_EVIDENCE_INCOMPLETE:"+id+":"+version;if(notifications.notifyOnce(((Number)row.get("user_id")).longValue(),"POST_EVENT_EVIDENCE_INCOMPLETE","Additional post-activity evidence is required","Additional post-activity evidence is required for "+row.get("event_name")+".","APPLICATION",id,"/student/applications/"+row.get("application_code"),key))generated++;}
        return generated;
    }

    private String summary(Map<String,Integer> counts,int total){StringBuilder value=new StringBuilder(" ").append(total).append(total==1?" student requires attention.":" students require attention."); for(String type:List.of("NEEDS_MENTOR_REVIEW","STUDENT_CORRECTION_PENDING","POST_EVENT_PENDING","POST_EVENT_EVIDENCE_INCOMPLETE","ACHIEVEMENT_VERIFICATION_PENDING")){Integer count=counts.get(type);if(count!=null) value.append(" ").append(count).append(" ").append(label(type)).append(".");}return value.toString().trim();}
    private String label(String type){return switch(type){case "NEEDS_MENTOR_REVIEW"->"needs mentor review";case "STUDENT_CORRECTION_PENDING"->"corrections awaiting students";case "POST_EVENT_PENDING"->"post-activity reports pending";case "POST_EVENT_EVIDENCE_INCOMPLETE"->"evidence incomplete";case "ACHIEVEMENT_VERIFICATION_PENDING"->"achievement verifications pending";default->type.toLowerCase().replace('_',' ');};}
}
