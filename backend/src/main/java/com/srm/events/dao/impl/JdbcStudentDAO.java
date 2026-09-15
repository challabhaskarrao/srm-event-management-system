package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.StudentDAO;
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
}
