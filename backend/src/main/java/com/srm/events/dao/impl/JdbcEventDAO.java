package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.model.Event;
import com.srm.events.model.EventMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEventDAO implements EventDAO {

    private final DataSource dataSource;

    @Autowired
    public JdbcEventDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setEventName(rs.getString("event_name"));
        event.setCategoryId(rs.getLong("category_id"));
        event.setCategoryName(rs.getString("category_name"));
        event.setOrganizer(rs.getString("organizer"));
        event.setStartDate(rs.getDate("start_date").toLocalDate());
        event.setEndDate(rs.getDate("end_date").toLocalDate());
        event.setVenue(rs.getString("venue"));
        event.setMode(EventMode.valueOf(rs.getString("mode")));
        event.setEventUrl(rs.getString("event_url"));
        event.setCreatedByUserId(rs.getLong("created_by_user_id"));
        
        // Sometimes we join with users, but the prompt only strictly mentioned event_categories join.
        // We will map createdByEmail if it exists in the ResultSet.
        try {
            String email = rs.getString("creator_email");
            event.setCreatedByEmail(email);
        } catch (SQLException ignore) {
            // Column might not exist in all queries
        }

        event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        event.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return event;
    }

    @Override
    public Event create(Event event) {
        String sql = "INSERT INTO events (event_name, category_id, organizer, start_date, end_date, venue, mode, event_url, created_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, event.getEventName());
            ps.setLong(2, event.getCategoryId());
            ps.setString(3, event.getOrganizer());
            ps.setDate(4, Date.valueOf(event.getStartDate()));
            ps.setDate(5, Date.valueOf(event.getEndDate()));
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getMode().name());
            ps.setString(8, event.getEventUrl());
            ps.setLong(9, event.getCreatedByUserId());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating event failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setEventId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating event failed, no ID obtained.");
                }
            }
            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving event", e);
        }
    }

    @Override
    public Optional<Event> findById(Long id) {
        String sql = "SELECT e.*, c.category_name, u.email AS creator_email " +
                     "FROM events e " +
                     "JOIN event_categories c ON e.category_id = c.category_id " +
                     "JOIN users u ON e.created_by_user_id = u.user_id " +
                     "WHERE e.event_id = ?";
                     
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding event by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Event> findAll(int limit, int offset) {
        String sql = "SELECT e.*, c.category_name, u.email AS creator_email " +
                     "FROM events e " +
                     "JOIN event_categories c ON e.category_id = c.category_id " +
                     "JOIN users u ON e.created_by_user_id = u.user_id " +
                     "ORDER BY e.start_date DESC LIMIT ? OFFSET ?";
                     
        List<Event> events = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all events", e);
        }
        return events;
    }

    @Override
    public List<Event> findByCategory(Long categoryId) {
        String sql = "SELECT e.*, c.category_name, u.email AS creator_email " +
                     "FROM events e " +
                     "JOIN event_categories c ON e.category_id = c.category_id " +
                     "JOIN users u ON e.created_by_user_id = u.user_id " +
                     "WHERE e.category_id = ? " +
                     "ORDER BY e.start_date DESC";
                     
        List<Event> events = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching events by category", e);
        }
        return events;
    }
}
