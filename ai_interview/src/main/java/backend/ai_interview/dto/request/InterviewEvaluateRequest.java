package backend.ai_interview.dto.request;

import jakarta.validation.constraints.Size;

/**
 * InterviewEvaluateRequest
 *
 * Request DTO used when the frontend/backend wants to explicitly trigger
 * evaluation of the latest answer or a specific interview turn.
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * Frontend → Backend:
 *   POST /api/interview/sessions/{sessionId}/evaluate
 *
 * Backend:
 *   - identifies which answer/turn to evaluate
 *   - optionally uses provided answer override
 *   - calls AI-engine for scoring + feedback
 *   - stores evaluation result
 *
 * -------------------------------------------------------------------------
 * USAGE NOTES
 * -------------------------------------------------------------------------
 * This DTO is useful in two modes:
 *
 * 1. Evaluate latest saved answer
 *    - send empty/minimal body
 *    - backend evaluates most recent turn in the session
 *
 * 2. Evaluate a specific turn or ad-hoc answer
 *    - provide turnId and/or answerOverride
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later add:
 * - emotion score hints from frontend
 * - webcam/audio analytics references
 * - rubric selection
 * - strictness level
 */
@SuppressWarnings("all")
public class InterviewEvaluateRequest {

    /**
     * Optional interview turn id.
     *
     * If provided, backend should evaluate this specific turn.
     * If omitted, backend may evaluate the latest turn in the session.
     */
    private Long turnId;

    /**
     * Optional explicit answer override.
     *
     * Useful when frontend wants evaluation without first persisting the answer,
     * or when re-evaluating edited transcript text.
     */
    @Size(max = 20000, message = "Answer override must not exceed 20000 characters")
    private String answerOverride;

    /**
     * Optional transcript override for speech-based answers.
     */
    @Size(max = 20000, message = "Transcript override must not exceed 20000 characters")
    private String transcriptOverride;

    /**
     * Optional question override.
     *
     * Useful if evaluation needs full context and backend wants to avoid
     * separately resolving the question from storage.
     */
    @Size(max = 10000, message = "Question override must not exceed 10000 characters")
    private String questionOverride;

    /**
     * Optional flag to force re-evaluation even if a score already exists.
     */
    private Boolean forceReevaluate = Boolean.FALSE;

    /**
     * Optional flag indicating whether backend should persist the evaluation result.
     */
    private Boolean saveResult = Boolean.TRUE;

    /**
     * Optional evaluation mode.
     * Example values:
     * - QUICK
     * - DETAILED
     * - FINAL
     */
    @Size(max = 50, message = "Evaluation mode must not exceed 50 characters")
    private String evaluationMode;

    /**
     * Optional strictness level from 1 to 5.
     * Can be used later by service/AI-engine.
     */
    private Integer strictnessLevel;

    /**
     * Optional client timestamp for synchronization/debugging.
     */
    @Size(max = 100, message = "Timestamp must not exceed 100 characters")
    private String clientTimestamp;

    /**
     * Optional interview token for this evaluation request.
     */
    private String token;

    /**
     * Optional session token alias.
     */
    private String sessionToken;

    public InterviewEvaluateRequest() {
    }

    public Long getTurnId() {
        return turnId;
    }

    public void setTurnId(Long turnId) {
        this.turnId = turnId;
    }

    public String getAnswerOverride() {
        return answerOverride;
    }

    public void setAnswerOverride(String answerOverride) {
        this.answerOverride = answerOverride;
    }

    public String getTranscriptOverride() {
        return transcriptOverride;
    }

    public void setTranscriptOverride(String transcriptOverride) {
        this.transcriptOverride = transcriptOverride;
    }

    public String getQuestionOverride() {
        return questionOverride;
    }

    public void setQuestionOverride(String questionOverride) {
        this.questionOverride = questionOverride;
    }

    public Boolean getForceReevaluate() {
        return forceReevaluate;
    }

    public void setForceReevaluate(Boolean forceReevaluate) {
        this.forceReevaluate = forceReevaluate;
    }

    public Boolean getSaveResult() {
        return saveResult;
    }

    public void setSaveResult(Boolean saveResult) {
        this.saveResult = saveResult;
    }

    public String getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(String evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    public Integer getStrictnessLevel() {
        return strictnessLevel;
    }

    public void setStrictnessLevel(Integer strictnessLevel) {
        this.strictnessLevel = strictnessLevel;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}