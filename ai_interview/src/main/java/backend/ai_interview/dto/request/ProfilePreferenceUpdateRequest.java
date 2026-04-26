package backend.ai_interview.dto.request;

import jakarta.validation.constraints.Size;

/**
 * ProfilePreferenceUpdateRequest
 *
 * DTO used to update profile-related preferences.
 * These preferences control how profile data behaves
 * with resume syncing and visibility settings.
 *
 * Used for both USER and ADMIN profiles if needed.
 */
@SuppressWarnings("all")
public class ProfilePreferenceUpdateRequest {

    /**
     * If true, the system will automatically sync profile
     * fields when a new resume version is uploaded.
     */
    private Boolean autoSyncFromResume;

    /**
     * If true, existing profile fields can be overwritten
     * when syncing from resume.
     */
    private Boolean allowResumeOverwrite;

    /**
     * Controls whether the profile is visible to admins
     * or internal reviewers.
     */
    private Boolean profileVisibleToAdmin;

    /**
     * Controls whether the profile is visible in internal
     * company or interview dashboards.
     */
    private Boolean profileVisibleInDashboard;

    /**
     * Optional preferred headline override.
     */
    @Size(max = 200, message = "Preferred headline must not exceed 200 characters")
    private String preferredHeadline;

    /**
     * Optional preferred location override.
     */
    @Size(max = 200, message = "Preferred location must not exceed 200 characters")
    private String preferredLocation;

    public ProfilePreferenceUpdateRequest() {
    }

    public Boolean getAutoSyncFromResume() {
        return autoSyncFromResume;
    }

    public void setAutoSyncFromResume(Boolean autoSyncFromResume) {
        this.autoSyncFromResume = autoSyncFromResume;
    }

    public Boolean getAllowResumeOverwrite() {
        return allowResumeOverwrite;
    }

    public void setAllowResumeOverwrite(Boolean allowResumeOverwrite) {
        this.allowResumeOverwrite = allowResumeOverwrite;
    }

    public Boolean getProfileVisibleToAdmin() {
        return profileVisibleToAdmin;
    }

    public void setProfileVisibleToAdmin(Boolean profileVisibleToAdmin) {
        this.profileVisibleToAdmin = profileVisibleToAdmin;
    }

    public Boolean getProfileVisibleInDashboard() {
        return profileVisibleInDashboard;
    }

    public void setProfileVisibleInDashboard(Boolean profileVisibleInDashboard) {
        this.profileVisibleInDashboard = profileVisibleInDashboard;
    }

    public String getPreferredHeadline() {
        return preferredHeadline;
    }

    public void setPreferredHeadline(String preferredHeadline) {
        this.preferredHeadline = preferredHeadline;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }
}