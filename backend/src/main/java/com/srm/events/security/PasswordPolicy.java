package com.srm.events.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@Component
public class PasswordPolicy {
    public static final int MIN_LENGTH = 12;
    public static final int MAX_BCRYPT_BYTES = 72;

    public void validate(String password) {
        boolean validLength = password != null && password.length() >= MIN_LENGTH
                && password.getBytes(StandardCharsets.UTF_8).length <= MAX_BCRYPT_BYTES;
        boolean hasUpper = password != null && password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password != null && password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password != null && password.chars().anyMatch(Character::isDigit);
        if (!validLength || !hasUpper || !hasLower || !hasDigit) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 12 characters and include uppercase, lowercase, and a number");
        }
    }
}
