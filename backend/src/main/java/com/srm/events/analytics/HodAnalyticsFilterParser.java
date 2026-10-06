package com.srm.events.analytics;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class HodAnalyticsFilterParser {
    public HodAnalyticsFilter parse(String rawPeriod, Integer month, Integer year, Long academicYearId, Integer semester,
                                    LocalDate fromDate, LocalDate toDate, Long sectionId, Integer studentYear,
                                    String eventTypeCode, String status, Long mentorId) {
        try {
            HodAnalyticsFilter.Period selected = rawPeriod == null || rawPeriod.isBlank() ? HodAnalyticsFilter.Period.MONTHLY : HodAnalyticsFilter.Period.valueOf(rawPeriod.trim().toUpperCase());
            if (selected == HodAnalyticsFilter.Period.MONTHLY && (month == null || year == null)) { YearMonth now = YearMonth.now(); month = now.getMonthValue(); year = now.getYear(); }
            return new HodAnalyticsFilter(selected, month, year, academicYearId, semester, fromDate, toDate, sectionId, studentYear, eventTypeCode, status, mentorId);
        } catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception); }
    }
}
