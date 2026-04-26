package backend.ai_interview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * InterviewStartRequest
 *
 * Request DTO used to start a new AI interview session.
 *
 * This request controls:
 * - interview type (technical / HR / mixed)
 * - interview mode (mock / real)
 * - difficulty level
 * - duration / number of questions
 * - context sources (resume / job description / github projects)
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * Frontend → Backend:
 *  POST /api/interview/sessions/start
 *
 * Backend:
 *  - validates request
 *  - creates InterviewSession
 *  - calls AI-engine to generate first question
 *  - returns session + first question
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Resume & GitHub data help AI ask personalized questions
 * - Job description helps align interview with real-world roles
 * - Language input is flexible (AI understands Indian languages)
 *   but response will be in English
 */
@SuppressWarnings("all")
public class InterviewStartRequest {

    /**
     * Interview type:
     * Example:
     * - TECHNICAL
     * - HR
     * - MIXED
     */
    @NotBlank(message = "Interview type is required")
    private String interviewType;

    /**
     * Interview mode:
     * - MOCK  -> AI helps, gives suggestions
     * - REAL  -> strict evaluation
     */
    @NotBlank(message = "Interview mode is required")
    private String interviewMode;

    /**
     * Optional role/job title for targeted interview.
     * Example:
     * - "Java Backend Developer"
     * - "Frontend Engineer"
     */
    @Size(max = 255, message = "Role must not exceed 255 characters")
    private String role;

    /**
     * Optional domain/subdomain.
     * Example:
     * - "Backend"
     * - "AI/ML"
     * - "Web Development"
     */
    @Size(max = 255, message = "Domain must not exceed 255 characters")
    private String domain;

    /**
     * Optional category for the interview.
     * Example:
     * - "Job Profile"
     * - "Mock Interview"
     * - "Technical Assessment"
     */
    @Size(max = 255, message = "Category must not exceed 255 characters")
    private String category;

    /**
     * Difficulty level (1–5).
     * 1 = very easy
     * 5 = very hard
     */
    @Min(value = 1, message = "Difficulty must be at least 1")
    @Max(value = 5, message = "Difficulty must be at most 5")
    private Integer difficulty = 3;

    /**
     * Number of questions in the interview.
     */
    @Min(value = 1, message = "Question count must be at least 1")
    @Max(value = 50, message = "Question count must not exceed 50")
    private Integer questionCount = 10;

    /**
     * Optional interview duration in minutes.
     */
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 180, message = "Duration must not exceed 180 minutes")
    private Integer durationMinutes;

    /**
     * Resume ID (if interview should be based on resume).
     */
    private Long resumeId;

    /**
     * Resume version ID (if specific version is used).
     */
    private Long resumeVersionId;

    /**
     * Optional job description for targeted interview.
     */
    @Size(max = 10000, message = "Job description too long")
    private String jobDescription;

    /**
     * Optional GitHub repository links for project-based questions.
     */
    private List<String> githubUrls;

    /**
     * Preferred spoken input language of the user.
     * AI will understand this but respond in English.
     *
     * Example:
     * - "Hindi"
     * - "English"
     * - "Hinglish"
     */
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String preferredLanguage;

    /**
     * Flag to include behavioral questions.
     */
    private Boolean includeBehavioral = Boolean.TRUE;

    /**
     * Flag to include coding/problem-solving questions.
     */
    private Boolean includeTechnical = Boolean.TRUE;

    /**
     * Flag to allow AI hints (especially for mock interviews).
     */
    private Boolean allowHints = Boolean.TRUE;

    public InterviewStartRequest() {
    }

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public String getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(String interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public List<String> getGithubUrls() {
        return githubUrls;
    }

    public void setGithubUrls(List<String> githubUrls) {
        this.githubUrls = githubUrls;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Boolean getIncludeBehavioral() {
        return includeBehavioral;
    }

    public void setIncludeBehavioral(Boolean includeBehavioral) {
        this.includeBehavioral = includeBehavioral;
    }

    public Boolean getIncludeTechnical() {
        return includeTechnical;
    }

    public void setIncludeTechnical(Boolean includeTechnical) {
        this.includeTechnical = includeTechnical;
    }

    public Boolean getAllowHints() {
        return allowHints;
    }

    public void setAllowHints(Boolean allowHints) {
        this.allowHints = allowHints;
    }
}