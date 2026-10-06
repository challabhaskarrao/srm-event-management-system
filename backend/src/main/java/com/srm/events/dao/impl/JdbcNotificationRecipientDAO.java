package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.NotificationRecipientDAO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcNotificationRecipientDAO implements NotificationRecipientDAO {
    private final JdbcTemplate jdbc;
    public JdbcNotificationRecipientDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public Optional<Long> findUserIdByFacultyId(Long facultyId) { return one("SELECT user_id FROM faculty WHERE faculty_id=? AND is_active=TRUE", facultyId); }
    @Override public Optional<Long> findHodUserIdByDepartmentId(Long departmentId) { return one("SELECT f.user_id FROM faculty f JOIN users u ON u.user_id=f.user_id JOIN roles r ON r.role_id=u.role_id WHERE f.department_id=? AND f.is_active=TRUE AND u.is_active=TRUE AND r.role_name='HOD' ORDER BY f.faculty_id LIMIT 1", departmentId); }
    private Optional<Long> one(String sql, Long value) { return jdbc.query(sql, rs -> rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty(), value); }
}
