package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * GitHubProjectAnalysisRequest
 *
 * Request DTO used when frontend/backend wants AI-backed analysis
 * of a GitHub project/repository.
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * Frontend -> Backend:
 *   POST /api/github/analyze
 *   POST /api/github/analyze/resume-project
 *
 * Backend:
 *   - validates repository/project input
 *   - optionally fetches README / repo metadata / comments
 *   - optionally calls AI-engine for deeper semantic analysis
 *   - returns structured project insights
 *
 * -------------------------------------------------------------------------
 * SUPPORTED USE CASES
 * -------------------------------------------------------------------------
 * 1. Analyze a direct GitHub repository URL
 * 2. Analyze a resume-linked project
 * 3. Generate interview-oriented project insights
 * 4. Extract likely stack / skills / complexity
 * 5. Compare project with a target job description
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - repositoryUrl is the primary required field
 * - other fields are optional context enhancers
 * - backend should decide what to trust and what to fetch from GitHub
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later add:
 * - pull request analysis
 * - branch-specific analysis
 * - contributor activity analysis
 * - commit-history analysis
 * - issue sentiment analysis
 */
@SuppressWarnings("all")
public class GitHubProjectAnalysisRequest {

    /**
     * Primary GitHub repository URL.
     *
     * Example:
     * - https://github.com/user/project
     */
    @NotBlank(message = "Repository URL is required")
    @Size(max = 1000, message = "Repository URL must not exceed 1000 characters")
    private String repositoryUrl;

    /**
     * Optional repository owner/username.
     * Example:
     * - torvalds
     * - octocat
     */
    @Size(max = 255, message = "Owner username must not exceed 255 characters")
    private String owner;

    /**
     * Optional repository name.
     * Example:
     * - linux
     * - hello-world
     */
    @Size(max = 255, message = "Repository name must not exceed 255 characters")
    private String repositoryName;

    /**
     * Optional project title from resume/editor.
     * Example:
     * - AI Interview System
     * - Resume Analyzer
     */
    @Size(max = 255, message = "Project title must not exceed 255 characters")
    private String projectTitle;

    /**
     * Optional project description already supplied by frontend/resume.
     * Backend/AI can use this as additional context.
     */
    @Size(max = 10000, message = "Project description must not exceed 10000 characters")
    private String projectDescription;

    /**
     * Optional resume id for linking analysis to a user's resume.
     */
    private Long resumeId;

    /**
     * Optional resume version id for linking analysis to a specific version.
     */
    private Long resumeVersionId;

    /**
     * Optional project id from a stored resume project section.
     * Can be used later if you persist project rows separately.
     */
    private Long resumeProjectId;

    /**
     * Optional target role/job title for interview-oriented analysis.
     * Example:
     * - Backend Developer
     * - SDE Intern
     */
    @Size(max = 255, message = "Target role must not exceed 255 characters")
    private String targetRole;

    /**
     * Optional job description for relevance comparison.
     */
    @Size(max = 15000, message = "Job description must not exceed 15000 characters")
    private String jobDescription;

    /**
     * Optional list of technologies already known from resume/editor.
     */
    private List<String> declaredTechnologies = new ArrayList<>();

    /**
     * Optional branch name.
     * If omitted, backend may use default branch.
     */
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    private String branchName;

    /**
     * Whether backend should analyze README content.
     */
    private Boolean includeReadmeAnalysis = Boolean.TRUE;

    /**
     * Whether backend should analyze repository metadata
     * such as stars, forks, primary language, topics.
     */
    private Boolean includeRepositoryMetadata = Boolean.TRUE;

    /**
     * Whether backend should inspect code/file tree at a shallow level.
     */
    private Boolean includeFileStructureAnalysis = Boolean.TRUE;

    /**
     * Whether backend should attempt interview-question generation
     * from the project analysis context.
     */
    private Boolean generateInterviewInsights = Boolean.TRUE;

    /**
     * Whether backend should extract skills/tech stack from the repo.
     */
    private Boolean extractSkills = Boolean.TRUE;

    /**
     * Optional analysis mode.
     *
     * Example values:
     * - BASIC
     * - DETAILED
     * - INTERVIEW_FOCUSED
     * - RESUME_VALIDATION
     */
    @Size(max = 50, message = "Analysis mode must not exceed 50 characters")
    private String analysisMode;

    /**
     * Optional client timestamp for tracing/debugging.
     */
    @Size(max = 100, message = "Client timestamp must not exceed 100 characters")
    private String clientTimestamp;

    public GitHubProjectAnalysisRequest() {
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
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

    public Long getResumeProjectId() {
        return resumeProjectId;
    }

    public void setResumeProjectId(Long resumeProjectId) {
        this.resumeProjectId = resumeProjectId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public List<String> getDeclaredTechnologies() {
        return declaredTechnologies;
    }

    public void setDeclaredTechnologies(List<String> declaredTechnologies) {
        this.declaredTechnologies = declaredTechnologies != null ? declaredTechnologies : new ArrayList<>();
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Boolean getIncludeReadmeAnalysis() {
        return includeReadmeAnalysis;
    }

    public void setIncludeReadmeAnalysis(Boolean includeReadmeAnalysis) {
        this.includeReadmeAnalysis = includeReadmeAnalysis;
    }

    public Boolean getIncludeRepositoryMetadata() {
        return includeRepositoryMetadata;
    }

    public void setIncludeRepositoryMetadata(Boolean includeRepositoryMetadata) {
        this.includeRepositoryMetadata = includeRepositoryMetadata;
    }

    public Boolean getIncludeFileStructureAnalysis() {
        return includeFileStructureAnalysis;
    }

    public void setIncludeFileStructureAnalysis(Boolean includeFileStructureAnalysis) {
        this.includeFileStructureAnalysis = includeFileStructureAnalysis;
    }

    public Boolean getGenerateInterviewInsights() {
        return generateInterviewInsights;
    }

    public void setGenerateInterviewInsights(Boolean generateInterviewInsights) {
        this.generateInterviewInsights = generateInterviewInsights;
    }

    public Boolean getExtractSkills() {
        return extractSkills;
    }

    public void setExtractSkills(Boolean extractSkills) {
        this.extractSkills = extractSkills;
    }

    public String getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(String analysisMode) {
        this.analysisMode = analysisMode;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }
}