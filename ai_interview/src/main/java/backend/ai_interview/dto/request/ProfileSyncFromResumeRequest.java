package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * ProfileSyncFromResumeRequest
 *
 * DTO used when syncing an official profile
 * from a specific resume/version.
 *
 * This can be used by both user and admin profile sync flows.
 */
@SuppressWarnings("all")
public class ProfileSyncFromResumeRequest {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @NotNull(message = "Resume version ID is required")
    private Long versionId;

    /**
     * Optional flag:
     * true  -> overwrite existing profile fields with resume data
     * false -> only fill empty / missing profile fields
     */
    private Boolean overwriteExisting = Boolean.TRUE;

    public ProfileSyncFromResumeRequest() {
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Boolean getOverwriteExisting() {
        return overwriteExisting;
    }

    public void setOverwriteExisting(Boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
}