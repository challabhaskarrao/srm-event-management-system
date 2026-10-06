package com.srm.events.service.impl;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.analytics.HodExportData;
import com.srm.events.dao.interfaces.FacultyDAO;
import com.srm.events.dao.interfaces.HodAnalyticsDAO;
import com.srm.events.model.Faculty;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HodExportService {
    private final FacultyDAO facultyDAO; private final HodAnalyticsDAO analyticsDAO;
    public HodExportService(FacultyDAO facultyDAO, HodAnalyticsDAO analyticsDAO) { this.facultyDAO = facultyDAO; this.analyticsDAO = analyticsDAO; }

    public byte[] excel(Long userId, HodAnalyticsFilter filter) {
        HodExportData data = data(userId, filter);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(workbook); writeSummary(workbook.createSheet("SUMMARY"), header, data); writeActivities(workbook.createSheet("ACTIVITY DETAILS"), header, data.activities()); writeAchievements(workbook.createSheet("ACHIEVEMENTS"), header, data.achievements()); writePending(workbook.createSheet("PENDING FOLLOW-UP"), header, pending(data)); workbook.write(output); return output.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("Unable to generate Excel report", exception); }
    }

    public byte[] pdf(Long userId, HodAnalyticsFilter filter) {
        HodExportData data = data(userId, filter);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            List<String> lines = new ArrayList<>(); lines.add("SRM Institute of Science and Technology"); lines.add("Student Activity & Achievement Report"); lines.add("Department: " + data.departmentName()); lines.add("Period: " + filter.getPeriod().name()); lines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))); lines.add(""); lines.add("EXECUTIVE SUMMARY");
            Map<String,Object> summary = summary(data); lines.add("Applications: " + number(summary,"total_applications") + "    Approved: " + number(summary,"approved_applications") + "    Participation: " + number(summary,"approved_participation")); lines.add("Completed activities: " + number(summary,"complete") + "    Achievements: " + number(summary,"achievements_recorded") + "    Pending follow-up: " + pending(data).size()); lines.add(""); lines.add("ACTIVITY BREAKDOWN"); grouped(lines, data.activities(), "activity_type", "activity"); lines.add(""); lines.add("ACHIEVEMENT BREAKDOWN"); grouped(lines, data.achievements(), "achievement_type", "achievement"); lines.add(""); lines.add("MENTOR FOLLOW-UP SUMMARY"); lines.add("Pending items are listed below; detailed rows are available in the Excel report."); for (Map<String,Object> row : pending(data).stream().limit(12).toList()) lines.add(String.valueOf(row.get("student")) + " — " + row.get("reason") + " — " + row.get("mentor"));
            int pageNumber = 1; for (int index=0; index<lines.size(); index += 34) { PDPage page = new PDPage(PDRectangle.A4); document.addPage(page); try (PDPageContentStream content = new PDPageContentStream(document, page)) { content.beginText(); content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10); content.setLeading(14); content.newLineAtOffset(42, 800); int end=Math.min(lines.size(), index+34); for (int line=index; line<end; line++) { String value=lines.get(line); content.setFont(new PDType1Font(line == 0 || line == 1 || value.endsWith("SUMMARY") || value.endsWith("BREAKDOWN") || value.endsWith("FOLLOW-UP SUMMARY") ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA), line == 0 ? 16 : line == 1 ? 13 : 10); content.showText(safePdf(value)); content.newLine(); } content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8); content.showText("Page " + pageNumber++); content.endText(); } }
            document.save(output); return output.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("Unable to generate PDF report", exception); }
    }

    private HodExportData data(Long userId, HodAnalyticsFilter filter) { Faculty faculty = facultyDAO.findActiveByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active HOD profile required")); return analyticsDAO.exportData(faculty.getDepartmentId(), filter); }
    private CellStyle headerStyle(Workbook workbook) { Font font=workbook.createFont(); font.setBold(true); CellStyle style=workbook.createCellStyle(); style.setFont(font); return style; }
    private void writeSummary(Sheet sheet, CellStyle header, HodExportData data) { sheet.createFreezePane(0,1); Row title=sheet.createRow(0); title.createCell(0).setCellValue("SRM Activity Report — " + data.departmentName()); title.getCell(0).setCellStyle(header); int row=2; row=summaryRow(sheet,row,"Department",data.departmentName()); row=summaryRow(sheet,row,"Report Period",data.dashboard().get("period")); row=summaryRow(sheet,row,"Generated At",LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))); row++; for (Map.Entry<String,Object> entry : summary(data).entrySet()) { Row line=sheet.createRow(row++); line.createCell(0).setCellValue(entry.getKey()); setCell(line.createCell(1), entry.getValue()); } sheet.autoSizeColumn(0); sheet.autoSizeColumn(1); }
    private int summaryRow(Sheet sheet,int row,String key,Object value){Row line=sheet.createRow(row++);line.createCell(0).setCellValue(key);setCell(line.createCell(1),value);return row;}
    private void writeActivities(Sheet sheet, CellStyle header, List<Map<String,Object>> rows) { String[] columns={"Student Name","Registration Number","Section","Student Year","Semester","Mentor","Activity","Activity Type","Start Date","End Date","Application Status","Participation","Outcome","Position","Post-Event Status","Academic Year"}; writeRows(sheet,header,columns,rows,List.of("student_name","register_number","section_name","student_year","semester","mentor","activity","activity_type","start_date","end_date","application_status","participation","outcome","position","post_event_status","academic_year")); }
    private void writeAchievements(Sheet sheet, CellStyle header, List<Map<String,Object>> rows) { String[] columns={"Student Name","Registration Number","Section","Student Year","Semester","Mentor","Achievement","Achievement Type","Recorded Date","Verification Status","Academic Year"}; writeRows(sheet,header,columns,rows,List.of("student_name","register_number","section_name","student_year","semester","mentor","achievement","achievement_type","recorded_date","verification_status","academic_year")); }
    private void writePending(Sheet sheet, CellStyle header, List<Map<String,Object>> rows) { String[] columns={"Student","Registration Number","Section","Mentor","Record","Reason / Follow-Up Type","Relevant Date","Days Pending"}; writeRows(sheet,header,columns,rows,List.of("student","registration_number","section","mentor","record","reason","relevant_date","days_pending")); }
    private void writeRows(Sheet sheet, CellStyle header, String[] columns, List<Map<String,Object>> rows, List<String> keys) { sheet.createFreezePane(0,1); Row head=sheet.createRow(0); for(int i=0;i<columns.length;i++){Cell c=head.createCell(i);c.setCellValue(columns[i]);c.setCellStyle(header);} int rowNumber=1; for(Map<String,Object> source:rows){Row row=sheet.createRow(rowNumber++);for(int i=0;i<keys.size();i++)setCell(row.createCell(i),source.get(keys.get(i)));} for(int i=0;i<columns.length;i++)sheet.autoSizeColumn(i); }
    private void setCell(Cell cell,Object value){ if(value==null){cell.setCellValue("");}else if(value instanceof Number number){cell.setCellValue(number.doubleValue());}else cell.setCellValue(String.valueOf(value)); }
    private Map<String,Object> summary(HodExportData data){ Object value=data.dashboard().get("summary"); return value instanceof Map<?,?> map ? (Map<String,Object>) map : Map.of(); }
    private String number(Map<String,Object> map,String key){Object value=map.get(key);return value==null?"0":String.valueOf(value);}
    private void grouped(List<String> lines,List<Map<String,Object>> rows,String type,String title){Map<String,Integer> counts=new LinkedHashMap<>();for(Map<String,Object> row:rows){String key=String.valueOf(row.getOrDefault(type,"Unknown"));counts.put(key,counts.getOrDefault(key,0)+1);}counts.forEach((key,value)->lines.add(key+" | "+value));if(counts.isEmpty())lines.add("No records in selected filters.");}
    private List<Map<String,Object>> pending(HodExportData data){List<Map<String,Object>> result=new ArrayList<>();for(Map<String,Object> row:data.activities()){String status=String.valueOf(row.get("application_status"));String post=String.valueOf(row.get("post_event_status"));String reason=null;if(status.equals("SUBMITTED"))reason="Mentor review";else if(status.equals("PENDING_HOD_APPROVAL"))reason="HOD review";else if(status.startsWith("RETURNED"))reason="Student correction";else if(post.equals("PENDING"))reason="Post-event report";else if(post.equals("SUBMITTED"))reason="Evidence/result completion";if(reason!=null){Map<String,Object> item=new LinkedHashMap<>();item.put("student",row.get("student_name"));item.put("registration_number",row.get("register_number"));item.put("section",row.get("section_name"));item.put("mentor",row.get("mentor"));item.put("record",row.get("activity"));item.put("reason",reason);item.put("relevant_date",row.get("end_date"));item.put("days_pending",0);result.add(item);}}for(Map<String,Object> row:data.achievements()){String status=String.valueOf(row.get("verification_status"));if(!status.equals("VERIFIED")&&!status.equals("REJECTED")){Map<String,Object> item=new LinkedHashMap<>();item.put("student",row.get("student_name"));item.put("registration_number",row.get("register_number"));item.put("section",row.get("section_name"));item.put("mentor",row.get("mentor"));item.put("record",row.get("achievement"));item.put("reason","Achievement verification");item.put("relevant_date",row.get("recorded_date"));item.put("days_pending",0);result.add(item);}}return result;}
    private String safePdf(String value){return value==null?"":value.replaceAll("[^\\x20-\\x7E]","?");}
}
