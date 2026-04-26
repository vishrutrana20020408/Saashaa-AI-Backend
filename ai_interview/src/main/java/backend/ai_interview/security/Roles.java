package backend.ai_interview.security;

/**
 * Roles Constants
 *
 * Central place to define application roles.
 *
 * NOTE:
 * Spring Security automatically prefixes roles with "ROLE_"
 * when using hasRole("ADMIN") or hasRole("USER").
 *
 * So:
 * hasRole(Roles.ADMIN)
 * internally checks for "ROLE_ADMIN"
 */
public final class Roles {

    // Prevent instantiation
    private Roles() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String COMPANY = "COMPANY";
    public static final String OWNER = "OWNER";
}