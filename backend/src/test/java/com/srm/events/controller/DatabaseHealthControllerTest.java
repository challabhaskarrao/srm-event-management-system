package com.srm.events.controller;

import com.srm.events.dao.interfaces.DatabaseHealthDAO;
import com.srm.events.exception.DatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DatabaseHealthControllerTest {

    @Test
    void reportsUpWhenDatabaseQuerySucceeds() {
        DatabaseHealthController controller = new DatabaseHealthController(() -> true);

        ResponseEntity<?> response = controller.databaseHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        DatabaseHealthController.DatabaseHealthResponse body = assertInstanceOf(
                DatabaseHealthController.DatabaseHealthResponse.class,
                response.getBody()
        );
        assertEquals("UP", body.status());
        assertEquals("MySQL", body.database());
    }

    @Test
    void reportsServiceUnavailableWithoutSensitiveDetails() {
        DatabaseHealthDAO unavailableDatabase = () -> {
            throw new DatabaseConnectionException("internal detail", new SQLException("secret detail"));
        };
        DatabaseHealthController controller = new DatabaseHealthController(unavailableDatabase);

        ResponseEntity<?> response = controller.databaseHealth();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        DatabaseHealthController.DatabaseHealthFailureResponse body = assertInstanceOf(
                DatabaseHealthController.DatabaseHealthFailureResponse.class,
                response.getBody()
        );
        assertEquals("DOWN", body.status());
        assertEquals("MySQL", body.database());
        assertEquals("Database is unavailable or not configured", body.message());
    }
}
