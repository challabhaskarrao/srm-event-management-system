package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.MentorFollowUpDAO;
import com.srm.events.dto.MentorFollowUpItemDTO;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.service.impl.PostEventLifecycle;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcMentorFollowUpDAO implements MentorFollowUpDAO {
    private final DataSource dataSource;

    public JdbcMentorFollowUpDAO(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public List<MentorFollowUpItemDTO> findForMentor(Long facultyId) {
        List<MentorFollowUpItemDTO> result = new ArrayList<>();
        findApplicationFollowUps(facultyId, result);
        findAchievementFollowUps(facultyId, result);
        result.sort((left, right) -> {
            int days = Long.compare(right.getDaysPending(), left.getDaysPending());
            return days != 0 ? days : left.getStudentName().compareToIgnoreCase(right.getStudentName());
        });
        return result;
    }

    private void findApplicationFollowUps(Long facultyId, List<MentorFollowUpItemDTO> result) {
        String sql = "WITH assigned_apps AS (" +
                "SELECT a.application_id,a.application_code,a.current_status,a.submitted_at,a.updated_at," +
                "a.mentor_remarks,a.hod_remarks,a.participation_status,a.outcome_notes," +
                "COALESCE((SELECT MAX(aa.action_at) FROM approval_actions aa WHERE aa.application_id=a.application_id " +
                "AND aa.action IN ('RETURNED_BY_MENTOR','RETURNED_BY_HOD')),a.updated_at,a.submitted_at) follow_up_at," +
                "s.student_id,s.name student_name,s.register_number,s.student_year,s.semester," +
                "d.department_name,sec.section_name,e.event_name,e.end_date,et.name event_type," +
                "EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id) result_present," +
                "CASE WHEN a.current_status='SUBMITTED' THEN 'NEEDS_MENTOR_REVIEW' " +
                "WHEN a.current_status IN ('RETURNED_BY_MENTOR','RETURNED_BY_HOD') THEN 'STUDENT_CORRECTION_PENDING' " +
                "ELSE NULL END follow_up_type " +
                "FROM applications a JOIN students s ON s.student_id=a.created_by_student_id " +
                "JOIN departments d ON d.department_id=s.department_id JOIN sections sec ON sec.section_id=s.section_id " +
                "JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " +
                "JOIN mentor_assignments ma ON ma.faculty_id=? AND ma.department_id=s.department_id " +
                "AND ma.section_id=s.section_id AND ma.academic_year_id=s.academic_year_id " +
                "AND ma.student_year=s.student_year AND ma.is_active=TRUE AND ma.active_from<=CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until>=CURRENT_DATE) " +
                "WHERE (a.assigned_mentor_id IS NULL OR a.assigned_mentor_id=ma.faculty_id) " +
                "AND (a.current_status IN ('SUBMITTED','RETURNED_BY_MENTOR','RETURNED_BY_HOD') " +
                "OR (a.current_status='APPROVED' AND e.end_date<CURRENT_DATE))" +
                ") SELECT * FROM assigned_apps";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, facultyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    MentorFollowUpItemDTO item = mapApplication(rs);
                    if (item != null) result.add(item);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error loading Mentor follow-ups", exception);
        }
    }

    private void findAchievementFollowUps(Long facultyId, List<MentorFollowUpItemDTO> result) {
        String sql = "SELECT sa.achievement_id,s.student_id,s.name student_name,s.register_number,s.student_year,s.semester," +
                "d.department_name,sec.section_name,sa.title,et.name event_type,sa.verification_status,sa.submitted_at " +
                "FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id " +
                "JOIN departments d ON d.department_id=s.department_id JOIN sections sec ON sec.section_id=s.section_id " +
                "JOIN event_types et ON et.event_type_id=sa.event_type_id " +
                "JOIN mentor_assignments ma ON ma.faculty_id=? AND ma.faculty_id=sa.assigned_mentor_id " +
                "AND ma.department_id=s.department_id AND ma.section_id=s.section_id " +
                "AND ma.academic_year_id=s.academic_year_id AND ma.student_year=s.student_year " +
                "AND ma.is_active=TRUE AND ma.active_from<=CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until>=CURRENT_DATE) " +
                "WHERE sa.verification_status='PENDING_MENTOR_VERIFICATION'";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, facultyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.add(mapAchievement(rs));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error loading Mentor achievement follow-ups", exception);
        }
    }

    private MentorFollowUpItemDTO mapApplication(ResultSet rs) throws SQLException {
        MentorFollowUpItemDTO value = base(rs);
        value.setRecordType("APPLICATION");
        value.setApplicationId(rs.getLong("application_id"));
        value.setApplicationCode(rs.getString("application_code"));
        value.setTitle(rs.getString("event_name"));
        value.setRecordCategory(rs.getString("event_type"));
        value.setFollowUpType(rs.getString("follow_up_type"));
        value.setCurrentStatus(rs.getString("current_status"));
        String type = value.getFollowUpType();
        if (type == null && "APPROVED".equals(rs.getString("current_status"))) {
            boolean resultPresent = rs.getBoolean("result_present");
            String lifecycle = PostEventLifecycle.derive(ApplicationStatus.APPROVED,
                    rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate(),
                    rs.getString("participation_status"), rs.getString("outcome_notes"), resultPresent);
            if ("PENDING".equals(lifecycle)) type = "POST_EVENT_PENDING";
            else if ("SUBMITTED".equals(lifecycle)) type = "POST_EVENT_EVIDENCE_INCOMPLETE";
            else return null;
            value.setFollowUpType(type);
        }
        LocalDate date;
        if ("POST_EVENT_PENDING".equals(type) || "POST_EVENT_EVIDENCE_INCOMPLETE".equals(type)) date = rs.getDate("end_date").toLocalDate();
        else {
            Timestamp followUp = rs.getTimestamp("follow_up_at");
            date = followUp == null ? null : followUp.toLocalDateTime().toLocalDate();
        }
        value.setRelevantDate(date);
        value.setDaysPending(daysSince(date));
        Timestamp last = rs.getTimestamp("follow_up_at");
        value.setLastActionAt(last == null ? null : last.toLocalDateTime());
        value.setRemarks("STUDENT_CORRECTION_PENDING".equals(type)
                ? (rs.getString("hod_remarks") == null ? rs.getString("mentor_remarks") : rs.getString("hod_remarks")) : null);
        return value;
    }

    private MentorFollowUpItemDTO mapAchievement(ResultSet rs) throws SQLException {
        MentorFollowUpItemDTO value = base(rs);
        value.setRecordType("ACHIEVEMENT");
        value.setAchievementId(rs.getLong("achievement_id"));
        value.setFollowUpType("ACHIEVEMENT_VERIFICATION_PENDING");
        value.setTitle(rs.getString("title"));
        value.setRecordCategory(rs.getString("event_type"));
        value.setCurrentStatus(rs.getString("verification_status"));
        Timestamp submitted = rs.getTimestamp("submitted_at");
        LocalDate date = submitted == null ? null : submitted.toLocalDateTime().toLocalDate();
        value.setRelevantDate(date);
        value.setDaysPending(daysSince(date));
        value.setLastActionAt(submitted == null ? null : submitted.toLocalDateTime());
        return value;
    }

    private MentorFollowUpItemDTO base(ResultSet rs) throws SQLException {
        MentorFollowUpItemDTO value = new MentorFollowUpItemDTO();
        value.setStudentId(rs.getLong("student_id")); value.setStudentName(rs.getString("student_name"));
        value.setRegistrationNumber(rs.getString("register_number")); value.setDepartment(rs.getString("department_name"));
        value.setSection(rs.getString("section_name")); value.setStudentYear(rs.getInt("student_year"));
        value.setSemester(rs.getInt("semester")); return value;
    }

    private long daysSince(LocalDate date) { return date == null ? 0 : Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now())); }
}
