package backend.ai_interview.dto.response;

/**
 * ProfileSummaryResponse
 *
 * Lightweight profile response used for navbar/profile-summary display
 * in the latest backend-integrated project structure.
 *
 * Contains only essential fields required for quick UI rendering.
 *
 * Used by:
 * - UserNavbar
 * - AdminNavbar
 * - profile summary dropdowns / compact profile cards
 */
@SuppressWarnings("all")
public class ProfileSummaryResponse {

    private boolean success;
    private String message;

    private String userId;
    private String adminId;

    private String fullName;
    private String headline;
    private String email;

    /**
     * Optional avatar initials generated from full name.
     * Example: "VR" for Vishrut Rana
     */
    private String avatarInitials;

    public ProfileSummaryResponse() {
    }

    public ProfileSummaryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Success factory.
     */
    public static ProfileSummaryResponse ok(String message) {
        return new ProfileSummaryResponse(true, message);
    }

    /**
     * Failure factory.
     */
    public static ProfileSummaryResponse fail(String message) {
        return new ProfileSummaryResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.avatarInitials = generateInitials(fullName);
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public void setAvatarInitials(String avatarInitials) {
        this.avatarInitials = avatarInitials;
    }

    /**
     * Utility method to generate initials from full name.
     */
    private String generateInitials(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}