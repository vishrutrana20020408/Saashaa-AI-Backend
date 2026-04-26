package backend.ai_interview.exception;

/**
 * ProfileSyncException
 *
 * Thrown when the system fails to synchronize
 * a user/admin profile from a resume version.
 *
 * Typical causes:
 * - Resume version not found
 * - Parsed resume data missing
 * - Invalid structured content
 * - Profile update conflict
 *
 * Used in:
 * - UserProfileService
 * - AdminProfileService
 * - Resume → Profile sync flows
 */
@SuppressWarnings("all")
public class ProfileSyncException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfileSyncException(String message) {
        super(message);
    }

    public ProfileSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory helper for missing resume snapshot
     */
    public static ProfileSyncException missingSnapshot(Long versionId) {
        return new ProfileSyncException(
                "Profile sync failed: no profile snapshot found for resume version " + versionId
        );
    }

    /**
     * Factory helper for invalid resume data
     */
    public static ProfileSyncException invalidResumeData() {
        return new ProfileSyncException(
                "Profile sync failed: resume data could not be parsed"
        );
    }

    /**
     * Factory helper for overwrite protection
     */
    public static ProfileSyncException overwriteNotAllowed() {
        return new ProfileSyncException(
                "Profile sync failed: overwriting existing profile data is not allowed"
        );
    }
}