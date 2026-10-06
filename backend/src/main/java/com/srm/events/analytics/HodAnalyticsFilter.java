package com.srm.events.analytics;

import java.time.LocalDate;
import java.time.YearMonth;

/** Validated, reusable filter source for HOD analytics and future exports. */
public final class HodAnalyticsFilter {
    public enum Period { MONTHLY, SEMESTER, ACADEMIC_YEAR, CUSTOM }

    private final Period period;
    private final Integer month;
    private final Integer year;
    private final Long academicYearId;
    private final Integer semester;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final Long sectionId;
    private final Integer studentYear;
    private final String eventTypeCode;
    private final String status;
    private final Long mentorId;

    public HodAnalyticsFilter(Period period, Integer month, Integer year, Long academicYearId,
                              Integer semester, LocalDate fromDate, LocalDate toDate,
                              Long sectionId, Integer studentYear, String eventTypeCode,
                              String status, Long mentorId) {
        this.period = period == null ? Period.MONTHLY : period;
        this.month = month;
        this.year = year;
        this.academicYearId = academicYearId;
        this.semester = semester;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.sectionId = sectionId;
        this.studentYear = studentYear;
        this.eventTypeCode = blankToNull(eventTypeCode);
        this.status = blankToNull(status);
        this.mentorId = mentorId;
        validate();
    }

    public static HodAnalyticsFilter monthly(Integer month, Integer year) {
        YearMonth now = YearMonth.now();
        return new HodAnalyticsFilter(month == null ? now.getMonthValue() : month,
                year == null ? now.getYear() : year);
    }

    private HodAnalyticsFilter(Integer month, Integer year) {
        this(Period.MONTHLY, month, year, null, null, null, null,
                null, null, null, null, null);
    }

    private void validate() {
        if (period == Period.MONTHLY && (month == null || month < 1 || month > 12 || year == null || year < 2000 || year > 2200)) {
            throw new IllegalArgumentException("MONTHLY analytics requires a valid month and year");
        }
        if (period == Period.SEMESTER && (academicYearId == null || semester == null || semester < 1)) {
            throw new IllegalArgumentException("SEMESTER analytics requires academicYearId and semester");
        }
        if (period == Period.ACADEMIC_YEAR && academicYearId == null) {
            throw new IllegalArgumentException("ACADEMIC_YEAR analytics requires academicYearId");
        }
        if (period == Period.CUSTOM && (fromDate == null || toDate == null || toDate.isBefore(fromDate))) {
            throw new IllegalArgumentException("CUSTOM analytics requires a valid date range");
        }
        if (studentYear != null && studentYear < 1) throw new IllegalArgumentException("studentYear must be positive");
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public Period getPeriod() { return period; }
    public Integer getMonth() { return month; }
    public Integer getYear() { return year; }
    public Long getAcademicYearId() { return academicYearId; }
    public Integer getSemester() { return semester; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public Long getSectionId() { return sectionId; }
    public Integer getStudentYear() { return studentYear; }
    public String getEventTypeCode() { return eventTypeCode; }
    public String getStatus() { return status; }
    public Long getMentorId() { return mentorId; }
}
