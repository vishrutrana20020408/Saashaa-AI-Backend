package backend.ai_interview.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter
 *
 * Reads "Authorization: Bearer <token>" header,
 * validates JWT, extracts role and subject,
 * and sets authentication in Spring Security context.
 */
@Component
@SuppressWarnings("all")
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate and extract claims
            String subjectId = jwtService.extractSubject(token); // adminId or userId
            String role = jwtService.extractRole(token);         // ADMIN or USER

            if (subjectId != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Spring Security expects roles like: ROLE_ADMIN, ROLE_USER
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        subjectId, // principal (we store the id here)
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ignored) {
            // Invalid token → do not authenticate, protected endpoints will deny access
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }
            return authHeader.trim();
        }

        if (request.getCookies() == null) {
            return resolveTokenFromCookieHeader(request.getHeader("Cookie"));
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie == null || cookie.getName() == null) {
                continue;
            }
            String token = switch (cookie.getName()) {
                case "Authorization", "Bearer",
                        "token", "authToken", "accessToken", "jwtToken",
                        "userToken", "adminToken", "companyToken", "ownerToken",
                        "access_token", "auth_token", "jwt_token" -> cookie.getValue();
                default -> null;
            };
            if (token != null) {
                return token;
            }
        }

        return resolveTokenFromCookieHeader(request.getHeader("Cookie"));
    }

    private String resolveTokenFromCookieHeader(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String[] cookies = cookieHeader.split(";\\s*");
        for (String cookie : cookies) {
            int equalsIndex = cookie.indexOf('=');
            if (equalsIndex <= 0 || equalsIndex >= cookie.length() - 1) {
                continue;
            }
            String name = cookie.substring(0, equalsIndex).trim();
            String value = cookie.substring(equalsIndex + 1).trim();
            if (value.isEmpty()) {
                continue;
            }
            String token = switch (name) {
                case "Authorization", "Bearer",
                        "token", "authToken", "accessToken", "jwtToken",
                        "userToken", "adminToken", "companyToken", "ownerToken",
                        "access_token", "auth_token", "jwt_token" -> value;
                default -> null;
            };
            if (token != null) {
                return token;
            }
        }

        return null;
    }
}