package com.srm.events.controller;

import com.srm.events.service.impl.DevelopmentOtpStore;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Local-only OTP retrieval for controlled development verification. */
@RestController
@RequestMapping("/api/dev/otp")
@Profile("dev")
public class DevelopmentOtpController {
    private final DevelopmentOtpStore store;
    public DevelopmentOtpController(DevelopmentOtpStore store) { this.store = store; }
    @GetMapping
    public ResponseEntity<?> latest(@RequestParam String purpose, @RequestParam String identifier) {
        String otp = store.get(purpose, identifier);
        return otp == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(Map.of("otp", otp));
    }
}
