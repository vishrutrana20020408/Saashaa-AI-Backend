package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHubProjectAnalysisResponse
 *
 * Response DTO returned after backend/AI-engine analysis of a GitHub repository.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - POST /api/github/analyze
 * - POST /api/github/analyze/resume-project
 *
 * -------------------------------------------------------------------------
 * FRONTEND USE
 * -------------------------------------------------------------------------
 * - show repository/project summary
 * - display detected tech stack
 * - show AI-generated strengths and risks
 * - generate interview-oriented talking points
 * - compare project with resume or job role
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - This DTO is intentionally rich because GitHub analysis can power:
 *   1. resume enrichment
 *   2. interview question generation
 *   3. skill extraction
 *   4. project credibility analysis
 * - Raw GitHub data should generally be processed before reaching frontend
 */
@SuppressWarnings("all")
public class GitHubProjectAnalysisResponse {

    /**
     * Original repository URL analyzed.
     */
    private String repositoryUrl;

    /**
     * Repository owner/user/org.
     */
    private String owner;

    /**
     * Repository name.
     */
    private String repositoryName;

    /**
     * Optional project title from resume/editor.
     */
    private String projectTitle;

    /**
     * Whether repository analysis completed successfully.
     */
    private Boolean analyzed;

    /**
     * Optional analysis mode used.
     * Example:
     * - BASIC
     * - DETAILED
     * - INTERVIEW_FOCUSED
     * - RESUME_VALIDATION
     */
    private String analysisMode;

    /**
     * Short AI-generated project summary.
     */
    private String summary;

    /**
     * Longer explanation/analysis body.
     */
    private String detailedAnalysis;

    /**
     * Optional README summary.
     */
    private String readmeSummary;

    /**
     * Optional detected purpose/use-case.
     */
    private String projectPurpose;

    /**
     * Optional detected complexity level.
     * Example:
     * - BEGINNER
     * - INTERMEDIATE
     * - ADVANCED
     */
    private String complexityLevel;

    /**
     * Optional overall project quality score.
     * Usually 0-100.
     */
    private Integer overallScore;

    /**
     * Optional resume relevance score.
     */
    private Integer resumeRelevanceScore;

    /**
     * Optional job-role relevance score.
     */
    private Integer roleRelevanceScore;

    /**
     * Optional code structure / repository organization score.
     */
    private Integer structureScore;

    /**
     * Optional documentation score.
     */
    private Integer documentationScore;

    /**
     * Optional technical depth score.
     */
    private Integer technicalDepthScore;

    /**
     * Optional originality/uniqueness score.
     */
    private Integer originalityScore;

    /**
     * Primary language reported by repository.
     */
    private String primaryLanguage;

    /**
     * Detected tech stack.
     */
    private List<String> detectedTechnologies = new ArrayList<>();

    /**
     * Skills inferred from repository.
     */
    private List<String> extractedSkills = new ArrayList<>();

    /**
     * Repository topics/tags if available.
     */
    private List<String> repositoryTopics = new ArrayList<>();

    /**
     * Key strengths of the project.
     */
    private List<String> strengths = new ArrayList<>();

    /**
     * Weaknesses / issues / concerns.
     */
    private List<String> weaknesses = new ArrayList<>();

    /**
     * Risks or red flags.
     */
    private List<String> risks = new ArrayList<>();

    /**
     * Improvement suggestions for the project itself.
     */
    private List<String> improvementSuggestions = new ArrayList<>();

    /**
     * Why this project is good to discuss in interviews.
     */
    private List<String> interviewTalkingPoints = new ArrayList<>();

    /**
     * Suggested interview questions that could be asked from this project.
     */
    private List<String> suggestedInterviewQuestions = new ArrayList<>();

    /**
     * Concepts the candidate should be ready to explain.
     */
    private List<String> keyConceptsToExplain = new ArrayList<>();

    /**
     * Claimed tech from resume but not strongly found in repo context.
     */
    private List<String> missingOrUnclearTechnologies = new ArrayList<>();

    /**
     * Optional notable features detected.
     */
    private List<String> notableFeatures = new ArrayList<>();

    /**
     * Optional architecture/design notes inferred from repo.
     */
    private List<String> architectureNotes = new ArrayList<>();

    /**
     * Optional relevant files discovered during analysis.
     */
    private List<RepositoryFileInfo> relevantFiles = new ArrayList<>();

    /**
     * Optional repository metadata snapshot.
     */
    private RepositoryMetadata metadata;

    /**
     * Optional link to corresponding resume.
     */
    private Long resumeId;

    /**
     * Optional link to corresponding resume version.
     */
    private Long resumeVersionId;

    /**
     * Optional link to corresponding resume project entry.
     */
    private Long resumeProjectId;

    /**
     * Optional target role used during analysis.
     */
    private String targetRole;

    /**
     * Whether README was included in analysis.
     */
    private Boolean readmeAnalyzed;

    /**
     * Whether repository metadata was included in analysis.
     */
    private Boolean repositoryMetadataAnalyzed;

    /**
     * Whether file structure was included in analysis.
     */
    private Boolean fileStructureAnalyzed;

    /**
     * Timestamp of analysis generation.
     */
    private LocalDateTime analyzedAt;

    /**
     * Optional generic message for frontend.
     */
    private String message;

    /**
     * Optional warnings encountered during analysis.
     */
    private List<String> warnings = new ArrayList<>();

    public GitHubProjectAnalysisResponse() {
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

    public Boolean getAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(Boolean analyzed) {
        this.analyzed = analyzed;
    }

    public String getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(String analysisMode) {
        this.analysisMode = analysisMode;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetailedAnalysis() {
        return detailedAnalysis;
    }

    public void setDetailedAnalysis(String detailedAnalysis) {
        this.detailedAnalysis = detailedAnalysis;
    }

    public String getReadmeSummary() {
        return readmeSummary;
    }

    public void setReadmeSummary(String readmeSummary) {
        this.readmeSummary = readmeSummary;
    }

    public String getProjectPurpose() {
        return projectPurpose;
    }

    public void setProjectPurpose(String projectPurpose) {
        this.projectPurpose = projectPurpose;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getResumeRelevanceScore() {
        return resumeRelevanceScore;
    }

    public void setResumeRelevanceScore(Integer resumeRelevanceScore) {
        this.resumeRelevanceScore = resumeRelevanceScore;
    }

    public Integer getRoleRelevanceScore() {
        return roleRelevanceScore;
    }

    public void setRoleRelevanceScore(Integer roleRelevanceScore) {
        this.roleRelevanceScore = roleRelevanceScore;
    }

    public Integer getStructureScore() {
        return structureScore;
    }

    public void setStructureScore(Integer structureScore) {
        this.structureScore = structureScore;
    }

    public Integer getDocumentationScore() {
        return documentationScore;
    }

    public void setDocumentationScore(Integer documentationScore) {
        this.documentationScore = documentationScore;
    }

    public Integer getTechnicalDepthScore() {
        return technicalDepthScore;
    }

    public void setTechnicalDepthScore(Integer technicalDepthScore) {
        this.technicalDepthScore = technicalDepthScore;
    }

    public Integer getOriginalityScore() {
        return originalityScore;
    }

    public void setOriginalityScore(Integer originalityScore) {
        this.originalityScore = originalityScore;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public List<String> getDetectedTechnologies() {
        return detectedTechnologies;
    }

    public void setDetectedTechnologies(List<String> detectedTechnologies) {
        this.detectedTechnologies = detectedTechnologies != null ? detectedTechnologies : new ArrayList<>();
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
    }

    public List<String> getRepositoryTopics() {
        return repositoryTopics;
    }

    public void setRepositoryTopics(List<String> repositoryTopics) {
        this.repositoryTopics = repositoryTopics != null ? repositoryTopics : new ArrayList<>();
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? strengths : new ArrayList<>();
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks != null ? risks : new ArrayList<>();
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions != null ? improvementSuggestions : new ArrayList<>();
    }

    public List<String> getInterviewTalkingPoints() {
        return interviewTalkingPoints;
    }

    public void setInterviewTalkingPoints(List<String> interviewTalkingPoints) {
        this.interviewTalkingPoints = interviewTalkingPoints != null ? interviewTalkingPoints : new ArrayList<>();
    }

    public List<String> getSuggestedInterviewQuestions() {
        return suggestedInterviewQuestions;
    }

    public void setSuggestedInterviewQuestions(List<String> suggestedInterviewQuestions) {
        this.suggestedInterviewQuestions = suggestedInterviewQuestions != null ? suggestedInterviewQuestions : new ArrayList<>();
    }

    public List<String> getKeyConceptsToExplain() {
        return keyConceptsToExplain;
    }

    public void setKeyConceptsToExplain(List<String> keyConceptsToExplain) {
        this.keyConceptsToExplain = keyConceptsToExplain != null ? keyConceptsToExplain : new ArrayList<>();
    }

    public List<String> getMissingOrUnclearTechnologies() {
        return missingOrUnclearTechnologies;
    }

    public void setMissingOrUnclearTechnologies(List<String> missingOrUnclearTechnologies) {
        this.missingOrUnclearTechnologies = missingOrUnclearTechnologies != null ? missingOrUnclearTechnologies : new ArrayList<>();
    }

    public List<String> getNotableFeatures() {
        return notableFeatures;
    }

    public void setNotableFeatures(List<String> notableFeatures) {
        this.notableFeatures = notableFeatures != null ? notableFeatures : new ArrayList<>();
    }

    public List<String> getArchitectureNotes() {
        return architectureNotes;
    }

    public void setArchitectureNotes(List<String> architectureNotes) {
        this.architectureNotes = architectureNotes != null ? architectureNotes : new ArrayList<>();
    }

    public List<RepositoryFileInfo> getRelevantFiles() {
        return relevantFiles;
    }

    public void setRelevantFiles(List<RepositoryFileInfo> relevantFiles) {
        this.relevantFiles = relevantFiles != null ? relevantFiles : new ArrayList<>();
    }

    public RepositoryMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(RepositoryMetadata metadata) {
        this.metadata = metadata;
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

    public Boolean getReadmeAnalyzed() {
        return readmeAnalyzed;
    }

    public void setReadmeAnalyzed(Boolean readmeAnalyzed) {
        this.readmeAnalyzed = readmeAnalyzed;
    }

    public Boolean getRepositoryMetadataAnalyzed() {
        return repositoryMetadataAnalyzed;
    }

    public void setRepositoryMetadataAnalyzed(Boolean repositoryMetadataAnalyzed) {
        this.repositoryMetadataAnalyzed = repositoryMetadataAnalyzed;
    }

    public Boolean getFileStructureAnalyzed() {
        return fileStructureAnalyzed;
    }

    public void setFileStructureAnalyzed(Boolean fileStructureAnalyzed) {
        this.fileStructureAnalyzed = fileStructureAnalyzed;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    /**
     * Repository metadata snapshot.
     */
    public static class RepositoryMetadata {

        private String defaultBranch;
        private String primaryLanguage;
        private Integer stars;
        private Integer forks;
        private Integer openIssues;
        private Integer watchers;
        private Boolean isPrivate;
        private Boolean isFork;
        private String licenseName;
        private String homepageUrl;
        private String createdAt;
        private String updatedAt;
        private String pushedAt;

        public RepositoryMetadata() {
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }

        public String getPrimaryLanguage() {
            return primaryLanguage;
        }

        public void setPrimaryLanguage(String primaryLanguage) {
            this.primaryLanguage = primaryLanguage;
        }

        public Integer getStars() {
            return stars;
        }

        public void setStars(Integer stars) {
            this.stars = stars;
        }

        public Integer getForks() {
            return forks;
        }

        public void setForks(Integer forks) {
            this.forks = forks;
        }

        public Integer getOpenIssues() {
            return openIssues;
        }

        public void setOpenIssues(Integer openIssues) {
            this.openIssues = openIssues;
        }

        public Integer getWatchers() {
            return watchers;
        }

        public void setWatchers(Integer watchers) {
            this.watchers = watchers;
        }

        public Boolean getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(Boolean isPrivate) {
            this.isPrivate = isPrivate;
        }

        public Boolean getIsFork() {
            return isFork;
        }

        public void setIsFork(Boolean isFork) {
            this.isFork = isFork;
        }

        public String getLicenseName() {
            return licenseName;
        }

        public void setLicenseName(String licenseName) {
            this.licenseName = licenseName;
        }

        public String getHomepageUrl() {
            return homepageUrl;
        }

        public void setHomepageUrl(String homepageUrl) {
            this.homepageUrl = homepageUrl;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getPushedAt() {
            return pushedAt;
        }

        public void setPushedAt(String pushedAt) {
            this.pushedAt = pushedAt;
        }
    }

    /**
     * Relevant file summary discovered during repository analysis.
     */
    public static class RepositoryFileInfo {

        private String path;
        private String type;
        private String purpose;
        private Boolean important;
        private String summary;

        public RepositoryFileInfo() {
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public Boolean getImportant() {
            return important;
        }

        public void setImportant(Boolean important) {
            this.important = important;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}