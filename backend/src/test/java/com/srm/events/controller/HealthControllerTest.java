package com.srm.events.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    @Test
    void healthReportsApplicationAsUp() {
        HealthController.HealthResponse response = new HealthController().health();

        assertEquals("UP", response.status());
        assertEquals("SRM Event Management System", response.application());
    }
}
