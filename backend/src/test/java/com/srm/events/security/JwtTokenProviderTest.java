package com.srm.events.security;

import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {
    private JwtTokenProvider jwtTokenProvider;
    private UserDAO userDAO;
    private User user;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        user = new User();
        user.setUserId(100L);
        user.setActive(true);
        user.setSessionVersion(4L);
        when(userDAO.findById(100L)).thenReturn(Optional.of(user));
        jwtTokenProvider = new JwtTokenProvider(userDAO);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtIssuer", "srm-event-management-test");
    }

    @Test
    void generateTokenCreatesValidVersionedToken() {
        String token = jwtTokenProvider.generateToken(100L, "test@example.com", "STUDENT", 4L);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(100L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("test@example.com", jwtTokenProvider.getEmailFromToken(token));
        assertEquals("STUDENT", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test void malformedTokenIsRejected() {
        assertFalse(jwtTokenProvider.validateToken("this.is.not.valid"));
    }

    @Test void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        String token = jwtTokenProvider.generateToken(100L, "test@example.com", "STUDENT", 4L);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test void priorSessionVersionIsRejectedAfterCredentialChange() {
        String token = jwtTokenProvider.generateToken(100L, "test@example.com", "STUDENT", 4L);
        user.setSessionVersion(5L);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test void inactiveAccountTokenIsRejected() {
        String token = jwtTokenProvider.generateToken(100L, "test@example.com", "STUDENT", 4L);
        user.setActive(false);
        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
