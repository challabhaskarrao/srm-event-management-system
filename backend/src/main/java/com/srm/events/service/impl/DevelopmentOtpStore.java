package com.srm.events.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory OTP capture for the explicitly enabled local dev profile only. */
@Component
@Profile("dev | test")
public class DevelopmentOtpStore {
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    public void put(String purpose, String identifier, String otp) {
        entries.put(key(purpose, identifier), new Entry(otp, Instant.now()));
    }
    public String get(String purpose, String identifier) {
        Entry entry = entries.get(key(purpose, identifier));
        return entry == null ? null : entry.otp();
    }
    private String key(String purpose, String identifier) { return purpose + "|" + identifier.trim().toUpperCase(); }
    private record Entry(String otp, Instant createdAt) { }
}
