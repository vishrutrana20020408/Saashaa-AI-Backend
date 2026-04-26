package backend.ai_interview.exception;

/**
 * ProfileNotFoundException
 *
 * Thrown when a requested user/admin profile
 * cannot be found in the database.
 *
 * Used in:
 * - UserProfileService
 * - AdminProfileService
 * - Profile sync operations
 */
@SuppressWarnings("all")
public class ProfileNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Helper factory for user profile not found
     */
    public static ProfileNotFoundException forUser(String userId) {
        return new ProfileNotFoundException("Profile not found for user: " + userId);
    }

    /**
     * Helper factory for admin profile not found
     */
    public static ProfileNotFoundException forAdmin(String adminId) {
        return new ProfileNotFoundException("Profile not found for admin: " + adminId);
    }
}