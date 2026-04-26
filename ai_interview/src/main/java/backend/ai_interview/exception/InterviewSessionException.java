package backend.ai_interview.exception;

/**
 * InterviewSessionException
 *
 * Custom exception for handling failures related to interview session lifecycle
 * and operations.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - InterviewSessionService
 * - InterviewSessionController
 * - Interview flow management (start / next question / submit answer / end)
 *
 * -------------------------------------------------------------------------
 * TYPICAL CAUSES
 * -------------------------------------------------------------------------
 * - Session not found
 * - Session already completed/cancelled
 * - Invalid state transition
 * - Unauthorized access to session
 * - Question index mismatch
 * - Session expired or inactive
 *
 * -------------------------------------------------------------------------
 * DESIGN NOTES
 * -------------------------------------------------------------------------
 * - Keeps interview flow errors separate from generic exceptions
 * - Helps in better API error responses
 * - Can be extended later with error codes
 */
@SuppressWarnings("all")
public class InterviewSessionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Optional session id
     */
    private Long sessionId;

    /**
     * Optional operation being performed
     * Example:
     * - START
     * - NEXT_QUESTION
     * - SUBMIT_ANSWER
     * - END
     */
    private String operation;

    /**
     * Optional current status
     */
    private String currentStatus;

    /**
     * Optional expected status
     */
    private String expectedStatus;

    public InterviewSessionException(String message) {
        super(message);
    }

    public InterviewSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterviewSessionException(String message, Long sessionId, String operation) {
        super(message);
        this.sessionId = sessionId;
        this.operation = operation;
    }

    public InterviewSessionException(String message,
                                     Long sessionId,
                                     String operation,
                                     String currentStatus,
                                     String expectedStatus) {
        super(message);
        this.sessionId = sessionId;
        this.operation = operation;
        this.currentStatus = currentStatus;
        this.expectedStatus = expectedStatus;
    }

    public InterviewSessionException(String message,
                                     Throwable cause,
                                     Long sessionId,
                                     String operation) {
        super(message, cause);
        this.sessionId = sessionId;
        this.operation = operation;
    }

    /**
     * Factory: session not found
     */
    public static InterviewSessionException notFound(Long sessionId) {
        return new InterviewSessionException(
                "Interview session not found for id: " + sessionId,
                sessionId,
                "LOOKUP"
        );
    }

    /**
     * Factory: invalid state transition
     */
    public static InterviewSessionException invalidState(Long sessionId,
                                                         String operation,
                                                         String currentStatus,
                                                         String expectedStatus) {
        return new InterviewSessionException(
                "Invalid session state for operation: " + operation +
                        " | current=" + currentStatus +
                        " | expected=" + expectedStatus,
                sessionId,
                operation,
                currentStatus,
                expectedStatus
        );
    }

    /**
     * Factory: already completed
     */
    public static InterviewSessionException alreadyCompleted(Long sessionId) {
        return new InterviewSessionException(
                "Interview session already completed for id: " + sessionId,
                sessionId,
                "VALIDATION"
        );
    }

    /**
     * Factory: already cancelled
     */
    public static InterviewSessionException alreadyCancelled(Long sessionId) {
        return new InterviewSessionException(
                "Interview session already cancelled for id: " + sessionId,
                sessionId,
                "VALIDATION"
        );
    }

    /**
     * Factory: session expired
     */
    public static InterviewSessionException expired(Long sessionId) {
        return new InterviewSessionException(
                "Interview session expired for id: " + sessionId,
                sessionId,
                "VALIDATION"
        );
    }

    /**
     * Factory: unauthorized access
     */
    public static InterviewSessionException unauthorized(Long sessionId) {
        return new InterviewSessionException(
                "Unauthorized access to interview session id: " + sessionId,
                sessionId,
                "AUTHORIZATION"
        );
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getOperation() {
        return operation;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getExpectedStatus() {
        return expectedStatus;
    }
}