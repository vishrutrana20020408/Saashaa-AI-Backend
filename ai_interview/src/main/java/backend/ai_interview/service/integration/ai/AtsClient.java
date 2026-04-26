package backend.ai_interview.service.integration.ai;

import backend.ai_interview.exception.AiIntegrationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AtsClient
 *
 * Feature-specific AI integration client for ATS scoring and resume optimization.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - call AI-engine ATS scoring endpoint
 * - normalize ATS request payloads
 * - safely extract ATS response fields
 * - provide a backend-friendly result structure
 *
 * -------------------------------------------------------------------------
 * EXPECTED AI-ENGINE ENDPOINT
 * -------------------------------------------------------------------------
 * POST /api/ai/ats/score
 *
 * Suggested request payload:
 * {
 *   "resumeText": "...",
 *   "jobDescription": "...",
 *   "resumeTitle": "...",
 *   "skills": ["Java", "Spring Boot"],
 *   "projects": ["..."],
 *   "experience": ["..."]
 * }
 *
 * Suggested AI response shape (flexible):
 * {
 *   "score": 82,
 *   "summary": "Good ATS alignment",
 *   "matchedKeywords": ["Java", "REST API"],
 *   "missingKeywords": ["Docker", "Kubernetes"],
 *   "suggestions": ["Add more cloud keywords"]
 * }
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * This client builds on top of AiEngineClient so higher-level services do not
 * need to manually deal with raw endpoint paths or response parsing.
 */
@Component
@SuppressWarnings("all")
public class AtsClient {

    private final AiEngineClient aiEngineClient;

    public AtsClient(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Score ATS using a pre-built payload map.
     *
     * @param payload request payload for AI-engine
     * @return normalized ATS result
     */
    public AtsScoreResult score(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.scoreAts(payload);
            return mapResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("ATS_SCORING", ex);
        }
    }

    /**
     * Score ATS using structured method parameters.
     *
     * @param resumeText full resume text
     * @param jobDescription target job description
     * @param resumeTitle optional resume title
     * @param skills optional extracted skill list
     * @param projects optional project summaries/titles
     * @param experience optional experience summaries
     * @return normalized ATS result
     */
    public AtsScoreResult score(
            String resumeText,
            String jobDescription,
            List<String> skills
    ) {
        Map<String, Object> payload = buildPayload(
                resumeText,
                jobDescription,
                null, // resumeTitle
                skills,
                null, // projects
                null, // experience
                null  // githubUrl
        );
        return score(payload);
    }

    /**
     * Build a standard ATS scoring payload.
     */
    public Map<String, Object> buildPayload(
            String resumeText,
            String jobDescription,
            String resumeTitle,
            List<String> skills,
            List<String> projects,
            List<String> experience,
            String githubUrl
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("resumeText", trimToNull(resumeText));
        payload.put("jobDescription", trimToNull(jobDescription));
        payload.put("resumeTitle", trimToNull(resumeTitle));
        payload.put("skills", safeList(skills));
        payload.put("projects", safeList(projects));
        payload.put("experience", safeList(experience));
        payload.put("github_url", trimToNull(githubUrl));
        return payload;
    }

    /**
     * Extract only the score from ATS response.
     */
    public Integer scoreOnly(Map<String, Object> payload) {
        return score(payload).getScore();
    }

    /**
     * Extract only suggestions from ATS response.
     */
    public List<String> suggestionsOnly(Map<String, Object> payload) {
        return score(payload).getSuggestions();
    }

    /**
     * Convert raw response map from AI-engine into a normalized result object.
     */
    private AtsScoreResult mapResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("ATS_SCORING");
        }

        AtsScoreResult result = new AtsScoreResult();

        Integer score = firstInteger(response,
                "score", "atsScore", "overallScore", "matchScore");
        result.setScore(score);

        String summary = firstString(response,
                "summary", "message", "analysisSummary", "feedback");
        result.setSummary(summary);

        result.setMatchedKeywords(firstStringList(response,
                "matchedKeywords", "matched_terms", "matchedSkills"));
        result.setMissingKeywords(firstStringList(response,
                "missingKeywords", "missing_terms", "missingSkills"));
        result.setSuggestions(firstStringList(response,
                "suggestions", "recommendations", "improvements"));

        result.setImprovedResumeText(firstString(response,
                "improved_resume_text", "improvedResumeText", "optimizedText"));

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

    /**
     * Normalized ATS scoring result used by backend services.
     */
    public static class AtsScoreResult {

        /**
         * ATS score, typically out of 100.
         */
        private Integer score;

        /**
         * Short summary of ATS analysis.
         */
        private String summary;

        /**
         * Keywords/skills well matched against job description.
         */
        private List<String> matchedKeywords = new ArrayList<>();

        /**
         * Important keywords/skills missing from the resume.
         */
        private List<String> missingKeywords = new ArrayList<>();

        /**
         * Improvement suggestions.
         */
        private List<String> suggestions = new ArrayList<>();

        /**
         * Improved resume text (target score 9-10).
         */
        private String improvedResumeText;

        /**
         * Full raw response for debugging or downstream custom handling.
         */
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public AtsScoreResult() {
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

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords != null ? matchedKeywords : new ArrayList<>();
        }

        public List<String> getMissingKeywords() {
            return missingKeywords;
        }

        public void setMissingKeywords(List<String> missingKeywords) {
            this.missingKeywords = missingKeywords != null ? missingKeywords : new ArrayList<>();
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        }

        public String getImprovedResumeText() {
            return improvedResumeText;
        }

        public void setImprovedResumeText(String improvedResumeText) {
            this.improvedResumeText = improvedResumeText;
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }
}