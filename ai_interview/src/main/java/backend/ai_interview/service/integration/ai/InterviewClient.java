package backend.ai_interview.service.integration.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import backend.ai_interview.exception.AiIntegrationException;

/**
 * InterviewClient
 *
 * Feature-specific AI integration client for interview lifecycle operations.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - start interview flow with AI-engine
 * - generate next question
 * - evaluate answers
 * - request hints / mock-help
 * - generate final score/summary
 * - normalize AI-engine interview responses into backend-friendly results
 *
 * -------------------------------------------------------------------------
 * EXPECTED AI-ENGINE ENDPOINTS
 * -------------------------------------------------------------------------
 * POST /api/ai/interview/start
 * POST /api/ai/interview/next-question
 * POST /api/ai/interview/evaluate-answer
 * POST /api/ai/interview/mock-help
 * POST /api/ai/interview/final-score
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * 1. This client is intentionally tolerant of response-shape differences.
 * 2. It builds on top of AiEngineClient so services/controllers do not need
 *    to know endpoint paths or raw response parsing details.
 * 3. You can later replace Map-based payloads with strongly typed DTOs.
 */
@Component
@SuppressWarnings("all")
public class InterviewClient {

    private final AiEngineClient aiEngineClient;

    public InterviewClient(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Start AI interview session/question generation using a pre-built payload.
     */
    public InterviewStartResult startInterview(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.startInterview(payload);
            return mapStartResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("QUESTION_GENERATION", ex);
        }
    }

    /**
     * Start AI interview with structured input parameters.
     */
    public InterviewStartResult startInterview(
            String interviewType,
            String interviewMode,
            String role,
            String domain,
            Integer difficulty,
            Integer questionCount,
            String jobDescription,
            String resumeText,
            List<String> githubUrls,
            String preferredLanguage
    ) {
        Map<String, Object> payload = buildStartPayload(
                interviewType,
                interviewMode,
                role,
                domain,
                difficulty,
                questionCount,
                jobDescription,
                resumeText,
                githubUrls,
                preferredLanguage
        );
        return startInterview(payload);
    }

    /**
     * Generate the next question using a pre-built payload.
     */
    public InterviewQuestionResult generateNextQuestion(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.generateNextInterviewQuestion(payload);
            return mapQuestionResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("NEXT_QUESTION", ex);
        }
    }

    /**
     * Generate the next question with structured parameters.
     */
    public InterviewQuestionResult generateNextQuestion(
            Long sessionId,
            Integer currentQuestionIndex,
            String interviewType,
            String role,
            String domain,
            String resumeText,
            String jobDescription,
            List<String> previousQuestions,
            List<Map<String, Object>> history,
            String preferredLanguage,
            Integer difficulty
    ) {
        Map<String, Object> payload = buildNextQuestionPayload(
                sessionId,
                currentQuestionIndex,
                interviewType,
                role,
                domain,
                resumeText,
                jobDescription,
                previousQuestions,
                history,
                preferredLanguage,
                difficulty
        );
        
        log.info("Generating next question for sessionId={}, type={}, role={}", sessionId, interviewType, role);
        log.debug("Next question payload for sessionId={}: {}", sessionId, payload);
        return generateNextQuestion(payload);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InterviewClient.class);

    /**
     * Evaluate interview answer using a pre-built payload.
     */
    public InterviewEvaluationResult evaluateAnswer(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.evaluateInterviewAnswer(payload);
            return mapEvaluationResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("ANSWER_EVALUATION", ex);
        }
    }

    /**
     * Evaluate interview answer with structured parameters.
     */
    public InterviewEvaluationResult evaluateAnswer(
            Long sessionId,
            Long turnId,
            String question,
            String answer,
            String transcript,
            String interviewMode,
            String questionType,
            Integer strictnessLevel
    ) {
        Map<String, Object> payload = buildEvaluationPayload(
                sessionId,
                turnId,
                question,
                answer,
                transcript,
                interviewMode,
                questionType,
                strictnessLevel
        );
        return evaluateAnswer(payload);
    }

    /**
     * Request hint/mock help using a pre-built payload.
     */
    public InterviewHintResult generateHint(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.generateInterviewHint(payload);
            return mapHintResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("INTERVIEW_HINT", ex);
        }
    }

    /**
     * Request hint/mock help with structured parameters.
     */
    public InterviewHintResult generateHint(
            Long sessionId,
            Long turnId,
            String question,
            String partialAnswer,
            String interviewMode,
            String hintType,
            Boolean allowSampleAnswer
    ) {
        Map<String, Object> payload = buildHintPayload(
                sessionId,
                turnId,
                question,
                partialAnswer,
                interviewMode,
                hintType,
                allowSampleAnswer
        );
        return generateHint(payload);
    }

    /**
     * Generate final interview score/summary using a pre-built payload.
     */
    public InterviewFinalScoreResult generateFinalScore(Map<String, Object> payload) {
        try {
            Map<String, Object> response = aiEngineClient.generateFinalInterviewScore(payload);
            return mapFinalScoreResult(response);
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AiIntegrationException.requestFailed("FINAL_SCORING", ex);
        }
    }

    /**
     * Generate final interview score/summary with structured parameters.
     */
    public InterviewFinalScoreResult generateFinalScore(
            Long sessionId,
            String interviewType,
            String interviewMode,
            List<Map<String, Object>> turns,
            String role,
            String domain
    ) {
        Map<String, Object> payload = buildFinalScorePayload(
                sessionId,
                interviewType,
                interviewMode,
                turns,
                role,
                domain
        );
        return generateFinalScore(payload);
    }

    // ---------------------------------------------------------------------
    // Payload builders
    // ---------------------------------------------------------------------

    public Map<String, Object> buildStartPayload(
            String interviewType,
            String interviewMode,
            String role,
            String domain,
            Integer difficulty,
            Integer questionCount,
            String jobDescription,
            String resumeText,
            List<String> githubUrls,
            String preferredLanguage
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("target_role", role != null ? role : domain);
        payload.put("interview_type", trimToNull(interviewType));
        payload.put("difficulty", difficulty != null ? mapDifficulty(difficulty) : "medium");
        payload.put("resume_text", trimToNull(resumeText));
        payload.put("job_description", trimToNull(jobDescription));
        payload.put("language", trimToNull(preferredLanguage));
        
        // Additional context fields
        payload.put("interview_mode", trimToNull(interviewMode));
        payload.put("question_count", questionCount);
        payload.put("github_urls", safeList(githubUrls));
        
        // Initial question index
        payload.put("question_index", 1);
        
        return payload;
    }

    public Map<String, Object> buildNextQuestionPayload(
            Long sessionId,
            Integer currentQuestionIndex,
            String interviewType,
            String role,
            String domain,
            String resumeText,
            String jobDescription,
            List<String> previousQuestions,
            List<Map<String, Object>> history,
            String preferredLanguage,
            Integer difficulty
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        // Note: AI-Engine model InterviewQuestionRequest uses these exact names (mostly snake_case)
        payload.put("target_role", role != null ? role : domain);
        payload.put("interview_type", trimToNull(interviewType));
        payload.put("difficulty", difficulty != null ? mapDifficulty(difficulty) : "medium");
        payload.put("resume_text", trimToNull(resumeText));
        payload.put("job_description", trimToNull(jobDescription));
        payload.put("previous_questions", safeList(previousQuestions));
        payload.put("history", safeList(history));
        payload.put("language", trimToNull(preferredLanguage));
        
        // Optional tracking fields not in Pydantic but useful for logging
        payload.put("session_id", sessionId);
        payload.put("question_index", currentQuestionIndex);
        
        return payload;
    }

    private String mapDifficulty(Integer difficulty) {
        if (difficulty == null) return "medium";
        if (difficulty <= 2) return "easy";
        if (difficulty >= 4) return "hard";
        return "medium";
    }

    public Map<String, Object> buildEvaluationPayload(
            Long sessionId,
            Long turnId,
            String question,
            String answer,
            String transcript,
            String interviewMode,
            String questionType,
            Integer strictnessLevel
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("turnId", turnId);
        payload.put("question", trimToNull(question));
        payload.put("answer", trimToNull(answer));
        payload.put("transcript", trimToNull(transcript));
        payload.put("interviewMode", trimToNull(interviewMode));
        payload.put("questionType", trimToNull(questionType));
        payload.put("strictnessLevel", strictnessLevel);
        return payload;
    }

    public Map<String, Object> buildHintPayload(
            Long sessionId,
            Long turnId,
            String question,
            String partialAnswer,
            String interviewMode,
            String hintType,
            Boolean allowSampleAnswer
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("turnId", turnId);
        payload.put("question", trimToNull(question));
        payload.put("partialAnswer", trimToNull(partialAnswer));
        payload.put("interviewMode", trimToNull(interviewMode));
        payload.put("hintType", trimToNull(hintType));
        payload.put("allowSampleAnswer", allowSampleAnswer);
        return payload;
    }

    public Map<String, Object> buildFinalScorePayload(
            Long sessionId,
            String interviewType,
            String interviewMode,
            List<Map<String, Object>> turns,
            String role,
            String domain
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("interviewType", trimToNull(interviewType));
        payload.put("interviewMode", trimToNull(interviewMode));
        payload.put("turns", turns != null ? turns : new ArrayList<>());
        payload.put("role", trimToNull(role));
        payload.put("domain", trimToNull(domain));
        return payload;
    }

    // ---------------------------------------------------------------------
    // Result mappers
    // ---------------------------------------------------------------------

    private InterviewStartResult mapStartResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("QUESTION_GENERATION");
        }

        InterviewStartResult result = new InterviewStartResult();
        result.setSessionToken(firstString(response, "sessionToken", "conversationId", "sessionId"));
        result.setQuestion(firstString(response, "question", "firstQuestion", "openingQuestion"));
        result.setQuestionType(firstString(response, "questionType", "question_type", "type"));
        result.setCategory(firstString(response, "category", "domain"));
        result.setDifficulty(firstInteger(response, "difficulty", "level"));
        result.setExpectedAnswerTimeSeconds(firstInteger(response, "expectedAnswerTimeSeconds", "expectedTimeSeconds", "answerTime"));
        result.setTargetSkills(firstStringList(response, "targetSkills", "skills"));
        result.setSummary(firstString(response, "summary", "message"));
        result.setRawResponse(response);
        return result;
    }

    private InterviewQuestionResult mapQuestionResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("NEXT_QUESTION");
        }

        InterviewQuestionResult result = new InterviewQuestionResult();
        result.setQuestion(firstString(response, "question", "nextQuestion", "current_question", "currentQuestion"));
        result.setQuestionType(firstString(response, "questionType", "question_type", "type"));
        result.setCategory(firstString(response, "category", "domain"));
        result.setDifficulty(firstInteger(response, "difficulty", "level"));
        result.setQuestionIndex(firstInteger(response, "questionIndex", "question_index", "index"));
        result.setExpectedAnswerTimeSeconds(firstInteger(response, "expectedAnswerTimeSeconds", "expectedTimeSeconds", "answerTime"));
        result.setTargetSkills(firstStringList(response, "targetSkills", "skills"));
        result.setFollowUpHint(firstString(response, "followUpHint", "guidance", "follow_up_suggestions"));
        result.setFinalQuestion(firstBoolean(response, "finalQuestion", "isFinalQuestion"));
        result.setSummary(firstString(response, "summary", "message"));
        result.setRawResponse(response);
        return result;
    }

    private InterviewEvaluationResult mapEvaluationResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("ANSWER_EVALUATION");
        }

        InterviewEvaluationResult result = new InterviewEvaluationResult();
        result.setOverallScore(firstInteger(response, "overallScore", "score", "evaluationScore"));
        result.setConfidenceScore(firstInteger(response, "confidenceScore"));
        result.setKnowledgeScore(firstInteger(response, "knowledgeScore"));
        result.setCommunicationScore(firstInteger(response, "communicationScore"));
        result.setClarityScore(firstInteger(response, "clarityScore"));
        result.setRelevanceScore(firstInteger(response, "relevanceScore"));
        result.setEmotionalComposureScore(firstInteger(response, "emotionalComposureScore", "emotionScore"));
        result.setTechnicalDepthScore(firstInteger(response, "technicalDepthScore"));
        result.setProblemSolvingScore(firstInteger(response, "problemSolvingScore"));

        result.setSummary(firstString(response, "summary", "message"));
        result.setFeedback(firstString(response, "feedback", "evaluation", "analysis"));
        result.setExplanation(firstString(response, "explanation", "reasoning"));
        result.setStrengths(firstStringList(response, "strengths"));
        result.setWeaknesses(firstStringList(response, "weaknesses"));
        result.setSuggestions(firstStringList(response, "suggestions", "recommendations", "improvements"));
        result.setMissingConcepts(firstStringList(response, "missingConcepts", "gaps"));
        result.setDetectedSkills(firstStringList(response, "detectedSkills", "skills"));
        result.setFollowUpQuestion(firstString(response, "followUpQuestion", "nextProbeQuestion"));
        result.setReadyForNextQuestion(firstBoolean(response, "readyForNextQuestion", "canProceed"));
        result.setRawResponse(response);
        return result;
    }

    private InterviewHintResult mapHintResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("INTERVIEW_HINT");
        }

        InterviewHintResult result = new InterviewHintResult();
        result.setHint(firstString(response, "hint", "feedback", "guidance"));
        result.setExplanation(firstString(response, "explanation", "summary"));
        result.setSampleAnswer(firstString(response, "sampleAnswer", "modelAnswer"));
        result.setSuggestions(firstStringList(response, "suggestions", "recommendations", "improvements"));
        result.setHintType(firstString(response, "hintType", "type"));
        result.setSummary(firstString(response, "summary", "message"));
        result.setRawResponse(response);
        return result;
    }

    private InterviewFinalScoreResult mapFinalScoreResult(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw AiIntegrationException.invalidResponse("FINAL_SCORING");
        }

        InterviewFinalScoreResult result = new InterviewFinalScoreResult();
        result.setOverallScore(firstInteger(response, "overallScore", "score", "finalScore"));
        result.setConfidenceScore(firstInteger(response, "confidenceScore"));
        result.setKnowledgeScore(firstInteger(response, "knowledgeScore"));
        result.setCommunicationScore(firstInteger(response, "communicationScore"));
        result.setClarityScore(firstInteger(response, "clarityScore"));
        result.setRelevanceScore(firstInteger(response, "relevanceScore"));
        result.setEmotionalComposureScore(firstInteger(response, "emotionalComposureScore", "emotionScore"));
        result.setTechnicalDepthScore(firstInteger(response, "technicalDepthScore"));
        result.setProblemSolvingScore(firstInteger(response, "problemSolvingScore"));
        result.setProfessionalismScore(firstInteger(response, "professionalismScore"));
        result.setPresenceScore(firstInteger(response, "presenceScore"));

        result.setGrade(firstString(response, "grade"));
        result.setRecommendation(firstString(response, "recommendation", "decision"));
        result.setSummary(firstString(response, "summary", "message", "overallSummary"));
        result.setStrengths(firstStringList(response, "strengths"));
        result.setWeaknesses(firstStringList(response, "weaknesses"));
        result.setSuggestions(firstStringList(response, "suggestions", "recommendations", "improvements"));
        result.setFocusAreas(firstStringList(response, "focusAreas", "missingConcepts", "revisionAreas"));
        result.setRawResponse(response);
        return result;
    }

    // ---------------------------------------------------------------------
    // Response helper readers
    // ---------------------------------------------------------------------

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

    private Boolean firstBoolean(Map<String, Object> response, String... keys) {
        for (String key : keys) {
            Boolean value = aiEngineClient.getBoolean(response, key);
            if (value != null) {
                return value;
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

    private <T> List<T> safeList(List<T> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ---------------------------------------------------------------------
    // Result DTOs
    // ---------------------------------------------------------------------

    public static class InterviewStartResult {
        private String sessionToken;
        private String question;
        private String questionType;
        private String category;
        private Integer difficulty;
        private Integer expectedAnswerTimeSeconds;
        private List<String> targetSkills = new ArrayList<>();
        private String summary;
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getQuestionType() {
            return questionType;
        }

        public void setQuestionType(String questionType) {
            this.questionType = questionType;
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

        public Integer getExpectedAnswerTimeSeconds() {
            return expectedAnswerTimeSeconds;
        }

        public void setExpectedAnswerTimeSeconds(Integer expectedAnswerTimeSeconds) {
            this.expectedAnswerTimeSeconds = expectedAnswerTimeSeconds;
        }

        public List<String> getTargetSkills() {
            return targetSkills;
        }

        public void setTargetSkills(List<String> targetSkills) {
            this.targetSkills = targetSkills != null ? targetSkills : new ArrayList<>();
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }

    public static class InterviewQuestionResult {
        private String question;
        private String questionType;
        private String category;
        private Integer difficulty;
        private Integer questionIndex;
        private Integer expectedAnswerTimeSeconds;
        private List<String> targetSkills = new ArrayList<>();
        private String followUpHint;
        private Boolean finalQuestion;
        private String summary;
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getQuestionType() {
            return questionType;
        }

        public void setQuestionType(String questionType) {
            this.questionType = questionType;
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

        public Integer getQuestionIndex() {
            return questionIndex;
        }

        public void setQuestionIndex(Integer questionIndex) {
            this.questionIndex = questionIndex;
        }

        public Integer getExpectedAnswerTimeSeconds() {
            return expectedAnswerTimeSeconds;
        }

        public void setExpectedAnswerTimeSeconds(Integer expectedAnswerTimeSeconds) {
            this.expectedAnswerTimeSeconds = expectedAnswerTimeSeconds;
        }

        public List<String> getTargetSkills() {
            return targetSkills;
        }

        public void setTargetSkills(List<String> targetSkills) {
            this.targetSkills = targetSkills != null ? targetSkills : new ArrayList<>();
        }

        public String getFollowUpHint() {
            return followUpHint;
        }

        public void setFollowUpHint(String followUpHint) {
            this.followUpHint = followUpHint;
        }

        public Boolean getFinalQuestion() {
            return finalQuestion;
        }

        public void setFinalQuestion(Boolean finalQuestion) {
            this.finalQuestion = finalQuestion;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }

    public static class InterviewEvaluationResult {
        private Integer overallScore;
        private Integer confidenceScore;
        private Integer knowledgeScore;
        private Integer communicationScore;
        private Integer clarityScore;
        private Integer relevanceScore;
        private Integer emotionalComposureScore;
        private Integer technicalDepthScore;
        private Integer problemSolvingScore;
        private String summary;
        private String feedback;
        private String explanation;
        private List<String> strengths = new ArrayList<>();
        private List<String> weaknesses = new ArrayList<>();
        private List<String> suggestions = new ArrayList<>();
        private List<String> missingConcepts = new ArrayList<>();
        private List<String> detectedSkills = new ArrayList<>();
        private String followUpQuestion;
        private Boolean readyForNextQuestion;
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public Integer getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Integer overallScore) {
            this.overallScore = overallScore;
        }

        public Integer getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(Integer confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public Integer getKnowledgeScore() {
            return knowledgeScore;
        }

        public void setKnowledgeScore(Integer knowledgeScore) {
            this.knowledgeScore = knowledgeScore;
        }

        public Integer getCommunicationScore() {
            return communicationScore;
        }

        public void setCommunicationScore(Integer communicationScore) {
            this.communicationScore = communicationScore;
        }

        public Integer getClarityScore() {
            return clarityScore;
        }

        public void setClarityScore(Integer clarityScore) {
            this.clarityScore = clarityScore;
        }

        public Integer getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Integer relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public Integer getEmotionalComposureScore() {
            return emotionalComposureScore;
        }

        public void setEmotionalComposureScore(Integer emotionalComposureScore) {
            this.emotionalComposureScore = emotionalComposureScore;
        }

        public Integer getTechnicalDepthScore() {
            return technicalDepthScore;
        }

        public void setTechnicalDepthScore(Integer technicalDepthScore) {
            this.technicalDepthScore = technicalDepthScore;
        }

        public Integer getProblemSolvingScore() {
            return problemSolvingScore;
        }

        public void setProblemSolvingScore(Integer problemSolvingScore) {
            this.problemSolvingScore = problemSolvingScore;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
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

        public List<String> getMissingConcepts() {
            return missingConcepts;
        }

        public void setMissingConcepts(List<String> missingConcepts) {
            this.missingConcepts = missingConcepts != null ? missingConcepts : new ArrayList<>();
        }

        public List<String> getDetectedSkills() {
            return detectedSkills;
        }

        public void setDetectedSkills(List<String> detectedSkills) {
            this.detectedSkills = detectedSkills != null ? detectedSkills : new ArrayList<>();
        }

        public String getFollowUpQuestion() {
            return followUpQuestion;
        }

        public void setFollowUpQuestion(String followUpQuestion) {
            this.followUpQuestion = followUpQuestion;
        }

        public Boolean getReadyForNextQuestion() {
            return readyForNextQuestion;
        }

        public void setReadyForNextQuestion(Boolean readyForNextQuestion) {
            this.readyForNextQuestion = readyForNextQuestion;
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }

    public static class InterviewHintResult {
        private String hint;
        private String explanation;
        private String sampleAnswer;
        private List<String> suggestions = new ArrayList<>();
        private String hintType;
        private String summary;
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        public String getSampleAnswer() {
            return sampleAnswer;
        }

        public void setSampleAnswer(String sampleAnswer) {
            this.sampleAnswer = sampleAnswer;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        }

        public String getHintType() {
            return hintType;
        }

        public void setHintType(String hintType) {
            this.hintType = hintType;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }

    public static class InterviewFinalScoreResult {
        private Integer overallScore;
        private Integer confidenceScore;
        private Integer knowledgeScore;
        private Integer communicationScore;
        private Integer clarityScore;
        private Integer relevanceScore;
        private Integer emotionalComposureScore;
        private Integer technicalDepthScore;
        private Integer problemSolvingScore;
        private Integer professionalismScore;
        private Integer presenceScore;
        private String grade;
        private String recommendation;
        private String summary;
        private List<String> strengths = new ArrayList<>();
        private List<String> weaknesses = new ArrayList<>();
        private List<String> suggestions = new ArrayList<>();
        private List<String> focusAreas = new ArrayList<>();
        private Map<String, Object> rawResponse = new LinkedHashMap<>();

        public Integer getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Integer overallScore) {
            this.overallScore = overallScore;
        }

        public Integer getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(Integer confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public Integer getKnowledgeScore() {
            return knowledgeScore;
        }

        public void setKnowledgeScore(Integer knowledgeScore) {
            this.knowledgeScore = knowledgeScore;
        }

        public Integer getCommunicationScore() {
            return communicationScore;
        }

        public void setCommunicationScore(Integer communicationScore) {
            this.communicationScore = communicationScore;
        }

        public Integer getClarityScore() {
            return clarityScore;
        }

        public void setClarityScore(Integer clarityScore) {
            this.clarityScore = clarityScore;
        }

        public Integer getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Integer relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public Integer getEmotionalComposureScore() {
            return emotionalComposureScore;
        }

        public void setEmotionalComposureScore(Integer emotionalComposureScore) {
            this.emotionalComposureScore = emotionalComposureScore;
        }

        public Integer getTechnicalDepthScore() {
            return technicalDepthScore;
        }

        public void setTechnicalDepthScore(Integer technicalDepthScore) {
            this.technicalDepthScore = technicalDepthScore;
        }

        public Integer getProblemSolvingScore() {
            return problemSolvingScore;
        }

        public void setProblemSolvingScore(Integer problemSolvingScore) {
            this.problemSolvingScore = problemSolvingScore;
        }

        public Integer getProfessionalismScore() {
            return professionalismScore;
        }

        public void setProfessionalismScore(Integer professionalismScore) {
            this.professionalismScore = professionalismScore;
        }

        public Integer getPresenceScore() {
            return presenceScore;
        }

        public void setPresenceScore(Integer presenceScore) {
            this.presenceScore = presenceScore;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
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

        public List<String> getFocusAreas() {
            return focusAreas;
        }

        public void setFocusAreas(List<String> focusAreas) {
            this.focusAreas = focusAreas != null ? focusAreas : new ArrayList<>();
        }

        public Map<String, Object> getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(Map<String, Object> rawResponse) {
            this.rawResponse = rawResponse != null ? rawResponse : new LinkedHashMap<>();
        }
    }
}