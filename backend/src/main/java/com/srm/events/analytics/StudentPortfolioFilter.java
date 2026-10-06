package com.srm.events.analytics;

import java.util.Locale;

public record StudentPortfolioFilter(Long academicYearId, Integer semester, String recordType, String typeCode) {
    public StudentPortfolioFilter {
        recordType = recordType == null || recordType.isBlank() ? "ALL" : recordType.trim().toUpperCase(Locale.ROOT);
        if (!recordType.equals("ALL") && !recordType.equals("ACTIVITIES") && !recordType.equals("ACHIEVEMENTS")) {
            throw new IllegalArgumentException("recordType must be ALL, ACTIVITIES, or ACHIEVEMENTS");
        }
        typeCode = typeCode == null || typeCode.isBlank() ? null : typeCode.trim().toUpperCase(Locale.ROOT);
        if (semester != null && (semester < 1 || semester > 12)) throw new IllegalArgumentException("semester must be between 1 and 12");
    }
}
