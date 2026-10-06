package com.srm.events.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {
    private final PasswordPolicy policy = new PasswordPolicy();

    @Test void acceptsReasonableStrongPassword() { assertDoesNotThrow(() -> policy.validate("SecurePassword1")); }
    @Test void rejectsEmptyPassword() { assertBad(""); }
    @Test void rejectsShortPassword() { assertBad("ShortPass1"); }
    @Test void rejectsPasswordWithoutUppercase() { assertBad("lowercaseonly1"); }
    @Test void rejectsPasswordWithoutLowercase() { assertBad("UPPERCASEONLY1"); }
    @Test void rejectsPasswordWithoutNumber() { assertBad("NoNumberPassword"); }

    private void assertBad(String value) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> policy.validate(value));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
