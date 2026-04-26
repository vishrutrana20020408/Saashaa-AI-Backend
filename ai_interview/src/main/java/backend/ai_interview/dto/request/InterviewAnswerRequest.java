package backend.ai_interview.dto.request;

import jakarta.validation.constraints.Size;

/**
 * InterviewAnswerRequest
 *
 * Request DTO used when a user submits an answer during an interview session.
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * Frontend → Backend:
 *   POST /api/interview/sessions/{sessionId}/answer
 *
 * Backend:
 *   - validates answer
 *   - stores answer in InterviewTurn
 *   - optionally sends to AI-engine for:
 *        → evaluation
 *        → feedback
 *        → next question generation
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Supports both text and speech-based answers
 * - Transcript field allows storing speech-to-text results
 * - Duration helps evaluate confidence/fluency
 * - Metadata can be extended later for advanced scoring
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later add:
 * - audio file reference (S3 URL)
 * - video response metadata
 * - emotion detection signals
 * - typing speed / hesitation metrics
 */
@SuppressWarnings("all")
public class InterviewAnswerRequest {

    /**
     * The main answer provided by the user (text or transcript).
     */
    @Size(max = 20000, message = "Answer must not exceed 20000 characters")
    private String answer;

    /**
     * Optional transcript if the answer came from speech input.
     * (Can be same as 'answer' or raw speech-to-text output)
     */
    @Size(max = 20000, message = "Transcript must not exceed 20000 characters")
    private String transcript;

    /**
     * Optional duration (in seconds) taken to answer the question.
     * Useful for:
     * - confidence scoring
     * - hesitation analysis
     */
    private Integer durationSeconds;

    /**
     * Optional flag indicating whether this was a speech-based answer.
     */
    private Boolean speechBased = Boolean.FALSE;

    /**
     * Optional language of input answer.
     * AI can understand multiple Indian languages but responds in English.
     */
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    /**
     * Optional flag indicating if the user skipped the question.
     */
    private Boolean skipped = Boolean.FALSE;

    /**
     * Optional flag indicating if user explicitly requested help/hint.
     */
    private Boolean requestedHint = Boolean.FALSE;

    /**
     * Optional client-side timestamp (ISO string or custom format).
     * Useful for syncing frontend/backend timelines.
     */
    @Size(max = 100, message = "Timestamp must not exceed 100 characters")
    private String clientTimestamp;

    /**
     * Optional interview token for this session.
     */
    private String token;

    /**
     * Optional session token alias.
     */
    private String sessionToken;

    public InterviewAnswerRequest() {
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Boolean getSpeechBased() {
        return speechBased;
    }

    public void setSpeechBased(Boolean speechBased) {
        this.speechBased = speechBased;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getSkipped() {
        return skipped;
    }

    public void setSkipped(Boolean skipped) {
        this.skipped = skipped;
    }

    public Boolean getRequestedHint() {
        return requestedHint;
    }

    public void setRequestedHint(Boolean requestedHint) {
        this.requestedHint = requestedHint;
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