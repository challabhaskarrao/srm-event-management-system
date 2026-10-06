package com.srm.events.controller;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.analytics.HodAnalyticsFilterParser;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.impl.HodExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hod/analytics/export")
public class HodAnalyticsExportController {
    private final HodExportService service; private final JwtTokenProvider jwt; private final HodAnalyticsFilterParser parser;
    public HodAnalyticsExportController(HodExportService service, JwtTokenProvider jwt, HodAnalyticsFilterParser parser){this.service=service;this.jwt=jwt;this.parser=parser;}
    @GetMapping(value="/excel", produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excel(@RequestParam(required=false) String period,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year,@RequestParam(required=false) Long academicYearId,@RequestParam(required=false) Integer semester,@RequestParam(required=false) LocalDate fromDate,@RequestParam(required=false) LocalDate toDate,@RequestParam(required=false) Long sectionId,@RequestParam(required=false) Integer studentYear,@RequestParam(required=false) String eventTypeCode,@RequestParam(required=false) String status,@RequestParam(required=false) Long mentorId,@RequestHeader(value="Authorization",required=false) String header){String token=requireHod(header);HodAnalyticsFilter filter=parser.parse(period,month,year,academicYearId,semester,fromDate,toDate,sectionId,studentYear,eventTypeCode,status,mentorId);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"SRM_Activity_Report.xlsx\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(service.excel(jwt.getUserIdFromToken(token),filter));}
    @GetMapping(value="/pdf", produces="application/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam(required=false) String period,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year,@RequestParam(required=false) Long academicYearId,@RequestParam(required=false) Integer semester,@RequestParam(required=false) LocalDate fromDate,@RequestParam(required=false) LocalDate toDate,@RequestParam(required=false) Long sectionId,@RequestParam(required=false) Integer studentYear,@RequestParam(required=false) String eventTypeCode,@RequestParam(required=false) String status,@RequestParam(required=false) Long mentorId,@RequestHeader(value="Authorization",required=false) String header){String token=requireHod(header);HodAnalyticsFilter filter=parser.parse(period,month,year,academicYearId,semester,fromDate,toDate,sectionId,studentYear,eventTypeCode,status,mentorId);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"SRM_Activity_Report.pdf\"").contentType(MediaType.APPLICATION_PDF).body(service.pdf(jwt.getUserIdFromToken(token),filter));}
    private String requireHod(String header){if(header==null||!header.startsWith("Bearer "))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication required");String token=header.substring(7).trim();if(!jwt.validateToken(token))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid or expired token");if(!"HOD".equalsIgnoreCase(jwt.getRoleFromToken(token)))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"HOD access required");return token;}
}
