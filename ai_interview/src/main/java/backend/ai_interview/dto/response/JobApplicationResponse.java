package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Job Application Response
 *
 * Returned in the latest backend-integrated job application flow after:
 * - creating a job application
 * - fetching one or more job applications
 * - linking base and tailored resume versions
 * - processing resume tailoring / AI-engine outputs
 *
 * Contains:
 * - job application metadata
 * - resume version linkage
 * - ATS score comparison
 * - optional tool knowledge answers
 * - optional detected tools / extracted keywords
 * - optional preview generation state
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("all")
public class JobApplicationResponse {

    private boolean success;
    private String message;

    private Long id;
    private String applicationCode;

    private Long baseResumeVersionId;
    private Long tailoredResumeVersionId;
    private String tailoredResumeVersionCode;

    private String companyName;
    private String jobTitle;
    private String applicationSource;

    private String status;

    private Integer atsScoreBefore;
    private Integer atsScoreAfter;

    /**
     * Tool knowledge answers submitted by the user.
     */
    private List<Map<String, Object>> toolAnswers;

    /**
     * Optional tools detected from the job description.
     */
    private List<String> detectedTools;

    /**
     * Optional extracted keywords for ATS / tailoring support.
     */
    private List<String> keywords;

    /**
     * Whether tailored preview was generated.
     */
    private Boolean tailoredPreviewGenerated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplicationResponse() {
    }

    public JobApplicationResponse(
            boolean success,
            String message,
            Long id,
            String applicationCode,
            Long baseResumeVersionId,
            Long tailoredResumeVersionId,
            String tailoredResumeVersionCode,
            String companyName,
            String jobTitle,
            String applicationSource,
            String status,
            Integer atsScoreBefore,
            Integer atsScoreAfter,
            List<Map<String, Object>> toolAnswers,
            List<String> detectedTools,
            List<String> keywords,
            Boolean tailoredPreviewGenerated,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.applicationCode = applicationCode;
        this.baseResumeVersionId = baseResumeVersionId;
        this.tailoredResumeVersionId = tailoredResumeVersionId;
        this.tailoredResumeVersionCode = tailoredResumeVersionCode;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.applicationSource = applicationSource;
        this.status = status;
        this.atsScoreBefore = atsScoreBefore;
        this.atsScoreAfter = atsScoreAfter;
        this.toolAnswers = toolAnswers;
        this.detectedTools = detectedTools;
        this.keywords = keywords;
        this.tailoredPreviewGenerated = tailoredPreviewGenerated;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static JobApplicationResponse success(String message) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static JobApplicationResponse fail(String message) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static JobApplicationResponse of(
            Long id,
            String applicationCode,
            Long baseResumeVersionId,
            Long tailoredResumeVersionId,
            String tailoredResumeVersionCode,
            String companyName,
            String jobTitle,
            String applicationSource,
            String status,
            Integer atsScoreBefore,
            Integer atsScoreAfter,
            List<Map<String, Object>> toolAnswers,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new JobApplicationResponse(
                true,
                "Job application processed successfully",
                id,
                applicationCode,
                baseResumeVersionId,
                tailoredResumeVersionId,
                tailoredResumeVersionCode,
                companyName,
                jobTitle,
                applicationSource,
                status,
                atsScoreBefore,
                atsScoreAfter,
                toolAnswers,
                null,
                null,
                null,
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

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public Long getBaseResumeVersionId() {
        return baseResumeVersionId;
    }

    public void setBaseResumeVersionId(Long baseResumeVersionId) {
        this.baseResumeVersionId = baseResumeVersionId;
    }

    public Long getTailoredResumeVersionId() {
        return tailoredResumeVersionId;
    }

    public void setTailoredResumeVersionId(Long tailoredResumeVersionId) {
        this.tailoredResumeVersionId = tailoredResumeVersionId;
    }

    public String getTailoredResumeVersionCode() {
        return tailoredResumeVersionCode;
    }

    public void setTailoredResumeVersionCode(String tailoredResumeVersionCode) {
        this.tailoredResumeVersionCode = tailoredResumeVersionCode;
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

    public String getApplicationSource() {
        return applicationSource;
    }

    public void setApplicationSource(String applicationSource) {
        this.applicationSource = applicationSource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAtsScoreBefore() {
        return atsScoreBefore;
    }

    public void setAtsScoreBefore(Integer atsScoreBefore) {
        this.atsScoreBefore = atsScoreBefore;
    }

    public Integer getAtsScoreAfter() {
        return atsScoreAfter;
    }

    public void setAtsScoreAfter(Integer atsScoreAfter) {
        this.atsScoreAfter = atsScoreAfter;
    }

    public List<Map<String, Object>> getToolAnswers() {
        return toolAnswers;
    }

    public void setToolAnswers(List<Map<String, Object>> toolAnswers) {
        this.toolAnswers = toolAnswers;
    }

    public List<String> getDetectedTools() {
        return detectedTools;
    }

    public void setDetectedTools(List<String> detectedTools) {
        this.detectedTools = detectedTools;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Boolean getTailoredPreviewGenerated() {
        return tailoredPreviewGenerated;
    }

    public void setTailoredPreviewGenerated(Boolean tailoredPreviewGenerated) {
        this.tailoredPreviewGenerated = tailoredPreviewGenerated;
    }

    public boolean isTailoredPreviewGenerated() {
        return Boolean.TRUE.equals(tailoredPreviewGenerated);
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
        return "JobApplicationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", id=" + id +
                ", applicationCode='" + applicationCode + '\'' +
                ", baseResumeVersionId=" + baseResumeVersionId +
                ", tailoredResumeVersionId=" + tailoredResumeVersionId +
                ", tailoredResumeVersionCode='" + tailoredResumeVersionCode + '\'' +
                ", companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", applicationSource='" + applicationSource + '\'' +
                ", status='" + status + '\'' +
                ", atsScoreBefore=" + atsScoreBefore +
                ", atsScoreAfter=" + atsScoreAfter +
                ", toolAnswers=" + toolAnswers +
                ", detectedTools=" + detectedTools +
                ", keywords=" + keywords +
                ", tailoredPreviewGenerated=" + tailoredPreviewGenerated +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}