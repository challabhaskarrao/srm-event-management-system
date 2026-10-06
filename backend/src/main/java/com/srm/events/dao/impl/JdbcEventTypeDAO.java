package com.srm.events.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.events.dao.interfaces.EventTypeDAO;
import com.srm.events.model.EventTypeDefinition;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcEventTypeDAO implements EventTypeDAO {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public JdbcEventTypeDAO(DataSource dataSource) { this.dataSource = dataSource; }

    private EventTypeDefinition map(ResultSet rs) throws Exception {
        Map<String, Object> config = objectMapper.readValue(rs.getString("config_json"), new TypeReference<>() {});
        return new EventTypeDefinition(rs.getLong("event_type_id"), rs.getString("code"), rs.getString("name"),
                rs.getInt("sort_order"), rs.getString("workflow_category"), config);
    }

    private String sql(String suffix) {
        return "SELECT event_type_id, code, name, sort_order, workflow_category, config_json " +
                "FROM event_types WHERE is_active = TRUE " + suffix;
    }

    @Override public List<EventTypeDefinition> findActive() {
        List<EventTypeDefinition> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql("ORDER BY sort_order, name")); ResultSet r = p.executeQuery()) {
            while (r.next()) result.add(map(r));
            return result;
        } catch (Exception e) { throw new RuntimeException("Error loading event types", e); }
    }

    @Override public Optional<EventTypeDefinition> findActiveByCode(String code) {
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql("AND code = ?"))) {
            p.setString(1, code);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        } catch (Exception e) { throw new RuntimeException("Error loading event type", e); }
    }
}
