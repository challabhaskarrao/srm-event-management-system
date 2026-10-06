package com.srm.events.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderConfigTest {
    @Test
    void newHashesUseCostTwelveAndExistingCostTenHashesRemainCompatible() {
        PasswordEncoder encoder = new PasswordEncoderConfig().passwordEncoder();
        String newHash = encoder.encode("SecurePassword1");
        assertTrue(newHash.startsWith("$2a$12$") || newHash.startsWith("$2b$12$"));
        String existingPassword = "ExistingPassword1";
        String existingCostTenHash = new BCryptPasswordEncoder(10).encode(existingPassword);
        assertTrue(encoder.matches(existingPassword, existingCostTenHash));
    }
}
