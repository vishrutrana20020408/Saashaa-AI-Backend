package backend.ai_interview.service.interview;

import backend.ai_interview.dto.response.InterviewFeedbackResponse;
import backend.ai_interview.dto.response.InterviewQuestionResponse;
import backend.ai_interview.dto.response.InterviewScoreResponse;
import backend.ai_interview.entity.InterviewSession;
import backend.ai_interview.entity.InterviewTurn;
import backend.ai_interview.exception.InterviewSessionException;
import backend.ai_interview.repository.InterviewSessionRepository;
import backend.ai_interview.repository.InterviewTurnRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * InterviewRealtimeService
 *
 * Service responsible for real-time WebSocket/STOMP publishing
 * for interview sessions.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - publish session lifecycle updates
 * - publish current/next question updates
 * - publish transcript/answer updates
 * - publish live feedback updates
 * - publish live score updates
 * - publish completion/error events
 *
 * -------------------------------------------------------------------------
 * FRONTEND SUBSCRIPTION CHANNELS
 * -------------------------------------------------------------------------
 * /topic/interview/{sessionId}
 * /topic/interview/{sessionId}/status
 * /topic/interview/{sessionId}/question
 * /topic/interview/{sessionId}/transcript
 * /topic/interview/{sessionId}/feedback
 * /topic/interview/{sessionId}/score
 * /topic/interview/{sessionId}/event
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * 1. This service only broadcasts data.
 *    It does not generate interview logic by itself.
 *
 * 2. It is meant to be called from:
 *    - InterviewSessionService
 *    - InterviewEvaluationService
 *    - future WebSocket controllers / AI streaming adapters
 *
 * 3. If WebSocket is not used in some environments, this service can still
 *    safely exist and simply be called conditionally by higher layers.
 */
@Service
@SuppressWarnings("all")
@Transactional(readOnly = true)
public class InterviewRealtimeService {

    private static final String TOPIC_PREFIX = "/topic/interview/";

    private final SimpMessagingTemplate messagingTemplate;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewTurnRepository interviewTurnRepository;

    public InterviewRealtimeService(
            SimpMessagingTemplate messagingTemplate,
            InterviewSessionRepository interviewSessionRepository,
            InterviewTurnRepository interviewTurnRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewTurnRepository = interviewTurnRepository;
    }

    /**
     * Publish a full session-state event.
     */
    public void publishSessionState(Long sessionId, Object payload) {
        validateSessionExists(sessionId);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish a session status event.
     */
    public void publishSessionStatus(Long sessionId, String status, String message) {
        InterviewSession session = getRequiredSession(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "SESSION_STATUS");
        payload.put("sessionId", sessionId);
        payload.put("status", status);
        payload.put("message", message);
        payload.put("currentQuestionIndex", session.getCurrentQuestionIndex());
        payload.put("totalQuestions", session.getTotalQuestions());
        payload.put("timestamp", LocalDateTime.now());

        send(sessionStatusTopic(sessionId), payload);
        send(sessionEventTopic(sessionId), payload);
    }

    /**
     * Publish a question update.
     */
    public void publishQuestion(Long sessionId, InterviewQuestionResponse questionResponse) {
        validateSessionExists(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "QUESTION");
        payload.put("sessionId", sessionId);
        payload.put("question", questionResponse);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionQuestionTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish transcript/answer update for a turn.
     */
    public void publishTranscriptUpdate(
            Long sessionId,
            Long turnId,
            String question,
            String answer,
            String transcript,
            Boolean speechBased,
            Integer durationSeconds
    ) {
        validateSessionExists(sessionId);
        validateTurnBelongsToSession(sessionId, turnId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "TRANSCRIPT");
        payload.put("sessionId", sessionId);
        payload.put("turnId", turnId);
        payload.put("question", question);
        payload.put("answer", answer);
        payload.put("transcript", transcript);
        payload.put("speechBased", speechBased);
        payload.put("durationSeconds", durationSeconds);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionTranscriptTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish lightweight partial transcript chunk.
     * Useful for future streaming speech-to-text updates.
     */
    public void publishTranscriptChunk(
            Long sessionId,
            Long turnId,
            String chunk,
            Boolean isFinal
    ) {
        validateSessionExists(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "TRANSCRIPT_CHUNK");
        payload.put("sessionId", sessionId);
        payload.put("turnId", turnId);
        payload.put("chunk", chunk);
        payload.put("final", isFinal);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionTranscriptTopic(sessionId), payload);
    }

    /**
     * Publish feedback update.
     */
    public void publishFeedback(Long sessionId, InterviewFeedbackResponse feedbackResponse) {
        validateSessionExists(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "FEEDBACK");
        payload.put("sessionId", sessionId);
        payload.put("feedback", feedbackResponse);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionFeedbackTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish score update.
     */
    public void publishScore(Long sessionId, InterviewScoreResponse scoreResponse) {
        validateSessionExists(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "SCORE");
        payload.put("sessionId", sessionId);
        payload.put("score", scoreResponse);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionScoreTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish interview completion event.
     */
    public void publishCompletion(Long sessionId, String message, Integer overallScore) {
        InterviewSession session = getRequiredSession(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "COMPLETED");
        payload.put("sessionId", sessionId);
        payload.put("status", session.getStatus() != null ? session.getStatus().name() : "COMPLETED");
        payload.put("overallScore", overallScore);
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionEventTopic(sessionId), payload);
        send(sessionStatusTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish generic interview event.
     */
    public void publishEvent(Long sessionId, String eventType, String message) {
        validateSessionExists(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", eventType);
        payload.put("sessionId", sessionId);
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionEventTopic(sessionId), payload);
    }

    /**
     * Publish error event.
     */
    public void publishError(Long sessionId, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "ERROR");
        payload.put("sessionId", sessionId);
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionEventTopic(sessionId), payload);
    }

    /**
     * Publish typing/thinking indicator for future AI conversational UX.
     */
    public void publishAiThinkingState(Long sessionId, Boolean thinking, String message) {
        validateSessionExists(sessionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "AI_THINKING");
        payload.put("sessionId", sessionId);
        payload.put("thinking", thinking);
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionEventTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    /**
     * Publish turn-level status event.
     */
    public void publishTurnStatus(
            Long sessionId,
            Long turnId,
            String turnStatus,
            String message
    ) {
        validateSessionExists(sessionId);
        validateTurnBelongsToSession(sessionId, turnId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "TURN_STATUS");
        payload.put("sessionId", sessionId);
        payload.put("turnId", turnId);
        payload.put("turnStatus", turnStatus);
        payload.put("message", message);
        payload.put("timestamp", LocalDateTime.now());

        send(sessionEventTopic(sessionId), payload);
        send(sessionBaseTopic(sessionId), payload);
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private void send(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }

    private void validateSessionExists(Long sessionId) {
        if (sessionId == null || !interviewSessionRepository.existsById(sessionId)) {
            throw InterviewSessionException.notFound(sessionId);
        }
    }

    private InterviewSession getRequiredSession(Long sessionId) {
        return interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> InterviewSessionException.notFound(sessionId));
    }

    private void validateTurnBelongsToSession(Long sessionId, Long turnId) {
        if (turnId == null) {
            throw new InterviewSessionException(
                    "Interview turn id is required",
                    sessionId,
                    "REALTIME_VALIDATION"
            );
        }

        InterviewTurn turn = interviewTurnRepository.findById(turnId)
                .orElseThrow(() -> new InterviewSessionException(
                        "Interview turn not found for id: " + turnId,
                        sessionId,
                        "REALTIME_VALIDATION"
                ));

        if (turn.getInterviewSession() == null
                || !sessionId.equals(turn.getInterviewSession().getId())) {
            throw new InterviewSessionException(
                    "Interview turn does not belong to session id: " + sessionId,
                    sessionId,
                    "REALTIME_VALIDATION"
            );
        }
    }

    private String sessionBaseTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId;
    }

    private String sessionStatusTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId + "/status";
    }

    private String sessionQuestionTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId + "/question";
    }

    private String sessionTranscriptTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId + "/transcript";
    }

    private String sessionFeedbackTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId + "/feedback";
    }

    private String sessionScoreTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId + "/score";
    }

    private String sessionEventTopic(Long sessionId) {
        return TOPIC_PREFIX + sessionId + "/event";
    }
}