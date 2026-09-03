package com.srm.events.controller;

import com.srm.events.dao.interfaces.DatabaseHealthDAO;
import com.srm.events.exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHealthController.class);
    private static final String DATABASE_NAME = "MySQL";

    private final DatabaseHealthDAO databaseHealthDAO;

    public DatabaseHealthController(DatabaseHealthDAO databaseHealthDAO) {
        this.databaseHealthDAO = databaseHealthDAO;
    }

    @GetMapping("/database")
    public ResponseEntity<?> databaseHealth() {
        try {
            if (databaseHealthDAO.isDatabaseAvailable()) {
                return ResponseEntity.ok(new DatabaseHealthResponse("UP", DATABASE_NAME));
            }
        } catch (DatabaseConnectionException exception) {
            LOGGER.warn("Database health check failed: {}", exception.getMessage());
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new DatabaseHealthFailureResponse(
                        "DOWN",
                        DATABASE_NAME,
                        "Database is unavailable or not configured"
                ));
    }

    public record DatabaseHealthResponse(String status, String database) {
    }

    public record DatabaseHealthFailureResponse(String status, String database, String message) {
    }
}
