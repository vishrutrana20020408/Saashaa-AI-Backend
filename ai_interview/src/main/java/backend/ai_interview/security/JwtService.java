package backend.ai_interview.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Service
 *
 * - Generates JWT tokens for Admin/User
 * - Validates and parses tokens
 * - Extracts subject (adminId/userId) and role (ADMIN/USER)
 *
 * Latest project alignment:
 * - supports frontend auth normalization flow
 * - keeps role values consistent for role-based routing
 * - preserves subject as business identity used across user/admin modules
 */
@Service
@SuppressWarnings("all")
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token
     *
     * @param subjectId adminId or userId
     * @param role      ADMIN or USER
     * @return jwt token string
     */
    public String generateToken(String subjectId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(normalizeSubject(subjectId))
                .issuedAt(now)
                .expiration(expiry)
                .claim("role", normalizeRole(role))
                .claim("sid", UUID.randomUUID().toString()) // Unique session ID for each login
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate token (signature + expiration)
     *
     * @param token jwt token
     * @return true if valid
     */
    public boolean isTokenValid(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extract subject (adminId/userId)
     */
    public String extractSubject(String token) {
        return parseAllClaims(token).getSubject();
    }

    /**
     * Extract role (ADMIN/USER)
     */
    public String extractRole(String token) {
        Object role = parseAllClaims(token).get("role");
        return role == null ? null : normalizeRole(role.toString());
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String normalizeSubject(String subjectId) {
        if (subjectId == null) {
            return "";
        }
        return subjectId.trim();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return normalized;
    }
}