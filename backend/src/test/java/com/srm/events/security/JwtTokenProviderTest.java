package com.srm.events.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Arrange
        Long userId = 100L;
        String email = "test@example.com";
        String role = "ROLE_USER";

        // Act
        String token = jwtTokenProvider.generateToken(userId, email, role);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(role, jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "this.is.not.valid";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }
    
    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Arrange: set a negative expiration time to make the token instantly expired
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L); 
        String token = jwtTokenProvider.generateToken(100L, "test@example.com", "ROLE_USER");
        
        // Assert
        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
