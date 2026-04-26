package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ResumeCreateResponse
 *
 * Response DTO returned after creating a new resume from the frontend editor.
 *
 * Typical flow:
 * - frontend sends ResumeCreateRequest
 * - backend creates Resume + ResumeVersion
 * - backend generates PDF
 * - backend stores file in AWS S3
 * - backend returns creation summary to frontend
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * This response is intentionally designed to support:
 * - structured resume editor flow
 * - PDF preview/download flow
 * - resume list/detail screens
 * - version-aware resume management
 */
@SuppressWarnings("all")
public class ResumeCreateResponse {

    /**
     * Main resume id.
     */
    private Long resumeId;

    /**
     * Newly created resume version id.
     */
    private Long resumeVersionId;

    /**
     * Resume code / public reference / tracking code if used.
     */
    private String resumeCode;

    /**
     * Resume version code if your system uses version identifiers.
     */
    private String versionCode;

    /**
     * Human-readable resume name.
     */
    private String resumeName;

    /**
     * Optional resume title shown in the document.
     */
    private String resumeTitle;

    /**
     * Whether this version is marked as the base/default resume.
     */
    private Boolean baseVersion;

    /**
     * Current resume status.
     * Example:
     * - DRAFT
     * - ACTIVE
     * - ARCHIVED
     */
    private String status;

    /**
     * Version type.
     * Example:
     * - BASE
     * - DUPLICATE
     * - TAILORED
     * - EDITED
     */
    private String versionType;

    /**
     * Original source of this resume.
     * Example:
     * - MANUAL_EDITOR
     * - UPLOAD
     */
    private String sourceType;

    /**
     * Generated PDF file name if applicable.
     */
    private String fileName;

    /**
     * MIME/content type if relevant.
     * Example:
     * - application/pdf
     */
    private String contentType;

    /**
     * S3 key or internal storage key for the generated PDF.
     */
    private String storageKey;

    /**
     * Download URL for generated PDF.
     * Prefer backend download endpoint rather than exposing raw S3 URL directly.
     */
    private String downloadUrl;

    /**
     * Preview URL if available.
     */
    private String previewUrl;

    /**
     * Optional public/share URL if your system supports sharing.
     */
    private String shareUrl;

    /**
     * Whether PDF generation completed successfully.
     */
    private Boolean pdfGenerated;

    /**
     * Whether file upload/storage completed successfully.
     */
    private Boolean storedInS3;

    /**
     * Whether AI-based analysis has been triggered or completed.
     */
    private Boolean aiAnalysisAvailable;

    /**
     * Optional ATS score if already calculated at creation time.
     */
    private Integer atsScore;

    /**
     * Optional summary/short message for frontend success state.
     */
    private String message;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * Minimal structured section summary for quick frontend rendering.
     */
    private SectionCounts sectionCounts;

    /**
     * Optional warnings encountered during generation/storage.
     */
    private List<String> warnings = new ArrayList<>();

    public ResumeCreateResponse() {
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

    public String getResumeName() {
        return resumeName;
    }

    public void setResumeName(String resumeName) {
        this.resumeName = resumeName;
    }

    public String getResumeTitle() {
        return resumeTitle;
    }

    public void setResumeTitle(String resumeTitle) {
        this.resumeTitle = resumeTitle;
    }

    public Boolean getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(Boolean baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public Boolean getPdfGenerated() {
        return pdfGenerated;
    }

    public void setPdfGenerated(Boolean pdfGenerated) {
        this.pdfGenerated = pdfGenerated;
    }

    public Boolean getStoredInS3() {
        return storedInS3;
    }

    public void setStoredInS3(Boolean storedInS3) {
        this.storedInS3 = storedInS3;
    }

    public Boolean getAiAnalysisAvailable() {
        return aiAnalysisAvailable;
    }

    public void setAiAnalysisAvailable(Boolean aiAnalysisAvailable) {
        this.aiAnalysisAvailable = aiAnalysisAvailable;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public SectionCounts getSectionCounts() {
        return sectionCounts;
    }

    public void setSectionCounts(SectionCounts sectionCounts) {
        this.sectionCounts = sectionCounts;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    /**
     * Helper block for frontend summary rendering.
     */
    public static class SectionCounts {

        private Integer skillsCount = 0;
        private Integer educationCount = 0;
        private Integer experienceCount = 0;
        private Integer projectsCount = 0;
        private Integer certificationsCount = 0;
        private Integer achievementsCount = 0;
        private Integer linksCount = 0;
        private Integer languagesCount = 0;

        public SectionCounts() {
        }

        public Integer getSkillsCount() {
            return skillsCount;
        }

        public void setSkillsCount(Integer skillsCount) {
            this.skillsCount = skillsCount;
        }

        public Integer getEducationCount() {
            return educationCount;
        }

        public void setEducationCount(Integer educationCount) {
            this.educationCount = educationCount;
        }

        public Integer getExperienceCount() {
            return experienceCount;
        }

        public void setExperienceCount(Integer experienceCount) {
            this.experienceCount = experienceCount;
        }

        public Integer getProjectsCount() {
            return projectsCount;
        }

        public void setProjectsCount(Integer projectsCount) {
            this.projectsCount = projectsCount;
        }

        public Integer getCertificationsCount() {
            return certificationsCount;
        }

        public void setCertificationsCount(Integer certificationsCount) {
            this.certificationsCount = certificationsCount;
        }

        public Integer getAchievementsCount() {
            return achievementsCount;
        }

        public void setAchievementsCount(Integer achievementsCount) {
            this.achievementsCount = achievementsCount;
        }

        public Integer getLinksCount() {
            return linksCount;
        }

        public void setLinksCount(Integer linksCount) {
            this.linksCount = linksCount;
        }

        public Integer getLanguagesCount() {
            return languagesCount;
        }

        public void setLanguagesCount(Integer languagesCount) {
            this.languagesCount = languagesCount;
        }
    }
}