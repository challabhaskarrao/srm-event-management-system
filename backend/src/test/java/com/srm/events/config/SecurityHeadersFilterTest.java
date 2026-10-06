package com.srm.events.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityHeadersFilterTest {
    @Test
    void apiResponsesReceiveCentralSecurityAndNoStoreHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new SecurityHeadersFilter().doFilter(request, response, new MockFilterChain());
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("frame-ancestors 'none'", response.getHeader("Content-Security-Policy"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }
}
