package backend.ai_interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Authentication Response DTO
 *
 * Returned after successful login or registration in the latest
 * backend-integrated authentication flow.
 *
 * This DTO stays aligned with:
 * - frontend auth normalization (token/role/id handling)
 * - role-based routing (USER / ADMIN / COMPANY / OWNER)
 * - flexible token field usage across frontend variations
 */
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT Token for authentication.
     */
    private String token;

    /**
     * Role of the authenticated user (USER or ADMIN).
     */
    private String role;

    /**
     * Unique ID (User_ID or Admin_ID).
     */
    private String id;
}