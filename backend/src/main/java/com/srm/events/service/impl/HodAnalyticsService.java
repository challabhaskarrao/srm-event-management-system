package com.srm.events.service.impl;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.HodAnalyticsDAO;
import com.srm.events.model.Faculty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class HodAnalyticsService {
    private final FacultyDAO facultyDAO;
    private final HodAnalyticsDAO analyticsDAO;

    public HodAnalyticsService(FacultyDAO facultyDAO, HodAnalyticsDAO analyticsDAO) {
        this.facultyDAO = facultyDAO;
        this.analyticsDAO = analyticsDAO;
    }

    public Map<String, Object> get(Long userId, HodAnalyticsFilter filter) {
        Faculty faculty = facultyDAO.findActiveByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active HOD profile required"));
        return analyticsDAO.load(faculty.getDepartmentId(), filter);
    }

    public List<Map<String, Object>> drilldown(Long userId, HodAnalyticsFilter filter, String metric) {
        String normalized = metric == null ? "" : metric.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Analytics drilldown metric is required");
        Faculty faculty = facultyDAO.findActiveByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active HOD profile required"));
        return analyticsDAO.drilldown(faculty.getDepartmentId(), filter, normalized);
    }
}
