package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Resume Version Response
 *
 * Used when returning resume version information to the frontend
 * in the latest backend-integrated project structure.
 *
 * Each resume can have multiple versions:
 * - BASE (original editable resume)
 * - DUPLICATE (manual copy)
 * - TAILORED (optimized for a job application / tailoring flow)
 *
 * This DTO stays aligned with:
 * - resume version listing and detail endpoints
 * - resume editor and preview flows
 * - job-application linked tailored resume versions
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("all")
public class ResumeVersionResponse {

    private boolean success;
    private String message;

    private Long id;
    private String versionCode;
    private String versionName;
    private String versionType;

    private Long resumeId;
    private String resumeCode;

    private boolean baseVersion;
    private Long parentVersionId;

    private String jobApplicationCode;

    private String fileUrl;
    private String previewUrl;

    private Integer atsScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeVersionResponse() {
    }

    public ResumeVersionResponse(
            boolean success,
            String message,
            Long id,
            String versionCode,
            String versionName,
            String versionType,
            Long resumeId,
            String resumeCode,
            boolean baseVersion,
            Long parentVersionId,
            String jobApplicationCode,
            String fileUrl,
            String previewUrl,
            Integer atsScore,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.versionType = versionType;
        this.resumeId = resumeId;
        this.resumeCode = resumeCode;
        this.baseVersion = baseVersion;
        this.parentVersionId = parentVersionId;
        this.jobApplicationCode = jobApplicationCode;
        this.fileUrl = fileUrl;
        this.previewUrl = previewUrl;
        this.atsScore = atsScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResumeVersionResponse success(String message) {
        ResumeVersionResponse response = new ResumeVersionResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static ResumeVersionResponse fail(String message) {
        ResumeVersionResponse response = new ResumeVersionResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static ResumeVersionResponse of(
            Long id,
            String versionCode,
            String versionName,
            String versionType,
            Long resumeId,
            String resumeCode,
            boolean baseVersion,
            Long parentVersionId,
            String jobApplicationCode,
            String fileUrl,
            String previewUrl,
            Integer atsScore,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ResumeVersionResponse(
                true,
                "Resume version fetched successfully",
                id,
                versionCode,
                versionName,
                versionType,
                resumeId,
                resumeCode,
                baseVersion,
                parentVersionId,
                jobApplicationCode,
                fileUrl,
                previewUrl,
                atsScore,
                createdAt,
                updatedAt
        );
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Frontend compatibility alias.
     */
    public Long getResumeVersionId() {
        return id;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.id = resumeVersionId;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeCode() {
        return resumeCode;
    }

    public void setResumeCode(String resumeCode) {
        this.resumeCode = resumeCode;
    }

    public boolean isBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(boolean baseVersion) {
        this.baseVersion = baseVersion;
    }

    public Long getParentVersionId() {
        return parentVersionId;
    }

    public void setParentVersionId(Long parentVersionId) {
        this.parentVersionId = parentVersionId;
    }

    public String getJobApplicationCode() {
        return jobApplicationCode;
    }

    public void setJobApplicationCode(String jobApplicationCode) {
        this.jobApplicationCode = jobApplicationCode;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ResumeVersionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", id=" + id +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionType='" + versionType + '\'' +
                ", resumeId=" + resumeId +
                ", resumeCode='" + resumeCode + '\'' +
                ", baseVersion=" + baseVersion +
                ", parentVersionId=" + parentVersionId +
                ", jobApplicationCode='" + jobApplicationCode + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                ", atsScore=" + atsScore +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}