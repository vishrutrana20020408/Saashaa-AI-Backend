package backend.ai_interview.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Job Application Create Request
 *
 * Used in the latest backend-integrated job application flow where a user
 * applies to a company using an existing resume version and the system may
 * generate a tailored duplicate aligned with the resume tailoring module.
 *
 * The backend may:
 * - create a job application record
 * - use the selected resume version as the base version
 * - generate a tailored duplicate resume version
 * - integrate tool knowledge answers into the tailoring flow
 * - optionally generate preview-ready output
 *
 * Example payload:
 * {
 *   "resumeVersionId": 10,
 *   "companyName": "Google",
 *   "jobTitle": "Backend Software Engineer",
 *   "jobDescription": "Full JD text...",
 *   "applicationSource": "LinkedIn",
 *   "additionalNotes": "Interested in distributed systems role",
 *   "generateTailoredResume": true,
 *   "generatePreview": true,
 *   "toolAnswers": [
 *     {
 *       "jobApplicationId": 0,
 *       "toolName": "Docker",
 *       "required": true,
 *       "userKnowsTool": true,
 *       "userExperienceLevel": "INTERMEDIATE",
 *       "notes": "Used in internship"
 *     }
 *   ]
 * }
 */
@SuppressWarnings("all")
public class JobApplicationCreateRequest {

    /**
     * Base resume version selected by the user.
     * This version should remain unchanged.
     */
    @NotNull(message = "Resume version id is required")
    private Long resumeVersionId;

    /**
     * Company name where the user is applying.
     */
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    /**
     * Job title or role.
     */
    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    private String jobTitle;

    /**
     * Full job description.
     */
    @NotBlank(message = "Job description is required")
    @Size(max = 50000, message = "Job description is too large")
    private String jobDescription;

    /**
     * Optional source of application like LinkedIn, Naukri, Company Portal.
     */
    @Size(max = 100, message = "Application source must not exceed 100 characters")
    private String applicationSource;

    /**
     * Optional user notes for this application.
     */
    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    private String additionalNotes;

    /**
     * Whether a tailored resume duplicate should be generated.
     */
    private Boolean generateTailoredResume;

    /**
     * Whether a preview should be generated immediately.
     */
    private Boolean generatePreview;

    /**
     * Tool knowledge answers given by the user.
     * These align with the resume tailoring / AI-engine flow.
     */
    @Valid
    private List<ToolKnowledgeAnswerRequest> toolAnswers;

    public JobApplicationCreateRequest() {
        this.generateTailoredResume = Boolean.TRUE;
        this.generatePreview = Boolean.TRUE;
        this.toolAnswers = new ArrayList<>();
    }

    public JobApplicationCreateRequest(
            Long resumeVersionId,
            String companyName,
            String jobTitle,
            String jobDescription,
            String applicationSource,
            String additionalNotes,
            Boolean generateTailoredResume,
            Boolean generatePreview,
            List<ToolKnowledgeAnswerRequest> toolAnswers
    ) {
        this.resumeVersionId = resumeVersionId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.applicationSource = applicationSource;
        this.additionalNotes = additionalNotes;
        this.generateTailoredResume = generateTailoredResume != null ? generateTailoredResume : Boolean.TRUE;
        this.generatePreview = generatePreview != null ? generatePreview : Boolean.TRUE;
        this.toolAnswers = toolAnswers != null ? toolAnswers : new ArrayList<>();
    }

    public Long getResumeVersionId() {
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

    public String getApplicationSource() {
        return applicationSource;
    }

    public void setApplicationSource(String applicationSource) {
        this.applicationSource = applicationSource;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public Boolean getGenerateTailoredResume() {
        return generateTailoredResume;
    }

    public void setGenerateTailoredResume(Boolean generateTailoredResume) {
        this.generateTailoredResume = generateTailoredResume;
    }

    public Boolean getGeneratePreview() {
        return generatePreview;
    }

    public void setGeneratePreview(Boolean generatePreview) {
        this.generatePreview = generatePreview;
    }

    public List<ToolKnowledgeAnswerRequest> getToolAnswers() {
        return toolAnswers;
    }

    public void setToolAnswers(List<ToolKnowledgeAnswerRequest> toolAnswers) {
        this.toolAnswers = toolAnswers != null ? toolAnswers : new ArrayList<>();
    }

    public boolean shouldGenerateTailoredResume() {
        return Boolean.TRUE.equals(generateTailoredResume);
    }

    public boolean shouldGeneratePreview() {
        return Boolean.TRUE.equals(generatePreview);
    }

    @Override
    public String toString() {
        return "JobApplicationCreateRequest{" +
                "resumeVersionId=" + resumeVersionId +
                ", companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", jobDescriptionLength=" + (jobDescription != null ? jobDescription.length() : 0) +
                ", applicationSource='" + applicationSource + '\'' +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", generateTailoredResume=" + generateTailoredResume +
                ", generatePreview=" + generatePreview +
                ", toolAnswersCount=" + (toolAnswers != null ? toolAnswers.size() : 0) +
                '}';
    }
}