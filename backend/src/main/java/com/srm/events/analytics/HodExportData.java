package com.srm.events.analytics;

import java.util.List;
import java.util.Map;

public record HodExportData(String departmentName, Map<String, Object> dashboard, List<Map<String, Object>> activities,
                            List<Map<String, Object>> achievements) { }
