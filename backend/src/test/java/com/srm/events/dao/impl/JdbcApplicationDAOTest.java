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
import java.util.List;
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
        app.setAssignedMentorId(20L);
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
        Application createdApp = jdbcApplicationDAO.create(app, null, List.of());

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
        app.setAssignedMentorId(20L);
        app.setAcademicYearId(3L);
        app.setParticipationType(ParticipationType.INDIVIDUAL);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("DB Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jdbcApplicationDAO.create(app, null, List.of()));

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

    @Test
    void updateMentorReview_shouldUseExpectedStatusForAtomicTransition() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement, teamPreparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(teamPreparedStatement.executeUpdate()).thenReturn(1);

        boolean updated = jdbcApplicationDAO.updateMentorReview(
                10L, ApplicationStatus.SUBMITTED, ApplicationStatus.PENDING_HOD_APPROVAL,
                "RECOMMENDED", "Reviewed", 20L, 200L);

        assertTrue(updated);
        verify(preparedStatement).setString(1, ApplicationStatus.PENDING_HOD_APPROVAL.name());
        verify(preparedStatement).setLong(3, 20L);
        verify(preparedStatement).setLong(4, 10L);
        verify(preparedStatement).setString(5, ApplicationStatus.SUBMITTED.name());
        verify(teamPreparedStatement).setLong(2, 200L);
        verify(teamPreparedStatement).setString(3, "MENTOR");
        verify(teamPreparedStatement).setString(4, "RECOMMENDED");
        verify(connection).commit();
    }

    @Test
    void updateHodReview_shouldReportConcurrentStateChange() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean updated = jdbcApplicationDAO.updateHodReview(
                10L, ApplicationStatus.PENDING_HOD_APPROVAL, ApplicationStatus.APPROVED,
                "APPROVED", null, 30L, 300L);

        assertFalse(updated);
        verify(connection).rollback();
    }
}
