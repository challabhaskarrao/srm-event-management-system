package com.srm.events.controller;

import com.srm.events.dao.interfaces.NotificationRecipientDAO;
import com.srm.events.dto.CreateParticipationRequestDTO;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationControllerTest {
    @Mock DataSource dataSource;
    @Mock JwtTokenProvider jwt;
    @Mock NotificationService notificationService;
    @Mock NotificationRecipientDAO notificationRecipients;
    @Mock Connection transaction;
    @Mock Connection notificationConnection;
    @Mock PreparedStatement studentQuery;
    @Mock PreparedStatement eventTypeQuery;
    @Mock PreparedStatement categoryQuery;
    @Mock PreparedStatement eventInsert;
    @Mock PreparedStatement routingQuery;
    @Mock ResultSet studentRow;
    @Mock ResultSet eventTypeRow;
    @Mock ResultSet categoryRow;
    @Mock ResultSet eventKeys;
    @Mock ResultSet applicationKeys;
    @Mock ResultSet routingRow;

    @Test
    void createBindsEveryCurrentApplicationColumnAndRoutesMentorNotification() throws Exception {
        BindingRecorder recorder = new BindingRecorder(applicationKeys);
        PreparedStatement applicationInsert = recorder.proxy();

        when(dataSource.getConnection()).thenReturn(transaction, notificationConnection);
        when(jwt.validateToken("student-token")).thenReturn(true);
        when(jwt.getRoleFromToken("student-token")).thenReturn("STUDENT");
        when(jwt.getUserIdFromToken("student-token")).thenReturn(100L);

        when(transaction.prepareStatement(anyString())).thenReturn(studentQuery, eventTypeQuery, categoryQuery);
        when(transaction.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("INSERT INTO applications")) {
                recorder.placeholderCount = (int) sql.chars().filter(character -> character == '?').count();
                return applicationInsert;
            }
            return eventInsert;
        });
        when(notificationConnection.prepareStatement(anyString())).thenReturn(routingQuery);

        when(studentQuery.executeQuery()).thenReturn(studentRow);
        when(studentRow.next()).thenReturn(true);
        when(studentRow.getLong("student_id")).thenReturn(2L);
        when(studentRow.getLong("academic_year_id")).thenReturn(3L);
        when(studentRow.getInt("semester")).thenReturn(5);
        when(studentRow.getInt("student_year")).thenReturn(2);
        when(studentRow.getString("register_number")).thenReturn("REG-001");

        when(eventTypeQuery.executeQuery()).thenReturn(eventTypeRow);
        when(eventTypeRow.next()).thenReturn(true);
        when(eventTypeRow.getLong("event_type_id")).thenReturn(4L);
        when(eventTypeRow.getString("name")).thenReturn("Hackathon");

        when(categoryQuery.executeQuery()).thenReturn(categoryRow);
        when(categoryRow.next()).thenReturn(true);
        when(categoryRow.getLong(1)).thenReturn(9L);

        when(eventInsert.executeUpdate()).thenReturn(1);
        when(eventInsert.getGeneratedKeys()).thenReturn(eventKeys);
        when(eventKeys.next()).thenReturn(true);
        when(eventKeys.getLong(1)).thenReturn(10L);

        when(applicationKeys.next()).thenReturn(true);
        when(applicationKeys.getLong(1)).thenReturn(11L);

        when(routingQuery.executeQuery()).thenReturn(routingRow);
        when(routingRow.next()).thenReturn(true);
        when(routingRow.getLong("assigned_mentor_id")).thenReturn(20L);
        when(routingRow.wasNull()).thenReturn(false);
        when(routingRow.getString("name")).thenReturn("Demo Student");
        when(notificationRecipients.findUserIdByFacultyId(20L)).thenReturn(Optional.of(300L));

        ParticipationController controller = new ParticipationController(dataSource, jwt, notificationService, notificationRecipients);
        var response = controller.create("Bearer student-token", requestWithOptionalFieldsAbsent());

        assertEquals(201, response.getStatusCode().value());
        assertEquals(11L, response.getBody().get("applicationId"));
        assertEquals("SUBMITTED", response.getBody().get("approvalStatus"));
        assertEquals(29, recorder.placeholderCount);
        assertEquals(29, recorder.boundIndexes.size());
        assertEquals(5, recorder.values.get(5));
        assertEquals(2, recorder.values.get(6));
        assertTrue(recorder.nullIndexes.containsAll(Set.of(10, 11, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 27)));
        verify(notificationService).notifyOnce(300L, "APPLICATION_SUBMITTED", "New activity application",
                "New activity application from Demo Student requires your review.", "APPLICATION", 11L,
                "/staff/mentor", "APPLICATION_SUBMITTED:11:SUBMITTED");
        verify(notificationRecipients).findUserIdByFacultyId(20L);
        verify(transaction).commit();
    }

    private CreateParticipationRequestDTO requestWithOptionalFieldsAbsent() {
        return new CreateParticipationRequestDTO(
                "Hackathon", // eventName
                "HACKATHON", // eventTypeCode
                9L, // categoryId
                "SRM", // organizer
                LocalDate.of(2026, 10, 10), // startDate
                LocalDate.of(2026, 10, 11), // endDate
                "ONLINE", // mode
                null, // venue
                null, // eventUrl
                null, // description
                com.srm.events.model.ParticipationType.INDIVIDUAL, // participationType
                false, // travelRequired
                false, // outsideState
                null, // teamName
                List.<String>of(), // teamMemberRegistrationNumbers
                null, // otherCategory
                null, // city
                null, // state
                "India", // country
                null, // venueAddress
                null, // modeOfTravel
                null, // departureAt
                null, // returnAt
                false, // accommodationRequired
                null, // accommodationName
                null, // accommodationAddress
                null, // checkInDate
                null, // checkOutDate
                null, // emergencyContactName
                null, // emergencyContactNumber
                null, // guardianContactNumber
                null, // travelRemarks
                false, // registrationFeeRequired
                null, // registrationFeeAmount
                true, // declarationAccepted
                Map.<String, String>of()); // typeSpecificDetails
    }

    private static final class BindingRecorder implements InvocationHandler {
        private final ResultSet generatedKeys;
        private final Map<Integer, Object> values = new HashMap<>();
        private final Set<Integer> boundIndexes = new HashSet<>();
        private final Set<Integer> nullIndexes = new HashSet<>();
        private int placeholderCount;

        private BindingRecorder(ResultSet generatedKeys) {
            this.generatedKeys = generatedKeys;
        }

        private PreparedStatement proxy() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(), new Class[]{PreparedStatement.class}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.startsWith("set") && args != null && args.length > 0 && args[0] instanceof Integer index) {
                boundIndexes.add(index);
                Object value = args.length > 1 ? args[1] : null;
                values.put(index, value);
                if ("setNull".equals(name)) nullIndexes.add(index);
                return null;
            }
            if ("executeUpdate".equals(name)) {
                for (int index = 1; index <= placeholderCount; index++) {
                    if (!boundIndexes.contains(index)) throw new SQLException("No value specified for parameter " + index);
                }
                return 1;
            }
            if ("getGeneratedKeys".equals(name)) return generatedKeys;
            if ("close".equals(name)) return null;
            if ("isClosed".equals(name)) return false;
            return defaultValue(method.getReturnType());
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) return null;
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0F;
            if (type == double.class) return 0D;
            if (type == char.class) return '\0';
            return null;
        }
    }
}
