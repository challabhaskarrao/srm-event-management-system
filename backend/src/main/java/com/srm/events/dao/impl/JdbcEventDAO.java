package com.srm.events.dao.impl;

import com.srm.events.dao.interfaces.EventDAO;
import com.srm.events.model.Event;
import com.srm.events.model.EventMode;
import com.srm.events.dto.EventCategoryDTO;
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
        if (hasColumn(rs, "event_type_code")) event.setEventTypeCode(rs.getString("event_type_code"));
        if (hasColumn(rs, "event_type_name")) event.setEventTypeName(rs.getString("event_type_name"));
        if (hasColumn(rs, "type_specific_details")) event.setTypeSpecificDetails(rs.getString("type_specific_details"));
        if (hasColumn(rs, "other_category")) event.setOtherCategory(rs.getString("other_category"));
        event.setOrganizer(rs.getString("organizer"));
        event.setStartDate(rs.getDate("start_date").toLocalDate());
        event.setEndDate(rs.getDate("end_date").toLocalDate());
        event.setVenue(rs.getString("venue"));
        if (hasColumn(rs, "city")) event.setCity(rs.getString("city"));
        if (hasColumn(rs, "state")) event.setState(rs.getString("state"));
        if (hasColumn(rs, "country")) event.setCountry(rs.getString("country"));
        event.setMode(EventMode.valueOf(rs.getString("mode")));
        event.setEventUrl(rs.getString("event_url"));
        if (hasColumn(rs, "description")) event.setDescription(rs.getString("description"));
        if (hasColumn(rs, "registration_fee_required")) event.setRegistrationFeeRequired(rs.getBoolean("registration_fee_required"));
        if (hasColumn(rs, "registration_fee_amount")) event.setRegistrationFeeAmount(rs.getBigDecimal("registration_fee_amount"));
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

    private boolean hasColumn(ResultSet rs, String name) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        if (metadata == null) return false;
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if (name.equalsIgnoreCase(metadata.getColumnLabel(i)) || name.equalsIgnoreCase(metadata.getColumnName(i))) return true;
        }
        return false;
    }

    private boolean eventTypesAvailable(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        if (metadata == null) return false;
        try (ResultSet resultSet = metadata.getTables(null, null, "event_types", new String[]{"TABLE"})) { return resultSet.next(); }
    }

    private String eventSelectSql(boolean includeEventTypes, String suffix) {
        String typeColumns = includeEventTypes ? ", et.code AS event_type_code, et.name AS event_type_name" : "";
        String typeJoin = includeEventTypes ? " LEFT JOIN event_types et ON e.event_type_id = et.event_type_id" : "";
        return "SELECT e.*, c.category_name" + typeColumns + ", u.email AS creator_email " +
                "FROM events e JOIN event_categories c ON e.category_id = c.category_id" + typeJoin +
                " JOIN users u ON e.created_by_user_id = u.user_id " + suffix;
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
    public Event update(Event event) {
        String sql = "UPDATE events SET event_name = ?, category_id = ?, organizer = ?, " +
                "start_date = ?, end_date = ?, venue = ?, mode = ?, event_url = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE event_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setLong(2, event.getCategoryId());
            ps.setString(3, event.getOrganizer());
            ps.setDate(4, Date.valueOf(event.getStartDate()));
            ps.setDate(5, Date.valueOf(event.getEndDate()));
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getMode().name());
            ps.setString(8, event.getEventUrl());
            ps.setLong(9, event.getEventId());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Updating event failed, event not found.");
            }
            return event;
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating event", exception);
        }
    }

    @Override
    public boolean categoryExists(Long categoryId) {
        String sql = "SELECT 1 FROM event_categories WHERE category_id = ? AND is_active = TRUE";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error checking event category", exception);
        }
    }

    @Override
    public List<EventCategoryDTO> findActiveCategories() {
        String sql = "SELECT category_id, category_name FROM event_categories WHERE is_active = TRUE ORDER BY category_name";
        List<EventCategoryDTO> categories = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) categories.add(new EventCategoryDTO(rs.getLong(1), rs.getString(2)));
            return categories;
        } catch (SQLException exception) {
            throw new RuntimeException("Error loading event categories", exception);
        }
    }

    @Override
    public Optional<Event> findById(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(eventSelectSql(eventTypesAvailable(conn), "WHERE e.event_id = ?"))) {
            
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
        List<Event> events = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(eventSelectSql(eventTypesAvailable(conn), "ORDER BY e.start_date DESC LIMIT ? OFFSET ?"))) {
            
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
    public List<Event> findAvailable(int limit, int offset) {
        List<Event> events = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(eventSelectSql(eventTypesAvailable(conn), "WHERE e.start_date >= CURRENT_DATE ORDER BY e.start_date ASC LIMIT ? OFFSET ?"))) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching available events", e);
        }
        return events;
    }

    @Override
    public List<Event> findByCategory(Long categoryId) {
        List<Event> events = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(eventSelectSql(eventTypesAvailable(conn), "WHERE e.category_id = ? ORDER BY e.start_date DESC"))) {
            
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
