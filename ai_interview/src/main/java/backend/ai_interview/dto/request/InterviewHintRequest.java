package backend.ai_interview.dto.request;

import jakarta.validation.constraints.Size;

/**
 * InterviewHintRequest
 *
 * Request DTO used when a user asks for help/hint during an interview session.
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * Frontend → Backend:
 *   POST /api/interview/sessions/{sessionId}/hint
 *
 * Backend:
 *   - identifies current question/turn
 *   - determines interview mode (MOCK / REAL)
 *   - calls AI-engine for:
 *        → hint
 *        → explanation
 *        → sample answer (if allowed)
 *   - returns structured help response
 *
 * -------------------------------------------------------------------------
 * BEHAVIOR RULES
 * -------------------------------------------------------------------------
 * MOCK INTERVIEW:
 *   - AI can give:
 *        ✓ full explanation
 *        ✓ structured answer
 *        ✓ improvement tips
 *
 * REAL INTERVIEW:
 *   - AI should give:
 *        ✓ light hint only
 *        ✗ no full answer
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - This request is flexible for future features
 * - Supports both current question hint and custom question hint
 * - Can be extended for difficulty-based hints
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later add:
 * - voice-based hint triggers
 * - partial hint vs full hint levels
 * - time-based hint unlocking
 * - adaptive hint difficulty
 */
@SuppressWarnings("all")
public class InterviewHintRequest {

    /**
     * Optional interview turn ID.
     *
     * If provided:
     *   → backend generates hint for this specific turn
     *
     * If NOT provided:
     *   → backend assumes latest/current question
     */
    private Long turnId;

    /**
     * Optional question override.
     *
     * Useful when frontend wants hint for:
     * - custom question
     * - practice mode
     */
    @Size(max = 10000, message = "Question must not exceed 10000 characters")
    private String questionOverride;

    /**
     * Optional partial answer provided by the user.
     *
     * AI can use this to:
     * - improve user's answer
     * - give targeted feedback
     */
    @Size(max = 20000, message = "Partial answer must not exceed 20000 characters")
    private String partialAnswer;

    /**
     * Hint type.
     *
     * Example values:
     * - BASIC        → small directional hint
     * - STRUCTURED   → outline/steps
     * - EXPLANATION  → conceptual explanation
     * - SAMPLE       → full answer (only allowed in MOCK)
     */
    @Size(max = 50, message = "Hint type must not exceed 50 characters")
    private String hintType;

    /**
     * Optional difficulty adjustment.
     *
     * Example:
     * - 1 → very easy hint
     * - 5 → very challenging hint
     */
    private Integer difficultyLevel;

    /**
     * Optional flag to explicitly allow a full sample answer.
     *
     * Backend should still validate based on interview mode.
     */
    private Boolean allowSampleAnswer = Boolean.FALSE;

    /**
     * Optional preferred language of input.
     *
     * AI understands multiple languages,
     * but response will always be in English.
     */
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    /**
     * Optional client timestamp for debugging/sync.
     */
    @Size(max = 100, message = "Timestamp must not exceed 100 characters")
    private String clientTimestamp;

    public InterviewHintRequest() {
    }

    public Long getTurnId() {
        return turnId;
    }

    public void setTurnId(Long turnId) {
        this.turnId = turnId;
    }

    public String getQuestionOverride() {
        return questionOverride;
    }

    public void setQuestionOverride(String questionOverride) {
        this.questionOverride = questionOverride;
    }

    public String getPartialAnswer() {
        return partialAnswer;
    }

    public void setPartialAnswer(String partialAnswer) {
        this.partialAnswer = partialAnswer;
    }

    public String getHintType() {
        return hintType;
    }

    public void setHintType(String hintType) {
        this.hintType = hintType;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Boolean getAllowSampleAnswer() {
        return allowSampleAnswer;
    }

    public void setAllowSampleAnswer(Boolean allowSampleAnswer) {
        this.allowSampleAnswer = allowSampleAnswer;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    /**
     * Optional interview token for this hint request.
     */
    private String token;

    /**
     * Optional session token alias.
     */
    private String sessionToken;

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