package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.ApplicationDAO;
import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.ParticipationType;
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
        app.setParticipationType(ParticipationType.valueOf(rs.getString("participation_type")));
        app.setTravelRequired(rs.getBoolean("travel_required"));
        app.setOutsideState(rs.getBoolean("outside_state"));
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
        
        app.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        app.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return app;
    }

    @Override
    public Application create(Application app, String teamName) {
        String appSql = "INSERT INTO applications (application_code, event_id, created_by_student_id, academic_year_id, participation_type, travel_required, outside_state, current_status, submitted_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
                appPs.setLong(4, app.getAcademicYearId());
                appPs.setString(5, app.getParticipationType().name());
                appPs.setBoolean(6, app.isTravelRequired());
                appPs.setBoolean(7, app.isOutsideState());
                appPs.setString(8, app.getCurrentStatus().name());
                
                if (app.getSubmittedAt() != null) {
                    appPs.setTimestamp(9, Timestamp.valueOf(app.getSubmittedAt()));
                } else {
                    appPs.setNull(9, Types.TIMESTAMP);
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
                            memberPs.setLong(1, teamId);
                            memberPs.setLong(2, app.getCreatedByStudentId());
                            memberPs.executeUpdate();
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
}
