package backend.ai_interview.dto.response;

import java.time.LocalDateTime;

/**
 * Resume Response
 *
 * Main response DTO for resume details in the latest
 * backend-integrated project structure.
 *
 * Used for:
 * - fetching a single resume
 * - listing user resumes
 * - admin resume inspection
 * - frontend compatibility with resume/version/editor flows
 *
 * Compatibility update:
 * - keeps existing fields
 * - keeps frontend-friendly alias getters/fields
 * - stays aligned with resume version, preview, and tailoring flows
 */
@SuppressWarnings("all")
public class ResumeResponse {

    private boolean success;
    private String message;

    /**
     * Primary resume database id.
     */
    private Long id;

    private String resumeCode;
    private String title;
    private String description;
    private String status;

    private String originalFileName;
    private String originalFileUrl;
    private String currentBaseVersionCode;

    private String userId;
    private String userEmail;
    private String userName;

    private Integer totalVersions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Optional frontend compatibility fields.
     * These help align simple resume endpoints with version/editor workflows.
     */
    private Long resumeVersionId;
    private Integer atsScore;
    private String rawText;
    private String structuredContentJson;

    public ResumeResponse() {
    }

    public ResumeResponse(
            boolean success,
            String message,
            Long id,
            String resumeCode,
            String title,
            String description,
            String status,
            String originalFileName,
            String originalFileUrl,
            String currentBaseVersionCode,
            String userId,
            String userEmail,
            String userName,
            Integer totalVersions,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.resumeCode = resumeCode;
        this.title = title;
        this.description = description;
        this.status = status;
        this.originalFileName = originalFileName;
        this.originalFileUrl = originalFileUrl;
        this.currentBaseVersionCode = currentBaseVersionCode;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.totalVersions = totalVersions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ResumeResponse success(String message) {
        ResumeResponse response = new ResumeResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static ResumeResponse fail(String message) {
        ResumeResponse response = new ResumeResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static ResumeResponse of(
            Long id,
            String resumeCode,
            String title,
            String description,
            String status,
            String originalFileName,
            String originalFileUrl,
            String currentBaseVersionCode,
            String userId,
            String userEmail,
            String userName,
            Integer totalVersions,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ResumeResponse(
                true,
                "Resume fetched successfully",
                id,
                resumeCode,
                title,
                description,
                status,
                originalFileName,
                originalFileUrl,
                currentBaseVersionCode,
                userId,
                userEmail,
                userName,
                totalVersions,
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

    /**
     * Existing id getter.
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Frontend/controller compatibility alias.
     */
    public Long getResumeId() {
        return id;
    }

    public void setResumeId(Long resumeId) {
        this.id = resumeId;
    }

    public String getResumeCode() {
        return resumeCode;
    }

    public void setResumeCode(String resumeCode) {
        this.resumeCode = resumeCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Frontend compatibility alias.
     */
    public String getResumeName() {
        return title;
    }

    public void setResumeName(String resumeName) {
        this.title = resumeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Optional frontend compatibility alias.
     */
    public String getResumeDescription() {
        return description;
    }

    public void setResumeDescription(String resumeDescription) {
        this.description = resumeDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    /**
     * Frontend compatibility alias.
     */
    public String getFileName() {
        return originalFileName;
    }

    public void setFileName(String fileName) {
        this.originalFileName = fileName;
    }

    public String getOriginalFileUrl() {
        return originalFileUrl;
    }

    public void setOriginalFileUrl(String originalFileUrl) {
        this.originalFileUrl = originalFileUrl;
    }

    /**
     * Frontend compatibility alias.
     */
    public String getFileUrl() {
        return originalFileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.originalFileUrl = fileUrl;
    }

    public String getCurrentBaseVersionCode() {
        return currentBaseVersionCode;
    }

    public void setCurrentBaseVersionCode(String currentBaseVersionCode) {
        this.currentBaseVersionCode = currentBaseVersionCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getTotalVersions() {
        return totalVersions;
    }

    public void setTotalVersions(Integer totalVersions) {
        this.totalVersions = totalVersions;
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

    public Long getResumeVersionId() {
        return resumeVersionId;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.resumeVersionId = resumeVersionId;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getStructuredContentJson() {
        return structuredContentJson;
    }

    public void setStructuredContentJson(String structuredContentJson) {
        this.structuredContentJson = structuredContentJson;
    }

    @Override
    public String toString() {
        return "ResumeResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", id=" + id +
                ", resumeCode='" + resumeCode + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", originalFileUrl='" + originalFileUrl + '\'' +
                ", currentBaseVersionCode='" + currentBaseVersionCode + '\'' +
                ", userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userName='" + userName + '\'' +
                ", totalVersions=" + totalVersions +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", resumeVersionId=" + resumeVersionId +
                ", atsScore=" + atsScore +
                ", rawText='" + rawText + '\'' +
                ", structuredContentJson='" + structuredContentJson + '\'' +
                '}';
    }
}