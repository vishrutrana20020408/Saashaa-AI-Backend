package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Resume Tailor Request
 *
 * Used in the latest resume tailoring flow where a user tailors
 * a resume version for a specific job application.
 *
 * This request stays aligned with:
 * - `/api/user/resume/tailor/*` backend endpoints
 * - duplicate-and-tailor resume version flow
 * - AI-engine processing for JD/tool alignment
 * - optional preview generation after tailoring
 *
 * Example payload:
 *
 * {
 *   "resumeVersionId": 10,
 *   "companyName": "Google",
 *   "jobTitle": "Backend Software Engineer",
 *   "jobDescription": "... full JD ...",
 *   "knownTools": ["Java", "Spring Boot", "Docker"],
 *   "unknownTools": ["Kubernetes"],
 *   "additionalNotes": "Worked with distributed systems"
 * }
 */
@SuppressWarnings("all")
public class ResumeTailorRequest {

    /**
     * Base resume version that will be duplicated and tailored.
     */
    @NotNull(message = "Resume version id is required")
    private Long resumeVersionId;

    /**
     * Company name for the application.
     */
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    /**
     * Job title / role.
     */
    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String jobTitle;

    /**
     * Full job description used for tailoring.
     */
    @NotBlank(message = "Job description is required")
    @Size(max = 50000, message = "Job description is too large")
    private String jobDescription;

    /**
     * Tools/skills the user confirmed they know.
     */
    private List<String> knownTools;

    /**
     * Tools extracted from the job description that the user does not know.
     */
    private List<String> unknownTools;

    /**
     * Optional notes provided by user.
     */
    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    private String additionalNotes;

    /**
     * Optional flag to generate preview immediately.
     */
    private Boolean generatePreview;

    public ResumeTailorRequest() {
        this.generatePreview = Boolean.TRUE;
    }

    public ResumeTailorRequest(
            Long resumeVersionId,
            String companyName,
            String jobTitle,
            String jobDescription,
            List<String> knownTools,
            List<String> unknownTools,
            String additionalNotes,
            Boolean generatePreview
    ) {
        this.resumeVersionId = resumeVersionId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.knownTools = knownTools;
        this.unknownTools = unknownTools;
        this.additionalNotes = additionalNotes;
        this.generatePreview = generatePreview != null ? generatePreview : Boolean.TRUE;
    }

    public @NotNull(message = "Resume version id is required") Long getResumeVersionId() {
        return resumeVersionId;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.resumeVersionId = resumeVersionId;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public List<String> getKnownTools() {
        return knownTools;
    }

    public void setKnownTools(List<String> knownTools) {
        this.knownTools = knownTools;
    }

    public List<String> getUnknownTools() {
        return unknownTools;
    }

    public void setUnknownTools(List<String> unknownTools) {
        this.unknownTools = unknownTools;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public Boolean getGeneratePreview() {
        return generatePreview;
    }

    public void setGeneratePreview(Boolean generatePreview) {
        this.generatePreview = generatePreview;
    }

    public boolean shouldGeneratePreview() {
        return Boolean.TRUE.equals(generatePreview);
    }

    @Override
    public String toString() {
        return "ResumeTailorRequest{" +
                "resumeVersionId=" + resumeVersionId +
                ", companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", jobDescriptionLength=" + (jobDescription != null ? jobDescription.length() : 0) +
                ", knownTools=" + knownTools +
                ", unknownTools=" + unknownTools +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", generatePreview=" + generatePreview +
                '}';
    }
}