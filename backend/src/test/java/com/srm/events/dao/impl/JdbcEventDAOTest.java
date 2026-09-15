package com.srm.events.dao.impl;

import com.srm.events.model.Event;
import com.srm.events.model.EventMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcEventDAOTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private JdbcEventDAO jdbcEventDAO;

    @Test
    void create_shouldReturnEventWithGeneratedId() throws SQLException {
        // Arrange
        Event event = new Event();
        event.setEventName("Test Event");
        event.setCategoryId(1L);
        event.setOrganizer("Organizer");
        event.setStartDate(LocalDate.now());
        event.setEndDate(LocalDate.now().plusDays(1));
        event.setVenue("Online");
        event.setMode(EventMode.ONLINE);
        event.setEventUrl("http://example.com");
        event.setCreatedByUserId(2L);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(100L);

        // Act
        Event createdEvent = jdbcEventDAO.create(event);

        // Assert
        assertNotNull(createdEvent);
        assertEquals(100L, createdEvent.getEventId());
        
        verify(preparedStatement).setString(1, "Test Event");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void findById_shouldReturnEvent() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getLong("event_id")).thenReturn(100L);
        when(resultSet.getString("event_name")).thenReturn("Test Event");
        when(resultSet.getLong("category_id")).thenReturn(1L);
        when(resultSet.getString("category_name")).thenReturn("Category");
        when(resultSet.getString("organizer")).thenReturn("Organizer");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(resultSet.getString("venue")).thenReturn("Online");
        when(resultSet.getString("mode")).thenReturn(EventMode.ONLINE.name());
        when(resultSet.getString("event_url")).thenReturn("http://example.com");
        when(resultSet.getLong("created_by_user_id")).thenReturn(2L);
        when(resultSet.getString("creator_email")).thenReturn("creator@example.com");
        when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

        // Act
        Optional<Event> eventOpt = jdbcEventDAO.findById(100L);

        // Assert
        assertTrue(eventOpt.isPresent());
        assertEquals("Test Event", eventOpt.get().getEventName());
        assertEquals("creator@example.com", eventOpt.get().getCreatedByEmail());
    }
}
