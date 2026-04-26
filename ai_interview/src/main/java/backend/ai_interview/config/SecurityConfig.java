package backend.ai_interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import backend.ai_interview.security.JwtAuthFilter;
import backend.ai_interview.security.Roles;

/**
 * Spring Security Configuration
 *
 * Features:
 * - JWT-based authentication
 * - Stateless session management
 * - Public auth endpoints for login/register
 * - Role-based protection for /api/user/** and /api/admin/**
 *
 * IMPORTANT:
 * Roles.USER and Roles.ADMIN must be:
 *   USER
 *   ADMIN
 *
 * Not:
 *   ROLE_USER
 *   ROLE_ADMIN
 *
 * because hasRole("USER") automatically checks for ROLE_USER internally.
 */
@Configuration
@SuppressWarnings("all")
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow browser preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Authenticated current-user endpoint: allow anonymous checks so the frontend can
                        // gracefully handle missing or invalid sessions without a 403 response.
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()

                        // Public authentication endpoints (login, register, logout, health)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/owner/auth/**").permitAll()
                        .requestMatchers("/api/auth/owner/**").permitAll()

                        // Role protected routes
                        .requestMatchers("/api/jobs/company").hasRole(Roles.COMPANY)
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole(Roles.COMPANY)
                        .requestMatchers("/api/jobs/admin").hasRole(Roles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/internal-job-applications/apply").hasRole(Roles.ADMIN)
                        .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)
                        .requestMatchers("/api/company/**").hasRole(Roles.COMPANY)
                        .requestMatchers("/api/user/resume/**").hasAnyRole(Roles.USER, Roles.ADMIN)
                        .requestMatchers("/api/user/interview/session/**", "/api/user/interview/session", "/api/user/interview/sessions").hasAnyRole(Roles.USER, Roles.ADMIN)
                        .requestMatchers("/api/interview/session/**", "/api/interview/session", "/api/interview/sessions").hasAnyRole(Roles.USER, Roles.ADMIN)
                        .requestMatchers("/api/speech/**").permitAll()
                        .requestMatchers("/api/profile/documents/view/**").authenticated()
                        .requestMatchers("/api/user/**").hasRole(Roles.USER)
                        
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}