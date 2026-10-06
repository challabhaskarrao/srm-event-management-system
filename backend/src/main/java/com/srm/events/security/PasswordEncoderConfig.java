package com.srm.events.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Cost 12 for newly generated hashes. BCrypt embeds its cost in each
        // hash, so existing cost-10 development hashes remain verifiable.
        return new BCryptPasswordEncoder(12);
    }
}
