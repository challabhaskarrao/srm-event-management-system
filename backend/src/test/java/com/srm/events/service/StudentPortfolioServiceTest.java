package com.srm.events.service;

import com.srm.events.analytics.StudentPortfolioData;
import com.srm.events.analytics.StudentPortfolioFilter;
import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dao.interfaces.StudentPortfolioDAO;
import com.srm.events.model.Student;
import com.srm.events.service.impl.StudentPortfolioService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StudentPortfolioServiceTest {
    @Test
    void loadsOnlyTheAuthenticatedStudentsRowsAndComputesHonestCounts() {
        StudentDAO students = mock(StudentDAO.class); StudentPortfolioDAO portfolio = mock(StudentPortfolioDAO.class);
        Student student = new Student(); student.setStudentId(41L); student.setUserId(9L);
        when(students.findByUserId(9L)).thenReturn(Optional.of(student));
        Map<String,Object> approved = new LinkedHashMap<>(); approved.put("current_status","APPROVED"); approved.put("participation_status","YES"); approved.put("post_event_status","COMPLETE"); approved.put("result_type","WINNER");
        Map<String,Object> rejected = new LinkedHashMap<>(); rejected.put("current_status","REJECTED"); rejected.put("participation_status","YES"); rejected.put("post_event_status","NOT_DUE");
        Map<String,Object> achievement = new LinkedHashMap<>(); achievement.put("verification_status","VERIFIED"); achievement.put("semester",null);
        when(portfolio.load(eq(41L), any())).thenReturn(new StudentPortfolioData(Map.of("name","Student"), List.of(approved,rejected), List.of(achievement), 2L));

        Map<String,Object> result = new StudentPortfolioService(students, portfolio).get(9L, new StudentPortfolioFilter(null,null,"ALL",null));

        Map<?,?> summary = (Map<?,?>) result.get("summary");
        assertEquals(2, summary.get("activitiesApplied"));
        assertEquals(1L, summary.get("activitiesParticipated"));
        assertEquals(1L, summary.get("winners"));
        assertEquals(1, summary.get("achievementsRecorded"));
        verify(portfolio).load(eq(41L), any());
    }
}
