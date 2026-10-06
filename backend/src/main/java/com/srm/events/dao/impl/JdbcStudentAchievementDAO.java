package com.srm.events.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.events.dao.interfaces.StudentAchievementDAO;
import com.srm.events.dto.TeamMemberDTO;
import com.srm.events.model.ParticipationType;
import com.srm.events.model.StudentAchievement;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcStudentAchievementDAO implements StudentAchievementDAO {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcStudentAchievementDAO(DataSource dataSource) { this.dataSource = dataSource; }

    private String baseSql(String suffix) {
        return "SELECT sa.*, et.code AS event_type_code, et.name AS event_type_name, " +
                "et.workflow_category, s.name AS student_name, s.register_number, " +
                "d.department_name, sec.section_name, s.student_year " +
                "FROM student_achievements sa JOIN event_types et ON et.event_type_id = sa.event_type_id " +
                "JOIN students s ON s.student_id = sa.student_id " +
                "JOIN departments d ON d.department_id = s.department_id " +
                "JOIN sections sec ON sec.section_id = s.section_id " + suffix;
    }

    private StudentAchievement map(ResultSet rs) throws SQLException {
        StudentAchievement value = new StudentAchievement();
        value.setAchievementId(rs.getLong("achievement_id"));
        value.setStudentId(rs.getLong("student_id"));
        value.setEventTypeId(rs.getLong("event_type_id"));
        value.setEventTypeCode(rs.getString("event_type_code"));
        value.setEventTypeName(rs.getString("event_type_name"));
        value.setWorkflowCategory(rs.getString("workflow_category"));
        value.setAcademicYearId(rs.getLong("academic_year_id"));
        if (hasColumn(rs, "achievement_semester")) {
            int semester = rs.getInt("achievement_semester");
            if (!rs.wasNull()) value.setAchievementSemester(semester);
        }
        if (hasColumn(rs, "achievement_student_year")) {
            int studentYear = rs.getInt("achievement_student_year");
            if (!rs.wasNull()) value.setAchievementStudentYear(studentYear);
        }
        value.setAssignedMentorId(rs.getLong("assigned_mentor_id"));
        value.setTitle(rs.getString("title"));
        value.setOrganization(rs.getString("organization"));
        value.setDescription(rs.getString("description"));
        Date startDate = rs.getDate("start_date");
        if (startDate != null) value.setStartDate(startDate.toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) value.setEndDate(endDate.toLocalDate());
        value.setOfficialUrl(rs.getString("official_url"));
        value.setParticipationType(ParticipationType.valueOf(rs.getString("participation_type")));
        String details = rs.getString("type_specific_details");
        if (details != null && !details.isBlank()) {
            try { value.setTypeSpecificDetails(objectMapper.readValue(details, new TypeReference<>() {})); }
            catch (Exception exception) { throw new SQLException("Invalid achievement type-specific JSON", exception); }
        }
        value.setVerificationStatus(rs.getString("verification_status"));
        value.setMentorRemarks(rs.getString("mentor_remarks"));
        value.setHodRemarks(rs.getString("hod_remarks"));
        value.setSubmittedAt(toLocalDateTime(rs.getTimestamp("submitted_at")));
        value.setMentorReviewedAt(toLocalDateTime(rs.getTimestamp("mentor_reviewed_at")));
        value.setHodReviewedAt(toLocalDateTime(rs.getTimestamp("hod_reviewed_at")));
        value.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        value.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        value.setStudentName(rs.getString("student_name"));
        value.setRegistrationNumber(rs.getString("register_number"));
        value.setDepartmentName(rs.getString("department_name"));
        value.setSectionName(rs.getString("section_name"));
        value.setStudentYear(rs.getInt("student_year"));
        return value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) { return timestamp == null ? null : timestamp.toLocalDateTime(); }

    @Override public StudentAchievement create(StudentAchievement value, String teamName, List<Long> teamMemberIds) {
        String sql = "INSERT INTO student_achievements (student_id,event_type_id,academic_year_id,achievement_semester,achievement_student_year,assigned_mentor_id,title,organization,description,start_date,end_date,official_url,participation_type,type_specific_details) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, value.getStudentId());
                statement.setLong(2, value.getEventTypeId());
                statement.setLong(3, value.getAcademicYearId());
                setNullableInteger(statement, 4, value.getAchievementSemester());
                setNullableInteger(statement, 5, value.getAchievementStudentYear());
                statement.setLong(6, value.getAssignedMentorId());
                statement.setString(7, value.getTitle());
                statement.setString(8, value.getOrganization());
                setNullableString(statement, 9, value.getDescription());
                setNullableDate(statement, 10, value.getStartDate());
                setNullableDate(statement, 11, value.getEndDate());
                setNullableString(statement, 12, value.getOfficialUrl());
                statement.setString(13, value.getParticipationType().name());
                if (value.getTypeSpecificDetails() == null || value.getTypeSpecificDetails().isEmpty()) statement.setNull(14, Types.VARCHAR);
                else statement.setString(14, objectMapper.writeValueAsString(value.getTypeSpecificDetails()));
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Achievement id was not generated");
                    value.setAchievementId(keys.getLong(1));
                }
            }
            if (value.getParticipationType() == ParticipationType.TEAM) {
                long teamId;
                try (PreparedStatement team = connection.prepareStatement("INSERT INTO achievement_teams (achievement_id,team_name,leader_student_id) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    team.setLong(1, value.getAchievementId()); team.setString(2, teamName); team.setLong(3, value.getStudentId()); team.executeUpdate();
                    try (ResultSet keys = team.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Achievement team id was not generated"); teamId = keys.getLong(1); }
                }
                try (PreparedStatement member = connection.prepareStatement("INSERT INTO achievement_team_members (achievement_team_id,student_id) VALUES (?,?)")) {
                    for (Long studentId : teamMemberIds) { member.setLong(1, teamId); member.setLong(2, studentId); member.addBatch(); }
                    member.executeBatch();
                }
            }
            connection.commit();
            return findById(value.getAchievementId()).orElseThrow();
        } catch (Exception exception) {
            throw new RuntimeException("Error saving Student achievement", exception);
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) statement.setNull(index, Types.VARCHAR); else statement.setString(index, value.trim());
    }
    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) statement.setNull(index, Types.INTEGER); else statement.setInt(index, value);
    }
    private boolean hasColumn(ResultSet rs, String name) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if (name.equalsIgnoreCase(metadata.getColumnLabel(i)) || name.equalsIgnoreCase(metadata.getColumnName(i))) return true;
        }
        return false;
    }
    private void setNullableDate(PreparedStatement statement, int index, java.time.LocalDate value) throws SQLException {
        if (value == null) statement.setNull(index, Types.DATE); else statement.setDate(index, Date.valueOf(value));
    }

    @Override public Optional<StudentAchievement> findById(Long id) { return one(baseSql("WHERE sa.achievement_id = ?"), id); }
    @Override public List<StudentAchievement> findByStudentId(Long id) { return many(baseSql("WHERE sa.student_id = ? ORDER BY sa.created_at DESC"), id); }
    @Override public List<StudentAchievement> findMentorQueue(Long facultyId) {
        return many(baseSql("JOIN mentor_assignments ma ON ma.faculty_id = sa.assigned_mentor_id " +
                "AND ma.faculty_id = ? AND ma.department_id = s.department_id AND ma.section_id = s.section_id " +
                "AND ma.academic_year_id = s.academic_year_id AND ma.student_year = s.student_year " +
                "AND ma.is_active = TRUE AND ma.active_from <= CURRENT_DATE AND (ma.active_until IS NULL OR ma.active_until >= CURRENT_DATE) " +
                "WHERE sa.verification_status = 'PENDING_MENTOR_VERIFICATION' ORDER BY sa.created_at DESC"), facultyId);
    }
    @Override public List<StudentAchievement> findHodQueue(Long departmentId) {
        return many(baseSql("WHERE s.department_id = ? AND sa.verification_status = 'PENDING_HOD_VERIFICATION' ORDER BY sa.created_at DESC"), departmentId);
    }

    private Optional<StudentAchievement> one(String sql, Long id) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id); try (ResultSet rs = statement.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (Exception exception) { throw new RuntimeException("Error loading Student achievement", exception); }
    }
    private List<StudentAchievement> many(String sql, Long id) {
        List<StudentAchievement> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id); try (ResultSet rs = statement.executeQuery()) { while (rs.next()) result.add(map(rs)); }
            return result;
        } catch (Exception exception) { throw new RuntimeException("Error loading Student achievements", exception); }
    }

    @Override public List<TeamMemberDTO> findTeamMembers(Long achievementId) {
        String sql = "SELECT s.student_id, s.register_number, s.name FROM achievement_teams t " +
                "JOIN achievement_team_members tm ON tm.achievement_team_id = t.achievement_team_id " +
                "JOIN students s ON s.student_id = tm.student_id WHERE t.achievement_id = ? ORDER BY tm.joined_at, tm.achievement_team_member_id";
        List<TeamMemberDTO> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, achievementId); try (ResultSet rs = statement.executeQuery()) { while (rs.next()) result.add(new TeamMemberDTO(rs.getLong(1), rs.getString(2), rs.getString(3))); }
            return result;
        } catch (SQLException exception) { throw new RuntimeException("Error loading achievement team", exception); }
    }

    @Override public boolean isMentorAssigned(Long achievementId, Long facultyId) {
        String sql = "SELECT 1 FROM student_achievements sa JOIN students s ON s.student_id = sa.student_id " +
                "JOIN mentor_assignments ma ON ma.faculty_id = ? AND ma.faculty_id = sa.assigned_mentor_id " +
                "AND ma.department_id = s.department_id AND ma.section_id = s.section_id AND ma.academic_year_id = s.academic_year_id " +
                "AND ma.student_year = s.student_year AND ma.is_active = TRUE AND ma.active_from <= CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until >= CURRENT_DATE) WHERE sa.achievement_id = ?";
        return exists(sql, facultyId, achievementId);
    }
    @Override public boolean isInDepartment(Long achievementId, Long departmentId) {
        return exists("SELECT 1 FROM student_achievements sa JOIN students s ON s.student_id = sa.student_id WHERE sa.achievement_id = ? AND s.department_id = ?", achievementId, departmentId);
    }
    private boolean exists(String sql, Long first, Long second) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, first); statement.setLong(2, second); try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        } catch (SQLException exception) { throw new RuntimeException("Error checking achievement authorization", exception); }
    }

    @Override public boolean updateMentorReview(Long id, String expected, String next, String remarks, Long reviewer) {
        return update("UPDATE student_achievements SET verification_status=?, mentor_remarks=?, mentor_reviewed_by_user_id=?, mentor_reviewed_at=CURRENT_TIMESTAMP WHERE achievement_id=? AND verification_status=?", next, remarks, reviewer, id, expected);
    }
    @Override public boolean updateHodReview(Long id, String expected, String next, String remarks, Long reviewer) {
        return update("UPDATE student_achievements SET verification_status=?, hod_remarks=?, hod_reviewed_by_user_id=?, hod_reviewed_at=CURRENT_TIMESTAMP WHERE achievement_id=? AND verification_status=?", next, remarks, reviewer, id, expected);
    }
    private boolean update(String sql, String next, String remarks, Long reviewer, Long id, String expected) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, next); statement.setString(2, remarks); statement.setLong(3, reviewer); statement.setLong(4, id); statement.setString(5, expected); return statement.executeUpdate() == 1;
        } catch (SQLException exception) { throw new RuntimeException("Error reviewing Student achievement", exception); }
    }
}
