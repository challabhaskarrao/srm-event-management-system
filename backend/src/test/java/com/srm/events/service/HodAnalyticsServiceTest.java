package com.srm.events.service;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.HodAnalyticsDAO;
import com.srm.events.model.Faculty;
import com.srm.events.service.impl.HodAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class HodAnalyticsServiceTest {
    private FacultyDAO facultyDAO;
    private HodAnalyticsDAO analyticsDAO;
    private HodAnalyticsService service;
    private HodAnalyticsFilter filter;

    @BeforeEach
    void setUp() {
        facultyDAO = mock(FacultyDAO.class);
        analyticsDAO = mock(HodAnalyticsDAO.class);
        service = new HodAnalyticsService(facultyDAO, analyticsDAO);
        filter = new HodAnalyticsFilter(HodAnalyticsFilter.Period.ACADEMIC_YEAR, null, null,
                12L, null, null, null, null, null, null, null, null);
    }

    @Test
    void derivesDepartmentFromAuthenticatedFacultyAndDoesNotAcceptCallerScope() {
        Faculty faculty = new Faculty(); faculty.setFacultyId(9L); faculty.setDepartmentId(77L);
        when(facultyDAO.findActiveByUserId(41L)).thenReturn(Optional.of(faculty));
        when(analyticsDAO.load(77L, filter)).thenReturn(Map.of("departmentId", 77L));

        service.get(41L, filter);

        verify(analyticsDAO).load(77L, filter);
        verifyNoMoreInteractions(analyticsDAO);
    }

    @Test
    void drilldownUsesTheSameAuthenticatedDepartmentScope() {
        Faculty faculty = new Faculty(); faculty.setDepartmentId(77L);
        when(facultyDAO.findActiveByUserId(41L)).thenReturn(Optional.of(faculty));
        when(analyticsDAO.drilldown(77L, filter, "WINNER")).thenReturn(List.of());

        service.drilldown(41L, filter, "winner");

        verify(analyticsDAO).drilldown(77L, filter, "WINNER");
    }

    @Test
    void missingActiveHodProfileIsForbiddenAndDoesNotQueryAnalytics() {
        when(facultyDAO.findActiveByUserId(41L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.get(41L, filter));
        verifyNoInteractions(analyticsDAO);
    }
}
