package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.model.Faculty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcFacultyDAO implements FacultyDAO {

    private final DataSource dataSource;

    @Autowired
    public JdbcFacultyDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Faculty> findActiveByUserId(Long userId) {
        String sql = "SELECT faculty_id, user_id, department_id, is_active " +
                "FROM faculty WHERE user_id = ? AND is_active = TRUE";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Faculty faculty = new Faculty();
                faculty.setFacultyId(resultSet.getLong("faculty_id"));
                faculty.setUserId(resultSet.getLong("user_id"));
                faculty.setDepartmentId(resultSet.getLong("department_id"));
                faculty.setActive(resultSet.getBoolean("is_active"));
                return Optional.of(faculty);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding faculty profile", exception);
        }
    }
}
