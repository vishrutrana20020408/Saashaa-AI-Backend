package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Resume Preview Response
 *
 * Used in the latest backend-integrated resume preview flow for:
 * - returning rendered preview content for a resume version
 * - returning preview/download metadata for frontend preview pages
 * - returning parsed profile snapshot extracted from the resume version
 * - returning resume format/template/layout metadata
 *
 * This DTO stays aligned with:
 * - resume version preview endpoints
 * - profile sync support
 * - resume editor and preview pages
 * - broader resume tailoring / AI-engine compatible flows
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("all")
public class ResumePreviewResponse {

    private boolean success;
    private String message;

    private Long resumeId;
    private Long resumeVersionId;

    private String resumeCode;
    private String versionCode;
    private String versionName;
    private String versionType;

    /**
     * HTML/text/structured preview content for frontend rendering.
     */
    private String previewContent;

    /**
     * Optional metadata for preview rendering.
     */
    private Map<String, Object> previewData;

    /**
     * Optional preview file URL.
     */
    private String previewUrl;

    /**
     * Optional downloadable file URL.
     */
    private String downloadUrl;

    /**
     * Optional original/generated file URL.
     */
    private String fileUrl;

    /**
     * Optional ATS score associated with the version.
     */
    private Integer atsScore;

    /**
     * Parsed profile snapshot extracted from this resume version.
     * Used by preview, edit, and profile sync flows.
     */
    private ResumeProfileSnapshotResponse profileSnapshot;

    /**
     * Resume format / template / layout metadata.
     * Kept flexible as structured key-value data.
     */
    private Map<String, Object> formatMetadata;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumePreviewResponse() {
    }

    public ResumePreviewResponse(
            boolean success,
            String message,
            Long resumeId,
            Long resumeVersionId,
            String resumeCode,
            String versionCode,
            String versionName,
            String versionType,
            String previewContent,
            Map<String, Object> previewData,
            String previewUrl,
            String downloadUrl,
            String fileUrl,
            Integer atsScore,
            ResumeProfileSnapshotResponse profileSnapshot,
            Map<String, Object> formatMetadata,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.success = success;
        this.message = message;
        this.resumeId = resumeId;
        this.resumeVersionId = resumeVersionId;
        this.resumeCode = resumeCode;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.versionType = versionType;
        this.previewContent = previewContent;
        this.previewData = previewData;
        this.previewUrl = previewUrl;
        this.downloadUrl = downloadUrl;
        this.fileUrl = fileUrl;
        this.atsScore = atsScore;
        this.profileSnapshot = profileSnapshot;
        this.formatMetadata = formatMetadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResumePreviewResponse success(String message) {
        ResumePreviewResponse response = new ResumePreviewResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static ResumePreviewResponse fail(String message) {
        ResumePreviewResponse response = new ResumePreviewResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static ResumePreviewResponse of(
            Long resumeId,
            Long resumeVersionId,
            String resumeCode,
            String versionCode,
            String versionName,
            String versionType,
            String previewContent,
            Map<String, Object> previewData,
            String previewUrl,
            String downloadUrl,
            String fileUrl,
            Integer atsScore,
            ResumeProfileSnapshotResponse profileSnapshot,
            Map<String, Object> formatMetadata,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ResumePreviewResponse(
                true,
                "Resume preview fetched successfully",
                resumeId,
                resumeVersionId,
                resumeCode,
                versionCode,
                versionName,
                versionType,
                previewContent,
                previewData,
                previewUrl,
                downloadUrl,
                fileUrl,
                atsScore,
                profileSnapshot,
                formatMetadata,
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

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getResumeVersionId() {
        return resumeVersionId;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.resumeVersionId = resumeVersionId;
    }

    public String getResumeCode() {
        return resumeCode;
    }

    public void setResumeCode(String resumeCode) {
        this.resumeCode = resumeCode;
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

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public Map<String, Object> getPreviewData() {
        return previewData;
    }

    public void setPreviewData(Map<String, Object> previewData) {
        this.previewData = previewData;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public ResumeProfileSnapshotResponse getProfileSnapshot() {
        return profileSnapshot;
    }

    public void setProfileSnapshot(ResumeProfileSnapshotResponse profileSnapshot) {
        this.profileSnapshot = profileSnapshot;
    }

    public Map<String, Object> getFormatMetadata() {
        return formatMetadata;
    }

    public void setFormatMetadata(Map<String, Object> formatMetadata) {
        this.formatMetadata = formatMetadata;
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
        return "ResumePreviewResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", resumeId=" + resumeId +
                ", resumeVersionId=" + resumeVersionId +
                ", resumeCode='" + resumeCode + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionType='" + versionType + '\'' +
                ", previewContentLength=" + (previewContent != null ? previewContent.length() : 0) +
                ", previewData=" + previewData +
                ", previewUrl='" + previewUrl + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", atsScore=" + atsScore +
                ", profileSnapshot=" + profileSnapshot +
                ", formatMetadata=" + formatMetadata +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}