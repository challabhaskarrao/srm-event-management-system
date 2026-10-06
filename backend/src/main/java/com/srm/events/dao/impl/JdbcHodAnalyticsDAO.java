package com.srm.events.dao.impl;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.analytics.HodExportData;
import com.srm.events.dao.interfaces.HodAnalyticsDAO;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Repository
public class JdbcHodAnalyticsDAO implements HodAnalyticsDAO {
    private final DataSource dataSource;

    public JdbcHodAnalyticsDAO(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public Map<String, Object> load(Long departmentId, HodAnalyticsFilter filter) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period", filter.getPeriod().name());
        response.put("departmentId", departmentId);
        response.put("historicalContext", Map.of(
                "semesterCoverage", "SNAPSHOT_ONLY",
                "studentYearCoverage", "SNAPSHOT_ONLY",
                "unknownHistoricalRowsExcludedFromSemester", true,
                "note", "Rows created before the historical context migration remain NULL and are not assigned to a semester or student year."));

        response.put("summary", summary(departmentId, filter));
        response.put("activityTypes", activityTypes(departmentId, filter));
        response.put("achievementTypes", achievementTypes(departmentId, filter));
        response.put("monthlyTrend", trend(departmentId, filter));
        response.put("sections", sections(departmentId, filter));
        response.put("studentYears", studentYears(departmentId, filter));
        response.put("mentors", mentors(departmentId, filter));
        response.put("zeroActivityStudents", Map.of(
                "available", false,
                "reason", "A complete institutional student registry is required before students with no activity can be calculated."));
        return response;
    }

    @Override
    public HodExportData exportData(Long departmentId, HodAnalyticsFilter filter) {
        // Export rows are loaded through the same filter builders used by the dashboard and drilldowns.
        List<Object> activityParams = new ArrayList<>();
        String activityWhere = applicationWhere(departmentId, filter, activityParams);
        String activitySql = "SELECT s.name AS student_name, s.register_number, sec.section_name, a.application_student_year AS student_year, a.application_semester AS semester, COALESCE(f.name,u.email) AS mentor, e.event_name AS activity, et.name AS activity_type, e.start_date, e.end_date, a.current_status AS application_status, COALESCE(a.participation_status,'NOT_REPORTED') AS participation, r.result_type AS outcome, r.position, CASE WHEN a.current_status <> 'APPROVED' OR e.end_date >= CURRENT_DATE THEN 'NOT_DUE' WHEN a.participation_status='REPORTED' THEN 'COMPLETE' WHEN a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NULL THEN 'SUBMITTED' WHEN a.participation_status='NO' THEN 'COMPLETE' WHEN a.participation_status='YES' AND r.achievement_id IS NOT NULL THEN 'COMPLETE' WHEN a.participation_status='YES' THEN 'SUBMITTED' ELSE 'PENDING' END AS post_event_status, ay.year_label AS academic_year FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN sections sec ON sec.section_id=s.section_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id JOIN academic_years ay ON ay.academic_year_id=a.academic_year_id LEFT JOIN faculty f ON f.faculty_id=a.assigned_mentor_id LEFT JOIN users u ON u.user_id=f.user_id LEFT JOIN achievements r ON r.application_id=a.application_id " + activityWhere + " ORDER BY e.start_date DESC, a.application_id DESC";
        List<Object> achievementParams = new ArrayList<>();
        String achievementWhere = achievementWhere(departmentId, filter, achievementParams);
        String achievementSql = "SELECT s.name AS student_name, s.register_number, sec.section_name, sa.achievement_student_year AS student_year, sa.achievement_semester AS semester, COALESCE(f.name,u.email) AS mentor, sa.title AS achievement, et.name AS achievement_type, COALESCE(sa.end_date,sa.start_date,DATE(sa.submitted_at)) AS recorded_date, sa.verification_status, ay.year_label AS academic_year FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN sections sec ON sec.section_id=s.section_id JOIN event_types et ON et.event_type_id=sa.event_type_id JOIN academic_years ay ON ay.academic_year_id=sa.academic_year_id LEFT JOIN faculty f ON f.faculty_id=sa.assigned_mentor_id LEFT JOIN users u ON u.user_id=f.user_id " + achievementWhere + " ORDER BY recorded_date DESC, sa.achievement_id DESC";
        String departmentName = jdbcSingle("SELECT department_name FROM departments WHERE department_id=?", departmentId);
        return new HodExportData(departmentName, load(departmentId, filter), rows(activitySql, activityParams), rows(achievementSql, achievementParams));
    }

    private String jdbcSingle(String sql, Object... params) {
        List<Map<String,Object>> values = rows(sql, Arrays.asList(params));
        return values.isEmpty() ? "Unknown department" : String.valueOf(values.getFirst().get("department_name"));
    }

    private Map<String, Object> summary(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> params = new ArrayList<>();
        String where = applicationWhere(departmentId, filter, params);
        String sql = "SELECT COUNT(*) AS total_applications, " +
                "SUM(a.current_status='APPROVED') AS approved_applications, " +
                "SUM(a.current_status='SUBMITTED') AS pending_mentor, " +
                "SUM(a.current_status='PENDING_HOD_APPROVAL') AS pending_hod, " +
                "SUM(a.current_status LIKE 'RETURNED%') AS returned_applications, " +
                "SUM(a.current_status LIKE 'REJECTED%') AS rejected_applications, " +
                "SUM(a.current_status='APPROVED' AND a.participation_status='YES') AS approved_participation, " +
                "COUNT(DISTINCT CASE WHEN a.current_status='APPROVED' AND a.participation_status='YES' THEN s.student_id END) AS unique_students_participated, " +
                "SUM(a.current_status='APPROVED' AND a.participation_status='NO') AS did_not_participate, " +
                "SUM(a.current_status='APPROVED' AND e.end_date < CURRENT_DATE AND (a.participation_status IS NULL OR a.participation_status='NOT_YET_OCCURRED')) AS post_event_pending, " +
                "SUM(a.current_status='APPROVED' AND e.end_date < CURRENT_DATE AND ((a.participation_status='YES' AND NOT EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NULL))) AS evidence_incomplete, " +
                "SUM(a.current_status='APPROVED' AND e.end_date < CURRENT_DATE AND ((a.participation_status='YES' AND EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NOT NULL))) AS complete, " +
                "COUNT(DISTINCT CASE WHEN et.workflow_category='ACTIVITY' THEN s.student_id END) AS unique_activity_students, " +
                "SUM(CASE WHEN EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='PARTICIPANT') THEN 1 ELSE 0 END) AS participant_results, " +
                "SUM(CASE WHEN EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='FINALIST') THEN 1 ELSE 0 END) AS finalist_results, " +
                "SUM(CASE WHEN EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='WINNER') THEN 1 ELSE 0 END) AS winner_results, " +
                "SUM(CASE WHEN EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='RUNNER_UP') THEN 1 ELSE 0 END) AS runner_up_results, " +
                "SUM(CASE WHEN EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='POSITION') THEN 1 ELSE 0 END) AS position_results, " +
                "SUM(CASE WHEN EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='OTHER') THEN 1 ELSE 0 END) AS other_results " +
                "FROM applications a JOIN students s ON s.student_id=a.created_by_student_id " +
                "JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " + where;
        Map<String, Object> result = first(sql, params);
        List<Object> achievementParams = new ArrayList<>();
        String achievementWhere = achievementWhere(departmentId, filter, achievementParams);
        Map<String, Object> achievements = first("SELECT COUNT(*) AS achievements_recorded, " +
                "SUM(sa.verification_status IN ('PENDING_MENTOR_VERIFICATION','PENDING_HOD_VERIFICATION','RESUBMISSION_REQUIRED')) AS achievements_verification_pending, " +
                "SUM(sa.verification_status='VERIFIED') AS achievements_verified " +
                "FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id " +
                "JOIN event_types et ON et.event_type_id=sa.event_type_id " + achievementWhere, achievementParams);
        result.putAll(achievements);
        Map<String, Object> engagement = engagementSummary(departmentId, filter);
        result.putAll(engagement);
        return result;
    }

    private Map<String, Object> engagementSummary(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> appParams = new ArrayList<>();
        String appWhere = applicationWhere(departmentId, filter, appParams);
        List<Object> achievementParams = new ArrayList<>();
        String achievementWhere = achievementWhere(departmentId, filter, achievementParams);
        Map<String, Object> achievement = first("SELECT COUNT(DISTINCT s.student_id) AS unique_achievement_students FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + achievementWhere, achievementParams);
        List<Object> anyParams = new ArrayList<>(); anyParams.addAll(appParams); anyParams.addAll(achievementParams);
        Map<String, Object> any = first("SELECT COUNT(DISTINCT student_id) AS unique_any_engagement FROM (SELECT s.student_id FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " + appWhere + " UNION SELECT s.student_id FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + achievementWhere + ") engagement", anyParams);
        achievement.putAll(any);
        return achievement;
    }

    private List<Map<String, Object>> activityTypes(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> params = new ArrayList<>();
        String where = applicationWhere(departmentId, filter, params) + " AND et.workflow_category='ACTIVITY'";
        return rows("SELECT et.code AS type_code, et.name AS type_name, COUNT(*) AS applications, " +
                "COUNT(DISTINCT s.student_id) AS unique_students, SUM(a.current_status='APPROVED') AS approved, " +
                "SUM(a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND ((a.participation_status='YES' AND EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NOT NULL))) AS complete " +
                "FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id " +
                "JOIN event_types et ON et.event_type_id=e.event_type_id " + where + " GROUP BY et.event_type_id, et.code, et.name ORDER BY applications DESC, type_name", params);
    }

    private List<Map<String, Object>> achievementTypes(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> params = new ArrayList<>();
        String where = achievementWhere(departmentId, filter, params);
        return rows("SELECT et.code AS type_code, et.name AS type_name, COUNT(*) AS recorded, COUNT(DISTINCT s.student_id) AS unique_students, " +
                "SUM(sa.verification_status IN ('PENDING_MENTOR_VERIFICATION','PENDING_HOD_VERIFICATION','RESUBMISSION_REQUIRED')) AS pending, SUM(sa.verification_status='VERIFIED') AS verified " +
                "FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + where + " GROUP BY et.event_type_id, et.code, et.name ORDER BY recorded DESC, type_name", params);
    }

    private List<Map<String, Object>> trend(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> applicationParams = new ArrayList<>();
        String applicationWhere = applicationWhere(departmentId, filter, applicationParams);
        List<Object> achievementParams = new ArrayList<>();
        String achievementWhere = achievementWhere(departmentId, filter, achievementParams);
        List<Object> params = new ArrayList<>(); params.addAll(applicationParams); params.addAll(achievementParams);
        return rows("SELECT label, SUM(activity_applications) AS activity_applications, SUM(approved_activities) AS approved_activities, " +
                "SUM(completed_activities) AS completed_activities, SUM(achievements_recorded) AS achievements_recorded FROM (" +
                "SELECT DATE_FORMAT(a.submitted_at,'%Y-%m') AS label, COUNT(*) AS activity_applications, SUM(a.current_status='APPROVED') AS approved_activities, " +
                "SUM(a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND ((a.participation_status='YES' AND EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NOT NULL))) AS completed_activities, 0 AS achievements_recorded " +
                "FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " + applicationWhere + " AND et.workflow_category='ACTIVITY' GROUP BY label " +
                "UNION ALL SELECT DATE_FORMAT(sa.submitted_at,'%Y-%m') AS label, 0, 0, 0, COUNT(*) FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + achievementWhere + " GROUP BY label) trend GROUP BY label ORDER BY label", params);
    }

    private List<Map<String, Object>> sections(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> params = new ArrayList<>();
        String where = applicationWhere(departmentId, filter, params);
        List<Map<String, Object>> result = rows("SELECT sec.section_id, sec.section_name, COUNT(DISTINCT s.student_id) AS students_with_records, COUNT(*) AS applications, " +
                "SUM(a.current_status='APPROVED') AS approved_activities, SUM(a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND ((a.participation_status='YES' AND EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NOT NULL))) AS completed_activities, " +
                "SUM(a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND (a.participation_status IS NULL OR a.participation_status='NOT_YET_OCCURRED')) AS post_event_pending " +
                "FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN sections sec ON sec.section_id=s.section_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " + where + " GROUP BY sec.section_id, sec.section_name ORDER BY applications DESC, section_name", params);
        List<Object> achievementParams = new ArrayList<>();
        String achievementWhere = achievementWhere(departmentId, filter, achievementParams);
        List<Map<String, Object>> achievementRows = rows("SELECT s.section_id, sec.section_name, COUNT(*) AS achievements_recorded, COUNT(DISTINCT s.student_id) AS achievement_students FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN sections sec ON sec.section_id=s.section_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + achievementWhere + " GROUP BY s.section_id, sec.section_name", achievementParams);
        for (Map<String, Object> achievement : achievementRows) {
            Map<String, Object> matching = result.stream().filter(row -> Objects.equals(number(row.get("section_id")), number(achievement.get("section_id")))).findFirst().orElse(null);
            if (matching == null) {
                matching = new LinkedHashMap<>(); matching.put("section_id", achievement.get("section_id")); matching.put("section_name", achievement.get("section_name")); matching.put("students_with_records", 0L); matching.put("applications", 0L); matching.put("approved_activities", 0L); matching.put("completed_activities", 0L); matching.put("post_event_pending", 0L); result.add(matching);
            }
            matching.put("achievements_recorded", achievement.get("achievements_recorded"));
            matching.put("achievement_students", achievement.get("achievement_students"));
        }
        result.forEach(row -> { row.putIfAbsent("achievements_recorded", 0L); row.putIfAbsent("achievement_students", 0L); });
        return result;
    }

    private Long number(Object value) { return value instanceof Number number ? number.longValue() : null; }

    private List<Map<String, Object>> studentYears(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> appParams = new ArrayList<>(); String appWhere = applicationWhere(departmentId, filter, appParams);
        List<Object> achievementParams = new ArrayList<>(); String achWhere = achievementWhere(departmentId, filter, achievementParams);
        List<Object> params = new ArrayList<>(); params.addAll(appParams); params.addAll(achievementParams);
        return rows("SELECT student_year_label, SUM(records) AS records, SUM(unique_students) AS unique_students FROM (" +
                "SELECT COALESCE(CAST(a.application_student_year AS CHAR),'Unknown') AS student_year_label, COUNT(*) AS records, COUNT(DISTINCT s.student_id) AS unique_students FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " + appWhere + " GROUP BY student_year_label " +
                "UNION ALL SELECT COALESCE(CAST(sa.achievement_student_year AS CHAR),'Unknown'), COUNT(*), COUNT(DISTINCT s.student_id) FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + achWhere + " GROUP BY COALESCE(CAST(sa.achievement_student_year AS CHAR),'Unknown')) years GROUP BY student_year_label ORDER BY student_year_label", params);
    }

    private List<Map<String, Object>> mentors(Long departmentId, HodAnalyticsFilter filter) {
        List<Object> applicationParams = new ArrayList<>();
        String applicationWhere = applicationWhere(departmentId, filter, applicationParams);
        List<Object> achievementParams = new ArrayList<>();
        String achievementWhere = achievementWhere(departmentId, filter, achievementParams);
        List<Object> params = new ArrayList<>(); params.addAll(applicationParams); params.addAll(achievementParams);
        return rows("SELECT f.faculty_id AS mentor_id, COALESCE(u.email, CONCAT('Faculty #', f.faculty_id)) AS mentor_name, " +
                "(SELECT COUNT(DISTINCT st.student_id) FROM mentor_assignments ma2 JOIN students st ON st.department_id=ma2.department_id AND st.section_id=ma2.section_id AND st.academic_year_id=ma2.academic_year_id AND st.student_year=ma2.student_year WHERE ma2.faculty_id=f.faculty_id AND ma2.department_id=f.department_id AND ma2.is_active=TRUE AND ma2.active_from<=CURRENT_DATE AND (ma2.active_until IS NULL OR ma2.active_until>=CURRENT_DATE)) AS assigned_student_count, " +
                "COALESCE(am.applications,0) AS applications, COALESCE(am.needs_review,0) AS needs_review, COALESCE(am.correction_pending,0) AS correction_pending, COALESCE(am.post_event_pending,0) AS post_event_pending, COALESCE(ach.achievement_verification_pending,0) AS achievement_verification_pending " +
                "FROM faculty f JOIN users u ON u.user_id=f.user_id " +
                "LEFT JOIN (SELECT a.assigned_mentor_id AS mentor_id, COUNT(DISTINCT a.application_id) AS applications, SUM(a.current_status='SUBMITTED') AS needs_review, SUM(a.current_status LIKE 'RETURNED%') AS correction_pending, SUM(a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND (a.participation_status IS NULL OR a.participation_status='NOT_YET_OCCURRED')) AS post_event_pending " +
                "FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id " + applicationWhere + " GROUP BY a.assigned_mentor_id) am ON am.mentor_id=f.faculty_id " +
                "LEFT JOIN (SELECT sa.assigned_mentor_id AS mentor_id, COUNT(*) AS achievement_verification_pending FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN event_types et ON et.event_type_id=sa.event_type_id " + achievementWhere + " AND sa.verification_status IN ('PENDING_MENTOR_VERIFICATION','PENDING_HOD_VERIFICATION','RESUBMISSION_REQUIRED') GROUP BY sa.assigned_mentor_id) ach ON ach.mentor_id=f.faculty_id " +
                "WHERE f.department_id=? AND f.is_active=TRUE ORDER BY mentor_name", append(params, departmentId));
    }

    private List<Object> append(List<Object> values, Object value) { List<Object> result = new ArrayList<>(values); result.add(value); return result; }

    @Override
    public List<Map<String, Object>> drilldown(Long departmentId, HodAnalyticsFilter filter, String metric) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (Set.of("ACHIEVEMENTS", "ACHIEVEMENT_VERIFICATION_PENDING", "VERIFIED_ACHIEVEMENTS").contains(metric)) {
            List<Object> params = new ArrayList<>(); String where = achievementWhere(departmentId, filter, params);
            String extra = metric.equals("ACHIEVEMENT_VERIFICATION_PENDING") ? " AND sa.verification_status IN ('PENDING_MENTOR_VERIFICATION','PENDING_HOD_VERIFICATION','RESUBMISSION_REQUIRED')" : metric.equals("VERIFIED_ACHIEVEMENTS") ? " AND sa.verification_status='VERIFIED'" : "";
            return rows("SELECT 'ACHIEVEMENT' AS record_type, sa.achievement_id AS record_id, s.name AS student_name, s.register_number, sec.section_name, COALESCE(u.email, CONCAT('Faculty #',sa.assigned_mentor_id)) AS mentor_name, sa.title, et.name AS type_name, sa.submitted_at AS relevant_date, sa.verification_status AS status, NULL AS result, COALESCE(sa.hod_remarks,sa.mentor_remarks) AS remarks FROM student_achievements sa JOIN students s ON s.student_id=sa.student_id JOIN sections sec ON sec.section_id=s.section_id JOIN event_types et ON et.event_type_id=sa.event_type_id LEFT JOIN faculty f ON f.faculty_id=sa.assigned_mentor_id LEFT JOIN users u ON u.user_id=f.user_id " + where + extra + " ORDER BY sa.submitted_at DESC", params);
        }
        List<Object> params = new ArrayList<>(); String where = applicationWhere(departmentId, filter, params);
        String extra = applicationMetric(metric);
        return rows("SELECT 'APPLICATION' AS record_type, a.application_id AS record_id, s.name AS student_name, s.register_number, sec.section_name, COALESCE(u.email, CONCAT('Faculty #',a.assigned_mentor_id)) AS mentor_name, e.event_name AS title, et.name AS type_name, a.submitted_at AS relevant_date, a.current_status AS status, (SELECT ac.result_type FROM achievements ac WHERE ac.application_id=a.application_id ORDER BY ac.achievement_id DESC LIMIT 1) AS result, COALESCE(a.hod_remarks,a.mentor_remarks) AS remarks FROM applications a JOIN students s ON s.student_id=a.created_by_student_id JOIN sections sec ON sec.section_id=s.section_id JOIN events e ON e.event_id=a.event_id JOIN event_types et ON et.event_type_id=e.event_type_id LEFT JOIN faculty f ON f.faculty_id=a.assigned_mentor_id LEFT JOIN users u ON u.user_id=f.user_id " + where + extra + " ORDER BY a.submitted_at DESC", params);
    }

    private String applicationMetric(String metric) {
        return switch (metric) {
            case "PENDING_MENTOR" -> " AND a.current_status='SUBMITTED'";
            case "PENDING_HOD" -> " AND a.current_status='PENDING_HOD_APPROVAL'";
            case "RETURNED" -> " AND a.current_status LIKE 'RETURNED%'";
            case "REJECTED" -> " AND a.current_status LIKE 'REJECTED%'";
            case "POST_EVENT_PENDING" -> " AND a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND (a.participation_status IS NULL OR a.participation_status='NOT_YET_OCCURRED')";
            case "EVIDENCE_INCOMPLETE" -> " AND a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND ((a.participation_status='YES' AND NOT EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NULL))";
            case "COMPLETE" -> " AND a.current_status='APPROVED' AND e.end_date<CURRENT_DATE AND ((a.participation_status='YES' AND EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id)) OR (a.participation_status='NO' AND NULLIF(TRIM(a.outcome_notes),'') IS NOT NULL))";
            case "PARTICIPANT", "FINALIST", "WINNER", "RUNNER_UP", "POSITION", "OTHER" -> " AND EXISTS (SELECT 1 FROM achievements ac WHERE ac.application_id=a.application_id AND ac.result_type='" + metric + "')";
            default -> "";
        };
    }

    private String applicationWhere(Long departmentId, HodAnalyticsFilter filter, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE s.department_id=?"); params.add(departmentId);
        addPeriod(where, params, filter, "a.submitted_at", "a.academic_year_id", "a.application_semester");
        if (filter.getSectionId() != null) { where.append(" AND s.section_id=?"); params.add(filter.getSectionId()); }
        if (filter.getStudentYear() != null) { where.append(" AND a.application_student_year=?"); params.add(filter.getStudentYear()); }
        if (filter.getEventTypeCode() != null) { where.append(" AND et.code=?"); params.add(filter.getEventTypeCode().toUpperCase(Locale.ROOT)); }
        if (filter.getStatus() != null) { where.append(" AND a.current_status=?"); params.add(filter.getStatus().toUpperCase(Locale.ROOT)); }
        if (filter.getMentorId() != null) { where.append(" AND a.assigned_mentor_id=?"); params.add(filter.getMentorId()); }
        return where.toString();
    }

    private String achievementWhere(Long departmentId, HodAnalyticsFilter filter, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE s.department_id=?"); params.add(departmentId);
        addPeriod(where, params, filter, "sa.submitted_at", "sa.academic_year_id", "sa.achievement_semester");
        if (filter.getSectionId() != null) { where.append(" AND s.section_id=?"); params.add(filter.getSectionId()); }
        if (filter.getStudentYear() != null) { where.append(" AND sa.achievement_student_year=?"); params.add(filter.getStudentYear()); }
        if (filter.getEventTypeCode() != null) { where.append(" AND et.code=?"); params.add(filter.getEventTypeCode().toUpperCase(Locale.ROOT)); }
        if (filter.getStatus() != null) { where.append(" AND sa.verification_status=?"); params.add(filter.getStatus().toUpperCase(Locale.ROOT)); }
        if (filter.getMentorId() != null) { where.append(" AND sa.assigned_mentor_id=?"); params.add(filter.getMentorId()); }
        return where.toString();
    }

    private void addPeriod(StringBuilder where, List<Object> params, HodAnalyticsFilter filter, String dateColumn, String yearColumn, String semesterColumn) {
        switch (filter.getPeriod()) {
            case MONTHLY -> {
                LocalDate start = LocalDate.of(filter.getYear(), filter.getMonth(), 1);
                LocalDate end = start.plusMonths(1);
                where.append(" AND ").append(dateColumn).append(" >= ? AND ").append(dateColumn).append(" < ?");
                params.add(Timestamp.valueOf(start.atStartOfDay())); params.add(Timestamp.valueOf(end.atStartOfDay()));
            }
            case SEMESTER -> { where.append(" AND ").append(yearColumn).append("=? AND ").append(semesterColumn).append("=?"); params.add(filter.getAcademicYearId()); params.add(filter.getSemester()); }
            case ACADEMIC_YEAR -> { where.append(" AND ").append(yearColumn).append("=?"); params.add(filter.getAcademicYearId()); }
            case CUSTOM -> { where.append(" AND ").append(dateColumn).append(" >= ? AND ").append(dateColumn).append(" < ?"); params.add(Timestamp.valueOf(filter.getFromDate().atStartOfDay())); params.add(Timestamp.valueOf(filter.getToDate().plusDays(1).atStartOfDay())); }
        }
    }

    private Map<String, Object> first(String sql, List<Object> params) {
        List<Map<String, Object>> result = rows(sql, params);
        return result.isEmpty() ? new LinkedHashMap<>() : result.getFirst();
    }

    private List<Map<String, Object>> rows(String sql, List<Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) setParameter(statement, i + 1, params.get(i));
            try (ResultSet rs = statement.executeQuery()) {
                ResultSetMetaData metadata = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= metadata.getColumnCount(); i++) row.put(metadata.getColumnLabel(i), rs.getObject(i));
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Error loading HOD analytics", exception);
        }
    }

    private void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value instanceof Timestamp timestamp) statement.setTimestamp(index, timestamp);
        else if (value instanceof Long number) statement.setLong(index, number);
        else if (value instanceof Integer number) statement.setInt(index, number);
        else statement.setObject(index, value);
    }
}
