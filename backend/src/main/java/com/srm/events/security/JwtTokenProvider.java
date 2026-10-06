package com.srm.events.security;

import com.srm.events.dao.interfaces.UserDAO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final UserDAO userDAO;

    public JwtTokenProvider(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.issuer:srm-event-management}")
    private String jwtIssuer;

    private SecretKey key() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a Base64-encoded signing key containing at least 32 bytes",
                    exception);
        }
    }

    public String generateToken(Long userId, String email, String role, long sessionVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .issuer(jwtIssuer)
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("userId", userId)
                .claim("role", role)
                .claim("sessionVersion", sessionVersion)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Number userId = claims.get("userId", Number.class);
            Number sessionVersion = claims.get("sessionVersion", Number.class);
            if (userId == null || sessionVersion == null) return false;
            return userDAO.findById(userId.longValue())
                    .filter(user -> user.isActive() && user.getSessionVersion() == sessionVersion.longValue())
                    .isPresent();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .requireIssuer(jwtIssuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
