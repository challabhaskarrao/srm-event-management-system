package com.srm.events.service.impl;

import com.srm.events.analytics.StudentPortfolioData;
import com.srm.events.analytics.StudentPortfolioFilter;
import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dao.interfaces.StudentPortfolioDAO;
import com.srm.events.model.Student;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentPortfolioService {
    private final StudentDAO studentDAO;
    private final StudentPortfolioDAO portfolioDAO;

    public StudentPortfolioService(StudentDAO studentDAO, StudentPortfolioDAO portfolioDAO) { this.studentDAO = studentDAO; this.portfolioDAO = portfolioDAO; }

    public Map<String, Object> get(Long userId, StudentPortfolioFilter filter) {
        Student student = studentDAO.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile required"));
        StudentPortfolioData data = portfolioDAO.load(student.getStudentId(), filter);
        Map<String, Object> summary = new LinkedHashMap<>();
        long approved = count(data.activities(), "current_status", "APPROVED");
        long participated = data.activities().stream().filter(row -> "APPROVED".equals(row.get("current_status")) && "YES".equalsIgnoreCase(String.valueOf(row.get("participation_status")))).count();
        long completed = count(data.activities(), "post_event_status", "COMPLETE");
        summary.put("activitiesApplied", data.activities().size()); summary.put("activitiesApproved", approved); summary.put("activitiesParticipated", participated); summary.put("completedActivities", completed);
        summary.put("achievementsRecorded", data.achievements().size()); summary.put("verifiedAchievements", count(data.achievements(), "verification_status", "VERIFIED"));
        for (String result : List.of("WINNER", "RUNNER_UP", "FINALIST", "POSITION")) summary.put(result.toLowerCase() + "s", data.activities().stream().filter(row -> result.equals(row.get("result_type")) && "APPROVED".equals(row.get("current_status"))).count());
        summary.put("evidenceCount", data.evidenceCount());

        List<Map<String, Object>> timeline = new ArrayList<>();
        data.activities().forEach(row -> { Map<String,Object> item = new LinkedHashMap<>(); item.put("recordType", "ACTIVITY"); item.put("title", row.get("event_name")); item.put("date", row.get("end_date")); item.put("status", row.get("post_event_status")); timeline.add(item); });
        data.achievements().forEach(row -> { Map<String,Object> item = new LinkedHashMap<>(); item.put("recordType", "ACHIEVEMENT"); item.put("title", row.get("title")); item.put("date", row.get("record_date")); item.put("status", verificationLabel(String.valueOf(row.get("verification_status")))); timeline.add(item); });
        timeline.sort(Comparator.comparing(item -> String.valueOf(item.get("date")), Comparator.reverseOrder()));
        Map<String, Object> response = new LinkedHashMap<>(); response.put("student", data.student()); response.put("summary", summary); response.put("activities", data.activities()); response.put("achievements", data.achievements()); response.put("timeline", timeline); response.put("historicalContext", Map.of("unknownSemesterLabel", "Unknown / Historical Semester Not Recorded", "note", "NULL snapshot values are preserved as Unknown; the current student semester is never substituted."));
        return response;
    }

    private long count(List<Map<String,Object>> rows, String key, String expected) { return rows.stream().filter(row -> expected.equals(row.get(key))).count(); }
    private String verificationLabel(String status) { return "VERIFIED".equals(status) ? "Verified" : "REJECTED".equals(status) ? "Rejected" : "Verification Pending"; }
}
