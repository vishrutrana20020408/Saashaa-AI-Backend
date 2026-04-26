package backend.ai_interview.service.integration.ai;

import backend.ai_interview.exception.AiIntegrationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHubAnalysisClient
 *
 * Feature-specific AI integration client for GitHub repository/project analysis.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - call AI-engine GitHub analysis endpoint
 * - normalize GitHub analysis request payloads
 * - extract structured project insights from AI response
 * - provide backend-friendly result objects
 *
 * -------------------------------------------------------------------------
 * EXPECTED AI-ENGINE ENDPOINT
 * -------------------------------------------------------------------------
 * POST /api/ai/github/analyze
 *
 * Suggested request payload:
 * {
 *   "repositoryUrl": "https://github.com/user/project",
 *   "owner": "user",
 *   "repositoryName": "project",
 *   "projectTitle": "AI Interview System",
 *   "projectDescription": "A full stack interview platform ...",
 *   "targetRole": "Backend Developer",
 *   "jobDescription": "...",
 *   "declaredTechnologies": ["Java", "Spring Boot", "AWS S3"],
 *   "analysisMode": "INTERVIEW_FOCUSED"
 * }
 *
 * Suggested AI response shape (flexible):
 * {
 *   "summary": "Strong backend-focused project",
 *   "detailedAnalysis": "...",
 *   "overallScore": 84,
 *   "detectedTechnologies": ["Java", "Spring Boot", "MySQL"],
 *   "extractedSkills": ["Backend Development", "API Design"],
 *   "strengths": ["Good project relevance"],
 *   "weaknesses": ["Limited documentation"],
 *   "risks": ["Architecture explanation may be weak"],
 *   "suggestions": ["Add deployment notes"],
 *   "interviewTalkingPoints": ["Explain S3 upload flow"],
 *   "suggestedInterviewQuestions": ["How does the backend talk to S3?"],
 *   "keyConceptsToExplain": ["Layered architecture"],
 *   "readmeSummary": "...",
 *   "complexityLevel": "INTERMEDIATE"
 * }
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * This client is intentionally tolerant of response-shape differences.
 * It can be tightened later once the AI-engine response contract is finalized.
 */
@Component
@SuppressWarnings("all")
public class GitHubAnalysisClient {

    private final AiEngineClient aiEngineClient;

    public GitHubAnalysisClient(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Analyze GitHub project using a pre-built payload.
     *
     * @param payload AI-engine request payload
     * @return normalized analysis result
     */
    public GitHubAnalysisResult analyze(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.analyzeGitHubProject(payload);
            return mapResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("GITHUB_ANALYSIS", ex);
        }
    }

    /**
     * Analyze GitHub project using structured input parameters.
     */
    public GitHubAnalysisResult analyze(
            String repositoryUrl,
            String owner,
            String repositoryName,
            String projectTitle,
            String projectDescription,
            String targetRole,
            String jobDescription,
            List<String> declaredTechnologies,
            String analysisMode
    ) {
        Map<String, Object> payload = buildPayload(
                repositoryUrl,
                owner,
                repositoryName,
                projectTitle,
                projectDescription,
                targetRole,
                jobDescription,
                declaredTechnologies,
                analysisMode
        );
        return analyze(payload);
    }

    /**
     * Build a standard GitHub analysis payload.
     */
    public Map<String, Object> buildPayload(
            String repositoryUrl,
            String owner,
            String repositoryName,
            String projectTitle,
            String projectDescription,
            String targetRole,
            String jobDescription,
            List<String> declaredTechnologies,
            String analysisMode
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("repositoryUrl", trimToNull(repositoryUrl));
        payload.put("owner", trimToNull(owner));
        payload.put("repositoryName", trimToNull(repositoryName));
        payload.put("projectTitle", trimToNull(projectTitle));
        payload.put("projectDescription", trimToNull(projectDescription));
        payload.put("targetRole", trimToNull(targetRole));
        payload.put("jobDescription", trimToNull(jobDescription));
        payload.put("declaredTechnologies", safeList(declaredTechnologies));
        payload.put("analysisMode", firstNonBlank(analysisMode, "INTERVIEW_FOCUSED"));
        return payload;
    }

    /**
     * Extract only summary from GitHub analysis.
     */
    public String summaryOnly(Map<String, Object> payload) {
        return analyze(payload).getSummary();
    }

    /**
     * Extract only overall score from GitHub analysis.
     */
    public Integer scoreOnly(Map<String, Object> payload) {
        return analyze(payload).getOverallScore();
    }

    /**
     * Extract only interview talking points.
     */
    public List<String> talkingPointsOnly(Map<String, Object> payload) {
        return analyze(payload).getInterviewTalkingPoints();
    }

    private GitHubAnalysisResult mapResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("GITHUB_ANALYSIS");
        }

        GitHubAnalysisResult result = new GitHubAnalysisResult();

        result.setSummary(firstString(response,
                "summary", "message", "analysisSummary"));

        result.setDetailedAnalysis(firstString(response,
                "detailedAnalysis", "analysis", "details", "fullAnalysis"));

        result.setReadmeSummary(firstString(response,
                "readmeSummary", "readmeAnalysis"));

        result.setProjectPurpose(firstString(response,
                "projectPurpose", "purpose"));

        result.setComplexityLevel(firstString(response,
                "complexityLevel", "complexity"));

        result.setOverallScore(firstInteger(response,
                "overallScore", "score", "analysisScore"));

        result.setResumeRelevanceScore(firstInteger(response,
                "resumeRelevanceScore", "resumeScore"));

        result.setRoleRelevanceScore(firstInteger(response,
                "roleRelevanceScore", "roleScore"));

        result.setStructureScore(firstInteger(response,
                "structureScore"));

        result.setDocumentationScore(firstInteger(response,
                "documentationScore"));

        result.setTechnicalDepthScore(firstInteger(response,
                "technicalDepthScore"));

        result.setOriginalityScore(firstInteger(response,
                "originalityScore"));

        result.setPrimaryLanguage(firstString(response,
                "primaryLanguage", "language"));

        result.setDetectedTechnologies(firstStringList(response,
                "detectedTechnologies", "technologies", "techStack"));

        result.setExtractedSkills(firstStringList(response,
                "extractedSkills", "skills"));

        result.setRepositoryTopics(firstStringList(response,
                "repositoryTopics", "topics", "tags"));

        result.setStrengths(firstStringList(response,
                "strengths", "positivePoints", "highlights"));

        result.setWeaknesses(firstStringList(response,
                "weaknesses", "gaps", "issues"));

        result.setRisks(firstStringList(response,
                "risks", "redFlags"));

        result.setSuggestions(firstStringList(response,
                "suggestions", "recommendations", "improvements"));

        result.setInterviewTalkingPoints(firstStringList(response,
                "interviewTalkingPoints", "talkingPoints"));

        result.setSuggestedInterviewQuestions(firstStringList(response,
                "suggestedInterviewQuestions", "interviewQuestions"));

        result.setKeyConceptsToExplain(firstStringList(response,
                "keyConceptsToExplain", "conceptsToExplain"));

        result.setMissingOrUnclearTechnologies(firstStringList(response,
                "missingOrUnclearTechnologies", "missingTechnologies", "unclearTechnologies"));

        result.setNotableFeatures(firstStringList(response,
                "notableFeatures", "features"));

        result.setArchitectureNotes(firstStringList(response,
                "architectureNotes", "architectureInsights"));

        result.setRawResponse(response);

        return result;
    }

    private Integer firstInteger(Map<String, Object> response, String... keys) {
        for (String key : keys) {
            Integer value = aiEngineClient.getInteger(response, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstString(Map<String, Object> response, String... keys) {
        for (String key : keys) {
            String value = aiEngineClient.getString(response, key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private List<String> firstStringList(Map<String, Object> response, String... keys) {
        for (String key : keys) {
            List<Object> list = aiEngineClient.getList(response, key);
            if (list != null) {
                return toStringList(list);
            }
        }
        return new ArrayList<>();
    }

    private List<String> toStringList(List<Object> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }

        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty() && !result.contains(text)) {
                result.add(text);
            }
        }
        return result;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * Normalized GitHub analysis result used by backend services.
     */
    public static class GitHubAnalysisResult {

        /**
         * Short AI-generated project summary.
         */
        private String summary;

        /**
         * Longer analysis body.
         */
        private String detailedAnalysis;

        /**
         * Optional README-focused summary.
         */
        private String readmeSummary;

        /**
         * Optional inferred project purpose.
         */
        private String projectPurpose;

        /**
         * Optional inferred complexity level.
         */
        private String complexityLevel;

        /**
         * Overall project analysis score.
         */
        private Integer overallScore;

        /**
         * Resume relevance score.
         */
        private Integer resumeRelevanceScore;

        /**
         * Role relevance score.
         */
        private Integer roleRelevanceScore;

        /**
         * Structure score.
         */
        private Integer structureScore;

    /**
         * Documentation score.
         */
        private Integer documentationScore;

        /**
         * Technical depth score.
         */
        private Integer technicalDepthScore;

        /**
         * Originality score.
         */
        private Integer originalityScore;

        /**
         * Primary language.
         */
        private String primaryLanguage;

        /**
         * Technologies detected from analysis.
         */
        private List<String> detectedTechnologies = new ArrayList<>();

        /**
         * Extracted skills from the repository/project.
         */
        private List<String> extractedSkills = new ArrayList<>();

        /**
         * Repository topics/tags.
         */
        private List<String> repositoryTopics = new ArrayList<>();

        /**
         * Strengths observed in project analysis.
         */
        private List<String> strengths = new ArrayList<>();

        /**
         * Weaknesses/gaps observed in project analysis.
         */
        private List<String> weaknesses = new ArrayList<>();

        /**
         * Risks/red flags observed in project analysis.
         */
        private List<String> risks = new ArrayList<>();

        /**
         * Suggestions for improvement.
         */
        private List<String> suggestions = new ArrayList<>();

        /**
         * Interview-oriented talking points.
         */
        private List<String> interviewTalkingPoints = new ArrayList<>();

        /**
         * Suggested interview questions from project context.
         */
        private List<String> suggestedInterviewQuestions = new ArrayList<>();

        /**
         * Important concepts the candidate should be ready to explain.
         */
        private List<String> keyConceptsToExplain = new ArrayList<>();

        /**
         * Technologies that are missing or unclear.
         */
        private List<String> missingOrUnclearTechnologies = new ArrayList<>();

        /**
         * Notable features found in analysis.
         */
        private List<String> notableFeatures = new ArrayList<>();

        /**
         * Architecture-related notes.
         */
        private List<String> architectureNotes = new ArrayList<>();

        /**
         * Full raw AI response for debugging or downstream custom handling.
         */
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public GitHubAnalysisResult() {
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

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
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

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }
}