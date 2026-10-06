package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.StudentDAO;
import com.srm.events.dto.StudentProfileDTO;
import com.srm.events.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcStudentDAO implements StudentDAO {

    private final DataSource dataSource;

    @Autowired
    public JdbcStudentDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getLong("student_id"));
        student.setUserId(rs.getLong("user_id"));
        student.setRegisterNumber(rs.getString("register_number"));
        student.setName(rs.getString("name"));
        student.setDepartmentId(rs.getLong("department_id"));
        student.setSectionId(rs.getLong("section_id"));
        student.setAcademicYearId(rs.getLong("academic_year_id"));
        student.setStudentYear(rs.getInt("student_year"));
        student.setSemester(rs.getInt("semester"));
        student.setBatch(rs.getString("batch"));
        student.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        student.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return student;
    }

    @Override
    public Optional<Student> findByUserId(Long userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding student by user_id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findById(Long studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding student by student_id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findByRegistrationNumber(String registrationNumber) {
        String sql = "SELECT * FROM students WHERE UPPER(register_number) = UPPER(?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registrationNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToStudent(rs)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding student by registration number", exception);
        }
    }

    @Override
    public Optional<StudentProfileDTO> findProfileByUserId(Long userId) {
        String sql = "SELECT s.student_id, s.register_number, s.name, u.email, " +
                "d.department_code, d.department_name, sec.section_name, " +
                "ay.year_label, s.student_year, s.semester, s.batch " +
                "FROM students s " +
                "JOIN users u ON u.user_id = s.user_id " +
                "JOIN departments d ON d.department_id = s.department_id " +
                "JOIN sections sec ON sec.section_id = s.section_id " +
                "JOIN academic_years ay ON ay.academic_year_id = s.academic_year_id " +
                "WHERE s.user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                StudentProfileDTO profile = new StudentProfileDTO();
                profile.setStudentId(rs.getLong("student_id"));
                profile.setRegistrationNumber(rs.getString("register_number"));
                profile.setName(rs.getString("name"));
                profile.setEmail(rs.getString("email"));
                profile.setDepartmentCode(rs.getString("department_code"));
                profile.setDepartmentName(rs.getString("department_name"));
                profile.setSectionName(rs.getString("section_name"));
                profile.setAcademicYear(rs.getString("year_label"));
                profile.setStudentYear(rs.getInt("student_year"));
                profile.setSemester(rs.getInt("semester"));
                profile.setBatch(rs.getString("batch"));
                return Optional.of(profile);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading Student profile", e);
        }
    }
}
