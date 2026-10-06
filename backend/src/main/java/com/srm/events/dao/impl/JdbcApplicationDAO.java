package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.ApplicationDAO;
import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.ParticipationType;
import com.srm.events.dto.TeamMemberDTO;
import com.srm.events.dto.ApprovalActionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcApplicationDAO implements ApplicationDAO {

    private final DataSource dataSource;

    @Autowired
    public JdbcApplicationDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Application mapRowToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setApplicationId(rs.getLong("application_id"));
        app.setApplicationCode(rs.getString("application_code"));
        app.setEventId(rs.getLong("event_id"));
        app.setCreatedByStudentId(rs.getLong("created_by_student_id"));
        
        long mentorId = rs.getLong("assigned_mentor_id");
        if (!rs.wasNull()) {
            app.setAssignedMentorId(mentorId);
        }
        
        app.setAcademicYearId(rs.getLong("academic_year_id"));
        if (hasColumn(rs, "application_semester")) {
            int value = rs.getInt("application_semester");
            if (!rs.wasNull()) app.setApplicationSemester(value);
        }
        if (hasColumn(rs, "application_student_year")) {
            int value = rs.getInt("application_student_year");
            if (!rs.wasNull()) app.setApplicationStudentYear(value);
        }
        app.setParticipationType(ParticipationType.valueOf(rs.getString("participation_type")));
        app.setTravelRequired(rs.getBoolean("travel_required"));
        app.setOutsideState(rs.getBoolean("outside_state"));
        if (hasColumn(rs, "destination_city")) app.setDestinationCity(rs.getString("destination_city"));
        if (hasColumn(rs, "destination_state")) app.setDestinationState(rs.getString("destination_state"));
        if (hasColumn(rs, "destination_country")) app.setDestinationCountry(rs.getString("destination_country"));
        if (hasColumn(rs, "venue_address")) app.setVenueAddress(rs.getString("venue_address"));
        if (hasColumn(rs, "mode_of_travel")) app.setModeOfTravel(rs.getString("mode_of_travel"));
        if (hasColumn(rs, "departure_at")) { Timestamp value = rs.getTimestamp("departure_at"); if (value != null) app.setDepartureAt(value.toLocalDateTime()); }
        if (hasColumn(rs, "return_at")) { Timestamp value = rs.getTimestamp("return_at"); if (value != null) app.setReturnAt(value.toLocalDateTime()); }
        if (hasColumn(rs, "accommodation_required")) app.setAccommodationRequired(rs.getBoolean("accommodation_required"));
        if (hasColumn(rs, "accommodation_name")) app.setAccommodationName(rs.getString("accommodation_name"));
        if (hasColumn(rs, "accommodation_address")) app.setAccommodationAddress(rs.getString("accommodation_address"));
        if (hasColumn(rs, "check_in_date")) { Date value = rs.getDate("check_in_date"); if (value != null) app.setCheckInDate(value.toLocalDate()); }
        if (hasColumn(rs, "check_out_date")) { Date value = rs.getDate("check_out_date"); if (value != null) app.setCheckOutDate(value.toLocalDate()); }
        if (hasColumn(rs, "emergency_contact_name")) app.setEmergencyContactName(rs.getString("emergency_contact_name"));
        if (hasColumn(rs, "emergency_contact_number")) app.setEmergencyContactNumber(rs.getString("emergency_contact_number"));
        if (hasColumn(rs, "guardian_contact_number")) app.setGuardianContactNumber(rs.getString("guardian_contact_number"));
        if (hasColumn(rs, "travel_remarks")) app.setTravelRemarks(rs.getString("travel_remarks"));
        if (hasColumn(rs, "registration_fee_paid")) app.setRegistrationFeePaid(rs.getBoolean("registration_fee_paid"));
        if (hasColumn(rs, "registration_fee_amount")) app.setRegistrationFeeAmount(rs.getBigDecimal("registration_fee_amount"));
        app.setCurrentStatus(ApplicationStatus.valueOf(rs.getString("current_status")));
        app.setCorrectionReturnStage(rs.getString("correction_return_stage"));
        
        Timestamp submittedAt = rs.getTimestamp("submitted_at");
        if (submittedAt != null) {
            app.setSubmittedAt(submittedAt.toLocalDateTime());
        }
        
        Timestamp mentorReviewedAt = rs.getTimestamp("mentor_reviewed_at");
        if (mentorReviewedAt != null) {
            app.setMentorReviewedAt(mentorReviewedAt.toLocalDateTime());
        }
        
        Timestamp hodReviewedAt = rs.getTimestamp("hod_reviewed_at");
        if (hodReviewedAt != null) {
            app.setHodReviewedAt(hodReviewedAt.toLocalDateTime());
        }
        
        long hodId = rs.getLong("hod_id");
        if (!rs.wasNull()) {
            app.setHodId(hodId);
        }
        app.setMentorRemarks(rs.getString("mentor_remarks"));
        app.setHodRemarks(rs.getString("hod_remarks"));
        if (hasColumn(rs, "participation_status")) app.setParticipationStatus(rs.getString("participation_status"));
        if (hasColumn(rs, "post_event_reported_at")) {
            Timestamp value = rs.getTimestamp("post_event_reported_at");
            if (value != null) app.setPostEventReportedAt(value.toLocalDateTime());
        }
        if (hasColumn(rs, "outcome_notes")) app.setOutcomeNotes(rs.getString("outcome_notes"));
        if (hasColumn(rs, "result_url")) app.setResultUrl(rs.getString("result_url"));

        app.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        app.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return app;
    }

    private boolean hasColumn(ResultSet rs, String name) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        if (metadata == null) return false;
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if (name.equalsIgnoreCase(metadata.getColumnLabel(i)) || name.equalsIgnoreCase(metadata.getColumnName(i))) return true;
        }
        return false;
    }

    @Override
    public Application create(Application app, String teamName, List<Long> teamMemberIds) {
        String appSql = "INSERT INTO applications (application_code, event_id, created_by_student_id, assigned_mentor_id, academic_year_id, application_semester, application_student_year, participation_type, travel_required, outside_state, current_status, submitted_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String teamSql = "INSERT INTO teams (application_id, team_name, leader_student_id) VALUES (?, ?, ?)";
        String teamMemberSql = "INSERT INTO team_members (team_id, student_id) VALUES (?, ?)";

        String appCode = "APP-" + java.time.Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        app.setApplicationCode(appCode);
        
        if (app.getCurrentStatus() == null) {
            app.setCurrentStatus(ApplicationStatus.SUBMITTED);
        }
        
        if (app.getSubmittedAt() == null && app.getCurrentStatus() == ApplicationStatus.SUBMITTED) {
            app.setSubmittedAt(java.time.LocalDateTime.now());
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement appPs = conn.prepareStatement(appSql, Statement.RETURN_GENERATED_KEYS)) {
                appPs.setString(1, app.getApplicationCode());
                appPs.setLong(2, app.getEventId());
                appPs.setLong(3, app.getCreatedByStudentId());
                appPs.setLong(4, app.getAssignedMentorId());
                appPs.setLong(5, app.getAcademicYearId());
                setNullableInteger(appPs, 6, app.getApplicationSemester());
                setNullableInteger(appPs, 7, app.getApplicationStudentYear());
                appPs.setString(8, app.getParticipationType().name());
                appPs.setBoolean(9, app.isTravelRequired());
                appPs.setBoolean(10, app.isOutsideState());
                appPs.setString(11, app.getCurrentStatus().name());
                
                if (app.getSubmittedAt() != null) {
                    appPs.setTimestamp(12, Timestamp.valueOf(app.getSubmittedAt()));
                } else {
                    appPs.setNull(12, Types.TIMESTAMP);
                }
                
                int affectedRows = appPs.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating application failed, no rows affected.");
                }
                
                try (ResultSet generatedKeys = appPs.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        app.setApplicationId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating application failed, no ID obtained.");
                    }
                }
                
                // If TEAM participation, create teams record
                if (app.getParticipationType() == ParticipationType.TEAM && teamName != null) {
                    long teamId = -1;
                    try (PreparedStatement teamPs = conn.prepareStatement(teamSql, Statement.RETURN_GENERATED_KEYS)) {
                        teamPs.setLong(1, app.getApplicationId());
                        teamPs.setString(2, teamName);
                        teamPs.setLong(3, app.getCreatedByStudentId());
                        
                        teamPs.executeUpdate();
                        try (ResultSet teamKeys = teamPs.getGeneratedKeys()) {
                            if (teamKeys.next()) {
                                teamId = teamKeys.getLong(1);
                            }
                        }
                    }
                    
                    if (teamId != -1) {
                        try (PreparedStatement memberPs = conn.prepareStatement(teamMemberSql)) {
                            for (Long studentId : teamMemberIds) {
                                memberPs.setLong(1, teamId);
                                memberPs.setLong(2, studentId);
                                memberPs.addBatch();
                            }
                            memberPs.executeBatch();
                        }
                    }
                }
                
                conn.commit();
                return app;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving application", e);
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) statement.setNull(index, Types.INTEGER); else statement.setInt(index, value);
    }

    @Override
    public Optional<Application> findByCode(String code) {
        String sql = "SELECT * FROM applications WHERE application_code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding application by code", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Application> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM applications WHERE created_by_student_id = ? ORDER BY created_at DESC";
        List<Application> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapRowToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding applications for student", e);
        }
        return applications;
    }

    @Override
    public boolean existsByStudentAndEvent(Long studentId, Long eventId) {
        String sql = "SELECT 1 FROM applications WHERE created_by_student_id = ? AND event_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking application existence", e);
        }
    }

    @Override
    public List<Application> findMentorReviewQueue(Long facultyId) {
        String sql = "SELECT DISTINCT a.* FROM applications a " +
                "JOIN students s ON s.student_id = a.created_by_student_id " +
                "JOIN mentor_assignments ma ON ma.faculty_id = ? " +
                "AND ma.department_id = s.department_id " +
                "AND ma.section_id = s.section_id " +
                "AND ma.academic_year_id = s.academic_year_id " +
                "AND ma.student_year = s.student_year " +
                "AND ma.is_active = TRUE " +
                "AND ma.active_from <= CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until >= CURRENT_DATE) " +
                "WHERE a.current_status = ? " +
                "AND (a.assigned_mentor_id IS NULL OR a.assigned_mentor_id = ?) " +
                "ORDER BY a.created_at DESC";
        List<Application> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, facultyId);
            ps.setString(2, ApplicationStatus.SUBMITTED.name());
            ps.setLong(3, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapRowToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Mentor review queue", e);
        }
        return applications;
    }

    @Override
    public Optional<String> findTeamNameByApplicationId(Long applicationId) {
        String sql = "SELECT team_name FROM teams WHERE application_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("team_name")) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding application team", exception);
        }
    }

    @Override
    public List<TeamMemberDTO> findTeamMembersByApplicationId(Long applicationId) {
        String sql = "SELECT s.student_id, s.register_number, s.name FROM teams t " +
                "JOIN team_members tm ON tm.team_id = t.team_id " +
                "JOIN students s ON s.student_id = tm.student_id " +
                "WHERE t.application_id = ? ORDER BY tm.joined_at, tm.team_member_id";
        List<TeamMemberDTO> members = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new TeamMemberDTO(rs.getLong("student_id"),
                            rs.getString("register_number"), rs.getString("name")));
                }
            }
            return members;
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding application team members", exception);
        }
    }

    @Override
    public List<Long> findActiveMentorIdsForStudent(Long studentId) {
        String sql = "SELECT DISTINCT ma.faculty_id FROM students s " +
                "JOIN mentor_assignments ma ON ma.department_id = s.department_id " +
                "AND ma.section_id = s.section_id " +
                "AND ma.academic_year_id = s.academic_year_id " +
                "AND ma.student_year = s.student_year " +
                "AND ma.is_active = TRUE " +
                "AND ma.active_from <= CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until >= CURRENT_DATE) " +
                "JOIN faculty f ON f.faculty_id = ma.faculty_id AND f.is_active = TRUE " +
                "WHERE s.student_id = ? ORDER BY ma.faculty_id";
        List<Long> mentorIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mentorIds.add(rs.getLong("faculty_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error resolving active Mentor assignment", e);
        }
        return mentorIds;
    }

    @Override
    public List<Application> findHodReviewQueue(Long departmentId) {
        String sql = "SELECT a.* FROM applications a " +
                "JOIN students s ON s.student_id = a.created_by_student_id " +
                "WHERE a.current_status = ? AND s.department_id = ? " +
                "ORDER BY a.created_at DESC";
        List<Application> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ApplicationStatus.PENDING_HOD_APPROVAL.name());
            ps.setLong(2, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapRowToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding HOD review queue", e);
        }
        return applications;
    }

    @Override
    public List<Application> findAll() {
        String sql = "SELECT * FROM applications ORDER BY created_at DESC";
        List<Application> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                applications.add(mapRowToApplication(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all applications", e);
        }
        return applications;
    }

    @Override
    public Optional<Application> findById(Long id) {
        String sql = "SELECT * FROM applications WHERE application_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToApplication(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding application by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean isMentorAssignedToApplication(Long applicationId, Long facultyId) {
        String sql = "SELECT 1 FROM applications a " +
                "JOIN students s ON s.student_id = a.created_by_student_id " +
                "JOIN mentor_assignments ma ON ma.faculty_id = ? " +
                "AND ma.department_id = s.department_id " +
                "AND ma.section_id = s.section_id " +
                "AND ma.academic_year_id = s.academic_year_id " +
                "AND ma.student_year = s.student_year " +
                "AND ma.is_active = TRUE " +
                "AND ma.active_from <= CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until >= CURRENT_DATE) " +
                "WHERE a.application_id = ? " +
                "AND (a.assigned_mentor_id IS NULL OR a.assigned_mentor_id = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, facultyId);
            ps.setLong(2, applicationId);
            ps.setLong(3, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking Mentor assignment", e);
        }
    }

    @Override
    public boolean isApplicationInDepartment(Long applicationId, Long departmentId) {
        String sql = "SELECT 1 FROM applications a " +
                "JOIN students s ON s.student_id = a.created_by_student_id " +
                "WHERE a.application_id = ? AND s.department_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            ps.setLong(2, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking application department", e);
        }
    }

    @Override
    public boolean updateMentorReview(Long applicationId, ApplicationStatus expectedStatus,
                                      ApplicationStatus newStatus, String action, String remarks,
                                      Long facultyId, Long approverUserId) {
        String sql = "UPDATE applications SET current_status = ?, mentor_remarks = ?, " +
                "assigned_mentor_id = ?, mentor_reviewed_at = CURRENT_TIMESTAMP, " +
                "updated_at = CURRENT_TIMESTAMP WHERE application_id = ? AND current_status = ?";
        return executeReviewUpdate(sql, applicationId, expectedStatus, newStatus, action, remarks,
                facultyId, approverUserId, "MENTOR");
    }

    @Override
    public boolean updateHodReview(Long applicationId, ApplicationStatus expectedStatus,
                                   ApplicationStatus newStatus, String action, String remarks,
                                   Long facultyId, Long approverUserId) {
        String sql = "UPDATE applications SET current_status = ?, hod_remarks = ?, " +
                "hod_id = ?, hod_reviewed_at = CURRENT_TIMESTAMP, " +
                "updated_at = CURRENT_TIMESTAMP WHERE application_id = ? AND current_status = ?";
        return executeReviewUpdate(sql, applicationId, expectedStatus, newStatus, action, remarks,
                facultyId, approverUserId, "HOD");
    }

    @Override
    public List<ApprovalActionDTO> findApprovalHistoryByApplicationId(Long applicationId) {
        String sql = "SELECT approval_action_id, approval_level, action, previous_status, " +
                "new_status, remarks, action_at FROM approval_actions " +
                "WHERE application_id = ? ORDER BY action_at, approval_action_id";
        List<ApprovalActionDTO> history = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp actionAt = rs.getTimestamp("action_at");
                    history.add(new ApprovalActionDTO(
                            rs.getLong("approval_action_id"),
                            rs.getString("approval_level"),
                            rs.getString("action"),
                            rs.getString("previous_status"),
                            rs.getString("new_status"),
                            rs.getString("remarks"),
                            actionAt == null ? null : actionAt.toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding application approval history", e);
        }
        return history;
    }

    @Override
    public List<Application> findPostEventPendingForMentor(Long facultyId) {
        String sql = "SELECT DISTINCT a.* FROM applications a " +
                "JOIN students s ON s.student_id = a.created_by_student_id " +
                "JOIN events e ON e.event_id = a.event_id " +
                "JOIN mentor_assignments ma ON ma.faculty_id = ? " +
                "AND ma.department_id = s.department_id AND ma.section_id = s.section_id " +
                "AND ma.academic_year_id = s.academic_year_id AND ma.student_year = s.student_year " +
                "AND ma.is_active = TRUE AND ma.active_from <= CURRENT_DATE " +
                "AND (ma.active_until IS NULL OR ma.active_until >= CURRENT_DATE) " +
                "WHERE a.current_status = 'APPROVED' AND e.end_date < CURRENT_DATE " +
                "AND (a.assigned_mentor_id IS NULL OR a.assigned_mentor_id = ?) " +
                "AND (a.participation_status IS NULL OR a.participation_status = 'NOT_YET_OCCURRED' " +
                "OR (a.participation_status = 'YES' AND NOT EXISTS " +
                "(SELECT 1 FROM achievements ac WHERE ac.application_id = a.application_id)) " +
                "OR (a.participation_status = 'NO' AND NULLIF(TRIM(a.outcome_notes), '') IS NULL)) " +
                "ORDER BY e.end_date, a.application_id";
        return findPostEventPending(sql, facultyId, facultyId, "Error finding Mentor post-event queue");
    }

    @Override
    public List<Application> findPostEventPendingForHod(Long departmentId) {
        String sql = "SELECT a.* FROM applications a " +
                "JOIN students s ON s.student_id = a.created_by_student_id " +
                "JOIN events e ON e.event_id = a.event_id " +
                "WHERE a.current_status = 'APPROVED' AND s.department_id = ? " +
                "AND e.end_date < CURRENT_DATE " +
                "AND (a.participation_status IS NULL OR a.participation_status = 'NOT_YET_OCCURRED' " +
                "OR (a.participation_status = 'YES' AND NOT EXISTS " +
                "(SELECT 1 FROM achievements ac WHERE ac.application_id = a.application_id)) " +
                "OR (a.participation_status = 'NO' AND NULLIF(TRIM(a.outcome_notes), '') IS NULL)) " +
                "ORDER BY e.end_date, a.application_id";
        return findPostEventPending(sql, departmentId, null, "Error finding HOD post-event queue");
    }

    private List<Application> findPostEventPending(String sql, Long first, Long second, String message) {
        List<Application> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, first);
            if (second != null) ps.setLong(2, second);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) applications.add(mapRowToApplication(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(message, e);
        }
        return applications;
    }

    private boolean executeReviewUpdate(String sql, Long applicationId, ApplicationStatus expectedStatus,
                                        ApplicationStatus newStatus, String action, String remarks,
                                        Long facultyId, Long approverUserId, String approvalLevel) {
        String actionSql = "INSERT INTO approval_actions " +
                "(application_id, approver_user_id, approval_level, action, previous_status, new_status, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, newStatus.name());
                    ps.setString(2, remarks);
                    ps.setLong(3, facultyId);
                    ps.setLong(4, applicationId);
                    ps.setString(5, expectedStatus.name());
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement actionStatement = conn.prepareStatement(actionSql)) {
                    actionStatement.setLong(1, applicationId);
                    actionStatement.setLong(2, approverUserId);
                    actionStatement.setString(3, approvalLevel);
                    actionStatement.setString(4, action);
                    actionStatement.setString(5, expectedStatus.name());
                    actionStatement.setString(6, newStatus.name());
                    actionStatement.setString(7, remarks);
                    actionStatement.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating application review", e);
        }
    }
}
