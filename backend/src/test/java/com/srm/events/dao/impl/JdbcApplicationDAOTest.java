package com.srm.events.dao.impl;

import com.srm.events.model.Application;
import com.srm.events.model.ApplicationStatus;
import com.srm.events.model.ParticipationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcApplicationDAOTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private PreparedStatement teamPreparedStatement;

    @Mock
    private PreparedStatement memberPreparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private JdbcApplicationDAO jdbcApplicationDAO;

    @Test
    void create_shouldReturnApplication_andCommitTransaction() throws SQLException {
        // Arrange
        Application app = new Application();
        app.setEventId(1L);
        app.setCreatedByStudentId(2L);
        app.setAcademicYearId(3L);
        app.setParticipationType(ParticipationType.INDIVIDUAL);
        app.setTravelRequired(false);
        app.setOutsideState(false);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(100L);

        // Act
        Application createdApp = jdbcApplicationDAO.create(app, null);

        // Assert
        assertNotNull(createdApp);
        assertEquals(100L, createdApp.getApplicationId());
        assertNotNull(createdApp.getApplicationCode());
        assertEquals(ApplicationStatus.SUBMITTED, createdApp.getCurrentStatus());

        verify(connection).setAutoCommit(false);
        verify(preparedStatement).executeUpdate();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    void create_shouldRollback_whenSQLExceptionOccurs() throws SQLException {
        // Arrange
        Application app = new Application();
        app.setEventId(1L);
        app.setCreatedByStudentId(2L);
        app.setAcademicYearId(3L);
        app.setParticipationType(ParticipationType.INDIVIDUAL);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("DB Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jdbcApplicationDAO.create(app, null));

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }

    @Test
    void existsByStudentAndEvent_shouldReturnTrue_whenExists() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // Act
        boolean exists = jdbcApplicationDAO.existsByStudentAndEvent(2L, 1L);

        // Assert
        assertTrue(exists);
    }
}
