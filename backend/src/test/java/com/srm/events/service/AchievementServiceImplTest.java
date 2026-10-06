package com.srm.events.service;

import com.srm.events.dao.interfaces.*;
import com.srm.events.dto.AchievementRequestDTO;
import com.srm.events.model.*;
import com.srm.events.service.impl.AchievementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AchievementServiceImplTest {
    private AchievementDAO achievements;private ApplicationDAO applications;private StudentDAO students;private FacultyDAO faculty;private EventDAO events;private AchievementServiceImpl service;
    private Application app;private Student student;private Event event;
    @BeforeEach void setup(){achievements=mock(AchievementDAO.class);applications=mock(ApplicationDAO.class);students=mock(StudentDAO.class);faculty=mock(FacultyDAO.class);events=mock(EventDAO.class);service=new AchievementServiceImpl(achievements,applications,students,faculty,events);app=new Application();app.setApplicationId(1L);app.setCreatedByStudentId(2L);app.setEventId(3L);app.setCurrentStatus(ApplicationStatus.APPROVED);student=new Student();student.setStudentId(2L);event=new Event();event.setEndDate(LocalDate.now().minusDays(1));when(applications.findById(1L)).thenReturn(Optional.of(app));when(students.findByUserId(4L)).thenReturn(Optional.of(student));when(events.findById(3L)).thenReturn(Optional.of(event));}
    @Test void approvedOwnerCanSubmitCompletedEventResult(){AchievementRequestDTO request=request("WINNER");when(achievements.create(any())).thenAnswer(invocation->{Achievement a=invocation.getArgument(0);a.setAchievementId(9L);a.setVerificationStatus("PENDING");a.setSubmittedAt(java.time.LocalDateTime.now());return a;});assertEquals("WINNER",service.submit(1L,request,4L,"STUDENT").getResultType());}
    @Test void rejectsResultBeforeApproval(){app.setCurrentStatus(ApplicationStatus.SUBMITTED);assertStatus(HttpStatus.CONFLICT,()->service.submit(1L,request("PARTICIPANT"),4L,"STUDENT"));}
    @Test void rejectsResultBeforeEventCompletes(){event.setEndDate(LocalDate.now().plusDays(1));assertStatus(HttpStatus.CONFLICT,()->service.submit(1L,request("PARTICIPANT"),4L,"STUDENT"));}
    @Test void rejectsResultOnEventEndDate(){event.setEndDate(LocalDate.now());assertStatus(HttpStatus.CONFLICT,()->service.submit(1L,request("PARTICIPANT"),4L,"STUDENT"));}
    @Test void rejectsAnotherStudent(){Student other=new Student();other.setStudentId(99L);when(students.findByUserId(5L)).thenReturn(Optional.of(other));assertStatus(HttpStatus.FORBIDDEN,()->service.submit(1L,request("PARTICIPANT"),5L,"STUDENT"));}
    private AchievementRequestDTO request(String result){AchievementRequestDTO request=new AchievementRequestDTO();request.setResultType(result);return request;}
    private void assertStatus(HttpStatus status,org.junit.jupiter.api.function.Executable action){assertEquals(status,assertThrows(ResponseStatusException.class,action).getStatusCode());}
}
