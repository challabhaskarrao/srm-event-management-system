package com.srm.events.controller;

import com.srm.events.analytics.HodAnalyticsFilter;
import com.srm.events.analytics.HodAnalyticsFilterParser;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.impl.HodAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hod/analytics")
public class HodAnalyticsController {
    private final HodAnalyticsService service;
    private final JwtTokenProvider jwt;
    private final HodAnalyticsFilterParser filterParser;

    public HodAnalyticsController(HodAnalyticsService service, JwtTokenProvider jwt, HodAnalyticsFilterParser filterParser) {
        this.service = service; this.jwt = jwt; this.filterParser = filterParser;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> get(@RequestParam(required = false) String period,
                                                    @RequestParam(required = false) Integer month,
                                                    @RequestParam(required = false) Integer year,
                                                    @RequestParam(required = false) Long academicYearId,
                                                    @RequestParam(required = false) Integer semester,
                                                    @RequestParam(required = false) LocalDate fromDate,
                                                    @RequestParam(required = false) LocalDate toDate,
                                                    @RequestParam(required = false) Long sectionId,
                                                    @RequestParam(required = false) Integer studentYear,
                                                    @RequestParam(required = false) String eventTypeCode,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Long mentorId,
                                                    @RequestHeader(value = "Authorization", required = false) String header) {
        String token = requireHod(header);
        return ResponseEntity.ok(service.get(jwt.getUserIdFromToken(token), filterParser.parse(period, month, year, academicYearId,
                semester, fromDate, toDate, sectionId, studentYear, eventTypeCode, status, mentorId)));
    }

    @GetMapping("/drilldown")
    public ResponseEntity<List<Map<String, Object>>> drilldown(@RequestParam String metric,
                                                                @RequestParam(required = false) String period,
                                                                @RequestParam(required = false) Integer month,
                                                                @RequestParam(required = false) Integer year,
                                                                @RequestParam(required = false) Long academicYearId,
                                                                @RequestParam(required = false) Integer semester,
                                                                @RequestParam(required = false) LocalDate fromDate,
                                                                @RequestParam(required = false) LocalDate toDate,
                                                                @RequestParam(required = false) Long sectionId,
                                                                @RequestParam(required = false) Integer studentYear,
                                                                @RequestParam(required = false) String eventTypeCode,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) Long mentorId,
                                                                @RequestHeader(value = "Authorization", required = false) String header) {
        String token = requireHod(header);
        return ResponseEntity.ok(service.drilldown(jwt.getUserIdFromToken(token), filterParser.parse(period, month, year, academicYearId,
                semester, fromDate, toDate, sectionId, studentYear, eventTypeCode, status, mentorId), metric));
    }

    private String requireHod(String header) {
        if (header == null || !header.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        String token = header.substring(7).trim();
        if (!jwt.validateToken(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        if (!"HOD".equalsIgnoreCase(jwt.getRoleFromToken(token))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "HOD access required");
        return token;
    }
}
