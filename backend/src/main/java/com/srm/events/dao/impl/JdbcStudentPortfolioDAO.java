package com.srm.events.dao.impl;

import com.srm.events.analytics.StudentPortfolioData;
import com.srm.events.analytics.StudentPortfolioFilter;
import com.srm.events.dao.interfaces.StudentPortfolioDAO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcStudentPortfolioDAO implements StudentPortfolioDAO {
    private final JdbcTemplate jdbc;

    public JdbcStudentPortfolioDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public StudentPortfolioData load(Long studentId, StudentPortfolioFilter filter) {
        Map<String, Object> student = jdbc.queryForMap("SELECT s.student_id, s.name, s.register_number, d.department_name, sec.section_name, s.student_year FROM students s JOIN departments d ON d.department_id=s.department_id JOIN sections sec ON sec.section_id=s.section_id WHERE s.student_id=?", studentId);
        List<Map<String, Object>> activities = filter.recordType().equals("ACHIEVEMENTS") ? List.of() : jdbc.queryForList(activitySql(filter), activityArgs(studentId, filter));
        List<Map<String, Object>> achievements = filter.recordType().equals("ACTIVITIES") ? List.of() : jdbc.queryForList(achievementSql(filter), achievementArgs(studentId, filter));
        Long evidence = jdbc.queryForObject("SELECT COUNT(*) FROM documents d WHERE d.application_id IN (SELECT application_id FROM applications WHERE created_by_student_id=?) OR d.achievement_id IN (SELECT achievement_id FROM student_achievements WHERE student_id=?)", Long.class, studentId, studentId);
        return new StudentPortfolioData(student, activities, achievements, evidence == null ? 0L : evidence);
    }

    private String activitySql(StudentPortfolioFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT a.application_code, e.event_name, et.code AS type_code, et.name AS type_name, e.start_date, e.end_date, a.current_status, COALESCE(a.participation_status,'NOT_REPORTED') AS participation_status, r.result_type, r.position, a.application_semester AS semester, ay.year_label AS academic_year, CASE WHEN a.current_status <> 'APPROVED' OR e.end_date >= CURRENT_DATE THEN 'NOT_DUE' WHEN a.participation_status='REPORTED' THEN 'COMPLETE' WHEN a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NULL THEN 'SUBMITTED' WHEN a.participation_status='NO' THEN 'COMPLETE' WHEN a.participation_status='YES' AND r.achievement_id IS NOT NULL THEN 'COMPLETE' WHEN a.participation_status='YES' THEN 'SUBMITTED' ELSE 'PENDING' END AS post_event_status FROM applications a JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id JOIN academic_years ay ON ay.academic_year_id=a.academic_year_id LEFT JOIN achievements r ON r.application_id=a.application_id WHERE a.created_by_student_id=?");
        appendContext(sql, filter, "a.academic_year_id", "a.application_semester", "et.code");
        return sql.append(" ORDER BY COALESCE(e.end_date,e.start_date) DESC, a.application_id DESC").toString();
    }

    private String achievementSql(StudentPortfolioFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT sa.achievement_id, sa.title, sa.organization, et.code AS type_code, et.name AS type_name, COALESCE(sa.end_date,sa.start_date,DATE(sa.submitted_at)) AS record_date, sa.start_date, sa.end_date, sa.achievement_semester AS semester, ay.year_label AS academic_year, sa.verification_status, sa.type_specific_details FROM student_achievements sa JOIN event_types et ON et.event_type_id=sa.event_type_id JOIN academic_years ay ON ay.academic_year_id=sa.academic_year_id WHERE sa.student_id=?");
        appendContext(sql, filter, "sa.academic_year_id", "sa.achievement_semester", "et.code");
        return sql.append(" ORDER BY COALESCE(sa.end_date,sa.start_date,DATE(sa.submitted_at)) DESC, sa.achievement_id DESC").toString();
    }

    private void appendContext(StringBuilder sql, StudentPortfolioFilter filter, String yearColumn, String semesterColumn, String typeColumn) {
        if (filter.academicYearId() != null) sql.append(" AND ").append(yearColumn).append("=?");
        if (filter.semester() != null) sql.append(" AND ").append(semesterColumn).append("=?");
        if (filter.typeCode() != null) sql.append(" AND ").append(typeColumn).append("=?");
    }

    private Object[] activityArgs(Long studentId, StudentPortfolioFilter filter) { return args(studentId, filter); }
    private Object[] achievementArgs(Long studentId, StudentPortfolioFilter filter) { return args(studentId, filter); }
    private Object[] args(Long studentId, StudentPortfolioFilter filter) {
        List<Object> args = new ArrayList<>(); args.add(studentId);
        if (filter.academicYearId() != null) args.add(filter.academicYearId());
        if (filter.semester() != null) args.add(filter.semester());
        if (filter.typeCode() != null) args.add(filter.typeCode());
        return args.toArray();
    }
}
