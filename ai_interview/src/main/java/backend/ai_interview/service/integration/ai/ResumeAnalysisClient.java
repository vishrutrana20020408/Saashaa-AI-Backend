package backend.ai_interview.service.integration.ai;

import backend.ai_interview.exception.AiIntegrationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ResumeAnalysisClient
 *
 * Feature-specific AI integration client for resume analysis.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - call AI-engine resume analysis endpoint
 * - normalize resume analysis payloads
 * - extract structured insights from AI response
 * - provide backend-friendly result objects
 *
 * -------------------------------------------------------------------------
 * EXPECTED AI-ENGINE ENDPOINT
 * -------------------------------------------------------------------------
 * POST /api/ai/resume/analyze
 *
 * Suggested request payload:
 * {
 *   "resumeText": "...",
 *   "resumeTitle": "...",
 *   "jobDescription": "...",
 *   "skills": ["Java", "Spring Boot"],
 *   "projects": ["AI Interview System", "Resume Analyzer"],
 *   "experience": ["Built backend APIs ..."],
 *   "analysisMode": "DETAILED"
 * }
 *
 * Suggested AI response shape (flexible):
 * {
 *   "summary": "Resume is strong for backend roles",
 *   "strengths": ["Strong Java backend experience"],
 *   "weaknesses": ["Cloud details are limited"],
 *   "suggestions": ["Add measurable impact"],
 *   "detectedSkills": ["Java", "Spring Boot", "REST API"],
 *   "missingSkills": ["Docker", "Kubernetes"],
 *   "projectInsights": ["Good real-world project alignment"],
 *   "experienceInsights": ["Backend focus is clear"],
 *   "score": 81
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
public class ResumeAnalysisClient {

    private final AiEngineClient aiEngineClient;

    public ResumeAnalysisClient(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Analyze resume using a pre-built payload.
     *
     * @param payload AI-engine request payload
     * @return normalized analysis result
     */
    public ResumeAnalysisResult analyze(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.analyzeResume(payload);
            return mapResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("RESUME_ANALYSIS", ex);
        }
    }

    /**
     * Analyze resume using structured input parameters.
     */
    public ResumeAnalysisResult analyze(
            String resumeText,
            String resumeTitle,
            String jobDescription,
            List<String> skills,
            List<String> projects,
            List<String> experience,
            String analysisMode
    ) {
        Map<String, Object> payload = buildPayload(
                resumeText,
                resumeTitle,
                jobDescription,
                skills,
                projects,
                experience,
                analysisMode
        );
        return analyze(payload);
    }

    /**
     * Build a standard resume analysis payload.
     */
    public Map<String, Object> buildPayload(
            String resumeText,
            String resumeTitle,
            String jobDescription,
            List<String> skills,
            List<String> projects,
            List<String> experience,
            String analysisMode
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("resumeText", trimToNull(resumeText));
        payload.put("resumeTitle", trimToNull(resumeTitle));
        payload.put("jobDescription", trimToNull(jobDescription));
        payload.put("skills", safeList(skills));
        payload.put("projects", safeList(projects));
        payload.put("experience", safeList(experience));
        payload.put("analysisMode", firstNonBlank(analysisMode, "DETAILED"));
        return payload;
    }

    /**
     * Extract only summary from resume analysis.
     */
    public String summaryOnly(Map<String, Object> payload) {
        return analyze(payload).getSummary();
    }

    /**
     * Extract only detected skills from resume analysis.
     */
    public List<String> detectedSkillsOnly(Map<String, Object> payload) {
        return analyze(payload).getDetectedSkills();
    }

    /**
     * Extract only score from resume analysis.
     */
    public Integer scoreOnly(Map<String, Object> payload) {
        return analyze(payload).getScore();
    }

    private ResumeAnalysisResult mapResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("RESUME_ANALYSIS");
        }

        ResumeAnalysisResult result = new ResumeAnalysisResult();

        result.setScore(firstInteger(response,
                "score", "overallScore", "analysisScore", "resumeScore"));

        result.setSummary(firstString(response,
                "summary", "message", "analysisSummary", "feedback"));

        result.setDetailedAnalysis(firstString(response,
                "detailedAnalysis", "details", "analysis", "fullAnalysis"));

        result.setStrengths(firstStringList(response,
                "strengths", "positivePoints", "highlights"));

        result.setWeaknesses(firstStringList(response,
                "weaknesses", "gaps", "issues"));

        result.setSuggestions(firstStringList(response,
                "suggestions", "recommendations", "improvements"));

        result.setDetectedSkills(firstStringList(response,
                "detectedSkills", "skills", "extractedSkills"));

        result.setMissingSkills(firstStringList(response,
                "missingSkills", "missingKeywords", "missingTechnologies"));

        result.setProjectInsights(firstStringList(response,
                "projectInsights", "projectsAnalysis", "projectHighlights"));

        result.setExperienceInsights(firstStringList(response,
                "experienceInsights", "experienceAnalysis", "experienceHighlights"));

        result.setRoleFit(firstString(response,
                "roleFit", "targetRoleFit", "fitAssessment"));

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
     * Normalized resume analysis result used by backend services.
     */
    public static class ResumeAnalysisResult {

        /**
         * Optional overall analysis score.
         */
        private Integer score;

        /**
         * Short summary of resume analysis.
         */
        private String summary;

        /**
         * Detailed AI analysis body.
         */
        private String detailedAnalysis;

        /**
         * Strengths found in the resume.
         */
        private List<String> strengths = new ArrayList<>();

        /**
         * Weaknesses/gaps found in the resume.
         */
        private List<String> weaknesses = new ArrayList<>();

        /**
         * Suggestions for improving the resume.
         */
        private List<String> suggestions = new ArrayList<>();

        /**
         * Skills detected from resume content.
         */
        private List<String> detectedSkills = new ArrayList<>();

        /**
         * Missing skills/keywords the resume may lack.
         */
        private List<String> missingSkills = new ArrayList<>();

        /**
         * Insights specific to project section/content.
         */
        private List<String> projectInsights = new ArrayList<>();

        /**
         * Insights specific to experience section/content.
         */
        private List<String> experienceInsights = new ArrayList<>();

        /**
         * Optional role-fit assessment.
         */
        private String roleFit;

        /**
         * Full raw AI response for debugging or downstream custom handling.
         */
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public ResumeAnalysisResult() {
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
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

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        }

        public List<String> getDetectedSkills() {
            return detectedSkills;
        }

        public void setDetectedSkills(List<String> detectedSkills) {
            this.detectedSkills = detectedSkills != null ? detectedSkills : new ArrayList<>();
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public void setMissingSkills(List<String> missingSkills) {
            this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
        }

        public List<String> getProjectInsights() {
            return projectInsights;
        }

        public void setProjectInsights(List<String> projectInsights) {
            this.projectInsights = projectInsights != null ? projectInsights : new ArrayList<>();
        }

        public List<String> getExperienceInsights() {
            return experienceInsights;
        }

        public void setExperienceInsights(List<String> experienceInsights) {
            this.experienceInsights = experienceInsights != null ? experienceInsights : new ArrayList<>();
        }

        public String getRoleFit() {
            return roleFit;
        }

        public void setRoleFit(String roleFit) {
            this.roleFit = roleFit;
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }
}