package com.srm.events.dao.interfaces;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.analytics.HodExportData;

import java.util.List;
import java.util.Map;

public interface HodAnalyticsDAO {
    Map<String, Object> load(Long departmentId, HodAnalyticsFilter filter);
    List<Map<String, Object>> drilldown(Long departmentId, HodAnalyticsFilter filter, String metric);
    HodExportData exportData(Long departmentId, HodAnalyticsFilter filter);
}
