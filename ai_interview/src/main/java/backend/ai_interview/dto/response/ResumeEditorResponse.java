package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Resume Editor Response
 *
 * Returned to the frontend resume editor in the latest
 * backend-integrated resume version flow.
 *
 * Contains the structured editing payload for a specific resume version
 * and stays aligned with:
 * - resume version editor endpoints
 * - preview generation flow
 * - ATS/editor support
 * - broader resume tailoring / AI-engine compatible resume content handling
 *
 * Example response:
 * {
 *   "success": true,
 *   "message": "Editor data loaded",
 *   "resumeVersionId": 10,
 *   "resumeId": 5,
 *   "versionCode": "RV-0010",
 *   "versionName": "Base Resume",
 *   "structuredContent": { ... },
 *   "rawText": "...",
 *   "previewUrl": "...",
 *   "atsScore": 82
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("all")
public class ResumeEditorResponse {

    private boolean success;
    private String message;

    private Long resumeId;
    private Long resumeVersionId;

    private String resumeCode;
    private String versionCode;
    private String versionName;
    private String versionType;

    /**
     * Structured JSON content used by the resume editor.
     */
    private Map<String, Object> structuredContent;

    /**
     * Optional raw text representation.
     */
    private String rawText;

    /**
     * Optional preview URL.
     */
    private String previewUrl;

    /**
     * Optional ATS score.
     */
    private Integer atsScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeEditorResponse() {
    }

    public ResumeEditorResponse(
            boolean success,
            String message,
            Long resumeId,
            Long resumeVersionId,
            String resumeCode,
            String versionCode,
            String versionName,
            String versionType,
            Map<String, Object> structuredContent,
            String rawText,
            String previewUrl,
            Integer atsScore,
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
        this.structuredContent = structuredContent;
        this.rawText = rawText;
        this.previewUrl = previewUrl;
        this.atsScore = atsScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResumeEditorResponse success(String message) {
        ResumeEditorResponse response = new ResumeEditorResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static ResumeEditorResponse fail(String message) {
        ResumeEditorResponse response = new ResumeEditorResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static ResumeEditorResponse of(
            Long resumeId,
            Long resumeVersionId,
            String resumeCode,
            String versionCode,
            String versionName,
            String versionType,
            Map<String, Object> structuredContent,
            String rawText,
            String previewUrl,
            Integer atsScore,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ResumeEditorResponse(
                true,
                "Resume editor data fetched successfully",
                resumeId,
                resumeVersionId,
                resumeCode,
                versionCode,
                versionName,
                versionType,
                structuredContent,
                rawText,
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

    public Map<String, Object> getStructuredContent() {
        return structuredContent;
    }

    public void setStructuredContent(Map<String, Object> structuredContent) {
        this.structuredContent = structuredContent;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
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
        return "ResumeEditorResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", resumeId=" + resumeId +
                ", resumeVersionId=" + resumeVersionId +
                ", resumeCode='" + resumeCode + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionType='" + versionType + '\'' +
                ", structuredContent=" + structuredContent +
                ", rawTextLength=" + (rawText != null ? rawText.length() : 0) +
                ", previewUrl='" + previewUrl + '\'' +
                ", atsScore=" + atsScore +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}