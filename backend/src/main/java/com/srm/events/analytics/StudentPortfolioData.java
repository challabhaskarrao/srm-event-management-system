package com.srm.events.analytics;

import java.util.List;
import java.util.Map;

public record StudentPortfolioData(Map<String, Object> student, List<Map<String, Object>> activities,
                                   List<Map<String, Object>> achievements, long evidenceCount) { }
