package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Resume Duplicate Create Request
 *
 * Used in the latest backend-integrated resume version flow to create
 * a duplicate resume version from an existing source version.
 *
 * The original resume version remains unchanged, while the duplicate can be
 * used for:
 * - manual editing
 * - job-specific customization
 * - resume tailoring / AI-engine flows
 * - preview generation
 *
 * Example payload:
 * {
 *   "sourceVersionId": 10,
 *   "versionName": "Google Backend Resume Copy",
 *   "reason": "Create duplicate for Google application",
 *   "companyName": "Google",
 *   "jobTitle": "Backend Software Engineer",
 *   "copyStructuredContent": true,
 *   "copyRawText": true,
 *   "generatePreview": true
 * }
 */
@SuppressWarnings("all")
public class ResumeDuplicateCreateRequest {

    /**
     * Existing resume version that will be duplicated.
     */
    @NotNull(message = "Source resume version id is required")
    private Long sourceVersionId;

    /**
     * Name for the new duplicate version.
     */
    @NotBlank(message = "Version name is required")
    @Size(max = 200, message = "Version name must not exceed 200 characters")
    private String versionName;

    /**
     * Optional reason for creating this duplicate.
     */
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    /**
     * Optional company name related to this duplicate.
     */
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    /**
     * Optional job title related to this duplicate.
     */
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String jobTitle;

    /**
     * Whether structured content should be copied.
     */
    private Boolean copyStructuredContent;

    /**
     * Whether raw text should be copied.
     */
    private Boolean copyRawText;

    /**
     * Whether preview should be generated for the duplicate.
     */
    private Boolean generatePreview;

    public ResumeDuplicateCreateRequest() {
        this.copyStructuredContent = Boolean.TRUE;
        this.copyRawText = Boolean.TRUE;
        this.generatePreview = Boolean.TRUE;
    }

    public ResumeDuplicateCreateRequest(
            Long sourceVersionId,
            String versionName,
            String reason,
            String companyName,
            String jobTitle,
            Boolean copyStructuredContent,
            Boolean copyRawText,
            Boolean generatePreview
    ) {
        this.sourceVersionId = sourceVersionId;
        this.versionName = versionName;
        this.reason = reason;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.copyStructuredContent = copyStructuredContent != null ? copyStructuredContent : Boolean.TRUE;
        this.copyRawText = copyRawText != null ? copyRawText : Boolean.TRUE;
        this.generatePreview = generatePreview != null ? generatePreview : Boolean.TRUE;
    }

    public Long getSourceVersionId() {
        return sourceVersionId;
    }

    public void setSourceVersionId(Long sourceVersionId) {
        this.sourceVersionId = sourceVersionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Boolean getCopyStructuredContent() {
        return copyStructuredContent;
    }

    public void setCopyStructuredContent(Boolean copyStructuredContent) {
        this.copyStructuredContent = copyStructuredContent;
    }

    public Boolean getCopyRawText() {
        return copyRawText;
    }

    public void setCopyRawText(Boolean copyRawText) {
        this.copyRawText = copyRawText;
    }

    public Boolean getGeneratePreview() {
        return generatePreview;
    }

    public void setGeneratePreview(Boolean generatePreview) {
        this.generatePreview = generatePreview;
    }

    public boolean shouldCopyStructuredContent() {
        return Boolean.TRUE.equals(copyStructuredContent);
    }

    public boolean shouldCopyRawText() {
        return Boolean.TRUE.equals(copyRawText);
    }

    public boolean shouldGeneratePreview() {
        return Boolean.TRUE.equals(generatePreview);
    }

    @Override
    public String toString() {
        return "ResumeDuplicateCreateRequest{" +
                "sourceVersionId=" + sourceVersionId +
                ", versionName='" + versionName + '\'' +
                ", reason='" + reason + '\'' +
                ", companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", copyStructuredContent=" + copyStructuredContent +
                ", copyRawText=" + copyRawText +
                ", generatePreview=" + generatePreview +
                '}';
    }
}