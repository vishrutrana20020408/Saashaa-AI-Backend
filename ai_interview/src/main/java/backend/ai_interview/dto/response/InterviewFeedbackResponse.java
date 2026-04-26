package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InterviewFeedbackResponse
 *
 * Response DTO used after:
 * - answer submission
 * - hint request
 * - explicit evaluation
 *
 * It supports both:
 * - quick per-answer feedback
 * - richer mock interview guidance
 * - partial scoring across multiple interview aspects
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - POST /api/interview/sessions/{sessionId}/answer
 * - POST /api/interview/sessions/{sessionId}/hint
 * - POST /api/interview/sessions/{sessionId}/evaluate
 *
 * -------------------------------------------------------------------------
 * FRONTEND USE
 * -------------------------------------------------------------------------
 * - show immediate AI feedback
 * - show hint/mock-help response
 * - display aspect-wise scores
 * - show improvement suggestions
 * - optionally show generated next step
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - In MOCK mode, this can include stronger guidance and sample answer help
 * - In REAL mode, this should remain stricter and avoid over-helping
 */
@SuppressWarnings("all")
public class InterviewFeedbackResponse {

    /**
     * Related interview session id.
     */
    private Long sessionId;

    /**
     * Related interview turn id.
     */
    private Long turnId;

    /**
     * Optional question id.
     */
    private Long questionId;

    /**
     * Response type.
     * Example:
     * - ANSWER_FEEDBACK
     * - HINT
     * - EVALUATION
     * - MOCK_HELP
     */
    private String responseType;

    /**
     * Main user-facing feedback text.
     */
    private String feedback;

    /**
     * Short summary for UI cards/badges.
     */
    private String summary;

    /**
     * Optional hint text.
     */
    private String hint;

    /**
     * Optional explanation text.
     */
    private String explanation;

    /**
     * Optional model/sample answer.
     * Usually allowed only in mock mode.
     */
    private String sampleAnswer;

    /**
     * Whether sample answer is included.
     */
    private Boolean sampleAnswerIncluded;

    /**
     * Whether hint was provided.
     */
    private Boolean hintProvided;

    /**
     * Whether the submitted answer was evaluated.
     */
    private Boolean evaluated;

    /**
     * Overall score for this turn/answer.
     * Usually 0-100.
     */
    private Integer overallScore;

    /**
     * Confidence score.
     */
    private Integer confidenceScore;

    /**
     * Knowledge score.
     */
    private Integer knowledgeScore;

    /**
     * Communication score.
     */
    private Integer communicationScore;

    /**
     * Clarity score.
     */
    private Integer clarityScore;

    /**
     * Relevance score.
     */
    private Integer relevanceScore;

    /**
     * Emotional composure / calmness score.
     */
    private Integer emotionalComposureScore;

    /**
     * Technical depth score.
     */
    private Integer technicalDepthScore;

    /**
     * Problem solving score.
     */
    private Integer problemSolvingScore;

    /**
     * Optional strengths observed in the answer.
     */
    private List<String> strengths = new ArrayList<>();

    /**
     * Optional weaknesses/gaps observed in the answer.
     */
    private List<String> weaknesses = new ArrayList<>();

    /**
     * Improvement suggestions for the candidate.
     */
    private List<String> improvementSuggestions = new ArrayList<>();

    /**
     * Optional keywords/skills detected in the answer.
     */
    private List<String> detectedSkills = new ArrayList<>();

    /**
     * Optional missing concepts that should have been mentioned.
     */
    private List<String> missingConcepts = new ArrayList<>();

    /**
     * Optional rubric notes from evaluator/AI.
     */
    private List<String> rubricNotes = new ArrayList<>();

    /**
     * Whether user skipped the question.
     */
    private Boolean skipped;

    /**
     * Whether hint/help was used before feedback generation.
     */
    private Boolean hintUsed;

    /**
     * Optional next-step guidance.
     * Example:
     * - "Try answering with a real project example."
     */
    private String nextStepSuggestion;

    /**
     * Optional suggested follow-up question.
     */
    private String followUpQuestion;

    /**
     * Whether backend recommends moving to next question.
     */
    private Boolean readyForNextQuestion;

    /**
     * Optional generated timestamp.
     */
    private LocalDateTime generatedAt;

    /**
     * Optional generic frontend message.
     */
    private String message;

    public InterviewFeedbackResponse() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getTurnId() {
        return turnId;
    }

    public void setTurnId(Long turnId) {
        this.turnId = turnId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

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

    public Boolean getSampleAnswerIncluded() {
        return sampleAnswerIncluded;
    }

    public void setSampleAnswerIncluded(Boolean sampleAnswerIncluded) {
        this.sampleAnswerIncluded = sampleAnswerIncluded;
    }

    public Boolean getHintProvided() {
        return hintProvided;
    }

    public void setHintProvided(Boolean hintProvided) {
        this.hintProvided = hintProvided;
    }

    public Boolean getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(Boolean evaluated) {
        this.evaluated = evaluated;
    }

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

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions != null ? improvementSuggestions : new ArrayList<>();
    }

    public List<String> getDetectedSkills() {
        return detectedSkills;
    }

    public void setDetectedSkills(List<String> detectedSkills) {
        this.detectedSkills = detectedSkills != null ? detectedSkills : new ArrayList<>();
    }

    public List<String> getMissingConcepts() {
        return missingConcepts;
    }

    public void setMissingConcepts(List<String> missingConcepts) {
        this.missingConcepts = missingConcepts != null ? missingConcepts : new ArrayList<>();
    }

    public List<String> getRubricNotes() {
        return rubricNotes;
    }

    public void setRubricNotes(List<String> rubricNotes) {
        this.rubricNotes = rubricNotes != null ? rubricNotes : new ArrayList<>();
    }

    public Boolean getSkipped() {
        return skipped;
    }

    public void setSkipped(Boolean skipped) {
        this.skipped = skipped;
    }

    public Boolean getHintUsed() {
        return hintUsed;
    }

    public void setHintUsed(Boolean hintUsed) {
        this.hintUsed = hintUsed;
    }

    public String getNextStepSuggestion() {
        return nextStepSuggestion;
    }

    public void setNextStepSuggestion(String nextStepSuggestion) {
        this.nextStepSuggestion = nextStepSuggestion;
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

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}