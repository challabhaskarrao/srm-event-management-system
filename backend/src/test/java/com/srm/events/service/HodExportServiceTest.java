package com.srm.events.service;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.analytics.HodExportData;
import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.HodAnalyticsDAO;
import com.srm.events.model.Faculty;
import com.srm.events.service.impl.HodExportService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HodExportServiceTest {
    @Test
    void exportsAreDepartmentScopedAndHaveRealFileFormats() throws Exception {
        FacultyDAO facultyDAO = mock(FacultyDAO.class); HodAnalyticsDAO analyticsDAO = mock(HodAnalyticsDAO.class);
        Faculty faculty = new Faculty(); faculty.setDepartmentId(7L); when(facultyDAO.findActiveByUserId(12L)).thenReturn(Optional.of(faculty));
        Map<String,Object> dashboard = Map.of("summary", Map.of("total_applications", 1L, "approved_applications", 1L, "complete", 1L, "achievements_recorded", 0L));
        when(analyticsDAO.exportData(eq(7L), any())).thenReturn(new HodExportData("Computer Science", dashboard, List.of(), List.of()));
        HodExportService service = new HodExportService(facultyDAO, analyticsDAO); HodAnalyticsFilter filter = new HodAnalyticsFilter(HodAnalyticsFilter.Period.MONTHLY, 10, 2026, null, null, null, null, null, null, null, null, null);

        byte[] xlsx = service.excel(12L, filter); byte[] pdf = service.pdf(12L, filter);

        assertEquals("PK", new String(xlsx, 0, 2)); assertTrue(pdf.length > 4); assertEquals('%', pdf[0]); assertEquals('P', pdf[1]); assertEquals('D', pdf[2]); assertEquals('F', pdf[3]);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) { assertArrayEquals(new String[]{"SUMMARY","ACTIVITY DETAILS","ACHIEVEMENTS","PENDING FOLLOW-UP"}, java.util.stream.IntStream.range(0, workbook.getNumberOfSheets()).mapToObj(workbook::getSheetName).toArray(String[]::new)); }
        verify(analyticsDAO, times(2)).exportData(7L, filter);
    }
}
