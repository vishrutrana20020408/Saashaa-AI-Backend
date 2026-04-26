package backend.ai_interview.service.integration.ai;

import backend.ai_interview.exception.AiIntegrationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ResumeTailoringClient
 *
 * Feature-specific AI integration client for resume tailoring.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - call AI-engine resume tailoring endpoint
 * - normalize tailoring request payloads
 * - extract structured tailoring insights from AI response
 * - provide backend-friendly result objects
 *
 * -------------------------------------------------------------------------
 * EXPECTED AI-ENGINE ENDPOINT
 * -------------------------------------------------------------------------
 * POST /api/ai/resume/tailor
 *
 * Suggested request payload:
 * {
 *   "resumeText": "...",
 *   "jobDescription": "...",
 *   "resumeTitle": "...",
 *   "skills": ["Java", "Spring Boot"],
 *   "projects": ["AI Interview System", "Resume Analyzer"],
 *   "experience": ["Built backend APIs ..."],
 *   "toolAnswers": ["I used Docker for local deployment"],
 *   "tailoringMode": "DETAILED"
 * }
 *
 * Suggested AI response shape (flexible):
 * {
 *   "tailoredResumeText": "...",
 *   "summary": "Resume tailored for backend role",
 *   "matchedKeywords": ["Java", "Spring Boot", "REST API"],
 *   "addedKeywords": ["Docker"],
 *   "removedKeywords": ["Irrelevant keyword"],
 *   "suggestions": ["Add more quantified impact"],
 *   "sectionsUpdated": ["Summary", "Skills", "Projects"],
 *   "score": 88
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
public class ResumeTailoringClient {

    private final AiEngineClient aiEngineClient;

    public ResumeTailoringClient(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Tailor resume using a pre-built payload.
     *
     * @param payload AI-engine request payload
     * @return normalized tailoring result
     */
    public ResumeTailoringResult tailor(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.tailorResume(payload);
            return mapResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("RESUME_TAILORING", ex);
        }
    }

    /**
     * Tailor resume using structured input parameters.
     */
    public ResumeTailoringResult tailor(
            String resumeText,
            String jobDescription,
            String resumeTitle,
            List<String> skills,
            List<String> projects,
            List<String> experience,
            List<String> toolAnswers,
            String tailoringMode
    ) {
        Map<String, Object> payload = buildPayload(
                resumeText,
                jobDescription,
                resumeTitle,
                skills,
                projects,
                experience,
                toolAnswers,
                tailoringMode
        );
        return tailor(payload);
    }

    /**
     * Build a standard resume tailoring payload.
     */
    public Map<String, Object> buildPayload(
            String resumeText,
            String jobDescription,
            String resumeTitle,
            List<String> skills,
            List<String> projects,
            List<String> experience,
            List<String> toolAnswers,
            String tailoringMode
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("resumeText", trimToNull(resumeText));
        payload.put("jobDescription", trimToNull(jobDescription));
        payload.put("resumeTitle", trimToNull(resumeTitle));
        payload.put("skills", safeList(skills));
        payload.put("projects", safeList(projects));
        payload.put("experience", safeList(experience));
        payload.put("toolAnswers", safeList(toolAnswers));
        payload.put("tailoringMode", firstNonBlank(tailoringMode, "DETAILED"));
        return payload;
    }

    /**
     * Extract only tailored resume text.
     */
    public String tailoredTextOnly(Map<String, Object> payload) {
        return tailor(payload).getTailoredResumeText();
    }

    /**
     * Extract only tailoring score.
     */
    public Integer scoreOnly(Map<String, Object> payload) {
        return tailor(payload).getScore();
    }

    /**
     * Extract only tailoring suggestions.
     */
    public List<String> suggestionsOnly(Map<String, Object> payload) {
        return tailor(payload).getSuggestions();
    }

    private ResumeTailoringResult mapResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("RESUME_TAILORING");
        }

        ResumeTailoringResult result = new ResumeTailoringResult();

        result.setScore(firstInteger(response,
                "score", "tailoringScore", "overallScore", "atsScore"));

        result.setSummary(firstString(response,
                "summary", "message", "tailoringSummary", "feedback"));

        result.setTailoredResumeText(firstString(response,
                "tailoredResumeText", "tailoredText", "resumeText", "optimizedResumeText"));

        result.setTailoredSummary(firstString(response,
                "tailoredSummary", "optimizedSummary", "updatedSummary"));

        result.setMatchedKeywords(firstStringList(response,
                "matchedKeywords", "matchedSkills", "matched_terms"));

        result.setAddedKeywords(firstStringList(response,
                "addedKeywords", "addedSkills", "insertedKeywords"));

        result.setRemovedKeywords(firstStringList(response,
                "removedKeywords", "removedSkills", "deletedKeywords"));

        result.setSuggestions(firstStringList(response,
                "suggestions", "recommendations", "improvements"));

        result.setSectionsUpdated(firstStringList(response,
                "sectionsUpdated", "updatedSections", "modifiedSections"));

        result.setProjectImprovements(firstStringList(response,
                "projectImprovements", "projectSuggestions", "projectsUpdated"));

        result.setExperienceImprovements(firstStringList(response,
                "experienceImprovements", "experienceSuggestions", "experienceUpdated"));

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
     * Normalized resume tailoring result used by backend services.
     */
    public static class ResumeTailoringResult {

        /**
         * Optional overall tailoring score.
         */
        private Integer score;

        /**
         * Short tailoring summary.
         */
        private String summary;

        /**
         * Tailored/optimized full resume text.
         */
        private String tailoredResumeText;

        /**
         * Tailored summary/objective section if separately returned.
         */
        private String tailoredSummary;

        /**
         * Keywords already well matched to the job description.
         */
        private List<String> matchedKeywords = new ArrayList<>();

        /**
         * Keywords added or recommended during tailoring.
         */
        private List<String> addedKeywords = new ArrayList<>();

        /**
         * Keywords removed or deprioritized during tailoring.
         */
        private List<String> removedKeywords = new ArrayList<>();

        /**
         * Tailoring suggestions or recommendations.
         */
        private List<String> suggestions = new ArrayList<>();

        /**
         * Resume sections updated by AI tailoring.
         */
        private List<String> sectionsUpdated = new ArrayList<>();

        /**
         * Improvements suggested/applied to projects section.
         */
        private List<String> projectImprovements = new ArrayList<>();

        /**
         * Improvements suggested/applied to experience section.
         */
        private List<String> experienceImprovements = new ArrayList<>();

        /**
         * Optional role-fit assessment after tailoring.
         */
        private String roleFit;

        /**
         * Full raw AI response for debugging or downstream custom handling.
         */
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public ResumeTailoringResult() {
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

        public String getTailoredResumeText() {
            return tailoredResumeText;
        }

        public void setTailoredResumeText(String tailoredResumeText) {
            this.tailoredResumeText = tailoredResumeText;
        }

        public String getTailoredSummary() {
            return tailoredSummary;
        }

        public void setTailoredSummary(String tailoredSummary) {
            this.tailoredSummary = tailoredSummary;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords != null ? matchedKeywords : new ArrayList<>();
        }

        public List<String> getAddedKeywords() {
            return addedKeywords;
        }

        public void setAddedKeywords(List<String> addedKeywords) {
            this.addedKeywords = addedKeywords != null ? addedKeywords : new ArrayList<>();
        }

        public List<String> getRemovedKeywords() {
            return removedKeywords;
        }

        public void setRemovedKeywords(List<String> removedKeywords) {
            this.removedKeywords = removedKeywords != null ? removedKeywords : new ArrayList<>();
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        }

        public List<String> getSectionsUpdated() {
            return sectionsUpdated;
        }

        public void setSectionsUpdated(List<String> sectionsUpdated) {
            this.sectionsUpdated = sectionsUpdated != null ? sectionsUpdated : new ArrayList<>();
        }

        public List<String> getProjectImprovements() {
            return projectImprovements;
        }

        public void setProjectImprovements(List<String> projectImprovements) {
            this.projectImprovements = projectImprovements != null ? projectImprovements : new ArrayList<>();
        }

        public List<String> getExperienceImprovements() {
            return experienceImprovements;
        }

        public void setExperienceImprovements(List<String> experienceImprovements) {
            this.experienceImprovements = experienceImprovements != null ? experienceImprovements : new ArrayList<>();
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