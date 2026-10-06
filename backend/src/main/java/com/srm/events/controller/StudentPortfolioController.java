package com.srm.events.controller;

import com.srm.events.analytics.StudentPortfolioFilter;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.impl.StudentPortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/students/me/portfolio")
public class StudentPortfolioController {
    private final StudentPortfolioService service; private final JwtTokenProvider jwt;
    public StudentPortfolioController(StudentPortfolioService service, JwtTokenProvider jwt) { this.service = service; this.jwt = jwt; }

    @GetMapping
    public ResponseEntity<Map<String,Object>> get(@RequestParam(required=false) Long academicYearId, @RequestParam(required=false) Integer semester,
                                                   @RequestParam(required=false, defaultValue="ALL") String recordType, @RequestParam(required=false) String typeCode,
                                                   @RequestHeader(value="Authorization", required=false) String header) {
        String token = extract(header); return ResponseEntity.ok(service.get(jwt.getUserIdFromToken(token), new StudentPortfolioFilter(academicYearId, semester, recordType, typeCode)));
    }
    private String extract(String header) { if (header == null || !header.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"); String token = header.substring(7).trim(); if (!jwt.validateToken(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"); if (!"STUDENT".equalsIgnoreCase(jwt.getRoleFromToken(token))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required"); return token; }
}
