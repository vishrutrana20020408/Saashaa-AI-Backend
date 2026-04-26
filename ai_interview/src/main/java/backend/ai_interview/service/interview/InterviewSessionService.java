package backend.ai_interview.service.interview;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.ai_interview.dto.request.InterviewAnswerRequest;
import backend.ai_interview.dto.request.InterviewHintRequest;
import backend.ai_interview.dto.request.InterviewStartRequest;
import backend.ai_interview.dto.response.InterviewFeedbackResponse;
import backend.ai_interview.dto.response.InterviewQuestionResponse;
import backend.ai_interview.dto.response.InterviewSessionResponse;
import backend.ai_interview.entity.InterviewEvaluation;
import backend.ai_interview.entity.InterviewSession;
import backend.ai_interview.entity.InterviewTurn;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.entity.enums.InterviewMode;
import backend.ai_interview.entity.enums.InterviewStatus;
import backend.ai_interview.entity.enums.InterviewType;
import backend.ai_interview.exception.AiIntegrationException;
import backend.ai_interview.exception.InterviewSessionException;
import backend.ai_interview.repository.InterviewEvaluationRepository;
import backend.ai_interview.repository.InterviewSessionRepository;
import backend.ai_interview.repository.InterviewTurnRepository;
import backend.ai_interview.repository.ResumeVersionRepository;
import backend.ai_interview.service.integration.ai.InterviewClient;
import backend.ai_interview.service.integration.ai.InterviewClient.InterviewEvaluationResult;
import backend.ai_interview.service.integration.ai.InterviewClient.InterviewHintResult;
import backend.ai_interview.service.integration.ai.InterviewClient.InterviewQuestionResult;
import lombok.extern.slf4j.Slf4j;

/**
 * InterviewSessionService
 *
 * Core service for interview session lifecycle management.
 *
 * -------------------------------------------------------------------------
 * CURRENT RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - start session
 * - fetch session details
 * - submit answers
 * - provide hints/mock help
 * - generate next question
 * - finish session
 *
 * -------------------------------------------------------------------------
 * IMPORTANT NOTE
 * -------------------------------------------------------------------------
 * Integrated with AI-Engine for question generation and feedback.
 */
@Slf4j
@SuppressWarnings("all")
@Service
@Transactional
public class InterviewSessionService {

    private static final long SESSION_TIME_LIMIT_HOURS = 4;
    private static final long BREAK_TIME_MINUTES = 15;
    private static final double LOOP_SIMILARITY_THRESHOLD = 0.85;

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewTurnRepository interviewTurnRepository;
    private final InterviewEvaluationRepository interviewEvaluationRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final InterviewClient interviewClient;

    public InterviewSessionService(
            InterviewSessionRepository interviewSessionRepository,
            InterviewTurnRepository interviewTurnRepository,
            InterviewEvaluationRepository interviewEvaluationRepository,
            ResumeVersionRepository resumeVersionRepository,
            InterviewClient interviewClient
    ) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewTurnRepository = interviewTurnRepository;
        this.interviewEvaluationRepository = interviewEvaluationRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.interviewClient = interviewClient;
    }

    /**
     * Start a new interview session and generate the first question.
     */
    public InterviewSessionResponse startSession(InterviewStartRequest request, Long userId, Long adminId) {
        // Enforce 15-minute break between rounds
        checkBreakTime(userId, adminId);

        InterviewType interviewType = parseInterviewType(request.getInterviewType());
        InterviewMode interviewMode = parseInterviewMode(request.getInterviewMode());

        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setAdminId(adminId);
        session.setInterviewType(interviewType);
        session.setInterviewMode(interviewMode);
        session.setInterviewToken(UUID.randomUUID().toString());
        session.setRole(trimToNull(request.getRole()));
        session.setDomain(trimToNull(request.getDomain()));
        session.setDifficulty(defaultIfNull(request.getDifficulty(), 3));
        session.setTotalQuestions(defaultIfNull(request.getQuestionCount(), 10));
        session.setDurationMinutes(request.getDurationMinutes());
        session.setResumeId(request.getResumeId());
        session.setResumeVersionId(request.getResumeVersionId());
        session.setJobDescription(trimToNull(request.getJobDescription()));
        session.setPreferredLanguage(trimToNull(request.getPreferredLanguage()));
        session.setAllowHints(defaultIfNull(request.getAllowHints(), Boolean.TRUE));
        session.setIncludeBehavioral(defaultIfNull(request.getIncludeBehavioral(), Boolean.TRUE));
        session.setIncludeTechnical(defaultIfNull(request.getIncludeTechnical(), Boolean.TRUE));
        session.setResumeBased(request.getResumeId() != null || request.getResumeVersionId() != null);
        session.setGithubBased(request.getGithubUrls() != null && !request.getGithubUrls().isEmpty());
        session.setJobDescriptionBased(request.getJobDescription() != null && !request.getJobDescription().isBlank());
        session.setGithubUrlList(request.getGithubUrls());

        session.markStarted();
        session = interviewSessionRepository.save(session);

        InterviewTurn firstTurn = null;
        try {
            firstTurn = createQuestionTurn(session, 1);
            session.setCurrentQuestionIndex(1);
            session.touchActivity();
            session = interviewSessionRepository.save(session);
        } catch (AiIntegrationException ex) {
            log.warn("AI engine unavailable while generating the first question for session {}: {}", session.getId(), ex.getMessage());
            session.setCurrentQuestionIndex(0);
            session.touchActivity();
            session = interviewSessionRepository.save(session);
        }

        return mapSessionResponse(session, firstTurn, true);
    }

    private void checkBreakTime(Long userId, Long adminId) {
        Optional<InterviewSession> lastSessionOpt = (userId != null) 
                ? interviewSessionRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                : interviewSessionRepository.findTopByAdminIdOrderByCreatedAtDesc(adminId);

        if (lastSessionOpt.isPresent()) {
            InterviewSession lastSession = lastSessionOpt.get();
            LocalDateTime lastActivity = lastSession.getLastActivityAt() != null 
                    ? lastSession.getLastActivityAt() 
                    : lastSession.getCreatedAt();
            
            if (lastActivity != null) {
                long minutesSinceLastSession = Duration.between(lastActivity, LocalDateTime.now()).toMinutes();
                if (minutesSinceLastSession < BREAK_TIME_MINUTES) {
                    throw new InterviewSessionException(
                            "Please take a break! A minimum of " + BREAK_TIME_MINUTES + " minutes is required between interview rounds. " +
                            "Remaining: " + (BREAK_TIME_MINUTES - minutesSinceLastSession) + " minutes.",
                            lastSession.getId(),
                            "SESSION_BREAK"
                    );
                }
            }
        }
    }

    /**
     * Get all active interview sessions for a specific user.
     */
    @Transactional(readOnly = true)
    public List<InterviewSessionResponse> getSessionsByUser(Long userId) {
        return interviewSessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(session -> mapSessionResponse(session, null, false))
                .toList();
    }

    /**
     * Get all active interview sessions for a specific admin.
     */
    @Transactional(readOnly = true)
    public List<InterviewSessionResponse> getSessionsByAdmin(Long adminId) {
        return interviewSessionRepository.findByAdminIdOrderByCreatedAtDesc(adminId).stream()
                .map(session -> mapSessionResponse(session, null, false))
                .toList();
    }

    /**
     * Fetch session by id with current turn context.
     */
    @Transactional(readOnly = true)
    public InterviewSessionResponse getSessionById(Long sessionId) {
        InterviewSession session = getRequiredSession(sessionId);
        InterviewTurn currentTurn = interviewTurnRepository
                .findTopByInterviewSessionIdOrderByQuestionIndexDesc(sessionId)
                .orElse(null);

        return mapSessionResponse(session, currentTurn, true);
    }

    /**
     * Submit answer for the latest/current turn.
     */
    public InterviewFeedbackResponse submitAnswer(Long sessionId, InterviewAnswerRequest request) {
        InterviewSession session = getRequiredActiveSession(sessionId);
        InterviewTurn turn = getCurrentTurnForAnswer(session);

        if (Boolean.TRUE.equals(turn.getEvaluated())) {
            throw new InterviewSessionException(
                    "Current turn is already evaluated for session id: " + sessionId,
                    sessionId,
                    "SUBMIT_ANSWER"
            );
        }

        if (Boolean.TRUE.equals(request.getSkipped())) {
            turn.markSkipped();
        } else {
            turn.markAnswered(
                    trimToNull(request.getAnswer()),
                    trimToNull(request.getTranscript()),
                    request.getDurationSeconds()
            );
            turn.setSpeechBased(defaultIfNull(request.getSpeechBased(), Boolean.FALSE));
            turn.setAnswerLanguage(trimToNull(request.getLanguage()));
            turn.setClientTimestamp(trimToNull(request.getClientTimestamp()));
        }

        if (Boolean.TRUE.equals(request.getRequestedHint())) {
            turn.setHintUsed(Boolean.TRUE);
        }

        InterviewFeedbackResponse feedback = buildFeedbackForAnswer(session, turn, request);

        turn.markEvaluated(feedback.getOverallScore(), feedback.getSummary());
        turn.setFollowUpQuestion(feedback.getFollowUpQuestion());
        interviewTurnRepository.save(turn);

        persistTurnEvaluation(session, turn, feedback, "TURN", "QUICK", Boolean.FALSE);

        session.touchActivity();
        interviewSessionRepository.save(session);

        return feedback;
    }

    /**
     * Generate hint/help for latest or specific turn.
     */
    public InterviewFeedbackResponse requestHint(Long sessionId, InterviewHintRequest request) {
        InterviewSession session = getRequiredActiveSession(sessionId);
        InterviewTurn turn = resolveTurnForHint(session, request.getTurnId());

        String partialAnswer = trimToNull(request.getPartialAnswer());
        
        InterviewHintResult result = interviewClient.generateHint(
                session.getId(),
                turn.getId(),
                turn.getQuestion(),
                partialAnswer,
                session.getInterviewMode().name(),
                request.getHintType(),
                Boolean.TRUE
        );

        turn.markHintUsed(result.getHint());
        interviewTurnRepository.save(turn);

        session.touchActivity();
        interviewSessionRepository.save(session);

        InterviewFeedbackResponse response = new InterviewFeedbackResponse();
        response.setSessionId(session.getId());
        response.setTurnId(turn.getId());
        response.setResponseType("HINT");
        response.setFeedback(result.getHint());
        response.setSummary(result.getSummary());
        response.setGeneratedAt(LocalDateTime.now());
        return response;
    }

    /**
     * Generate the next question in the session.
     */
    public InterviewQuestionResponse generateNextQuestion(Long sessionId) {
        InterviewSession session = getRequiredActiveSession(sessionId);

        int currentIndex = defaultIfNull(session.getCurrentQuestionIndex(), 0);
        int total = defaultIfNull(session.getTotalQuestions(), 10);

        if (currentIndex >= total) {
            throw new InterviewSessionException(
                    "All interview questions have already been generated for session id: " + sessionId,
                    sessionId,
                    "NEXT_QUESTION"
            );
        }

        int nextIndex = currentIndex + 1;
        InterviewTurn nextTurn = createQuestionTurn(session, nextIndex);

        session.setCurrentQuestionIndex(nextIndex);
        session.touchActivity();
        interviewSessionRepository.save(session);

        return mapQuestionResponse(session, nextTurn, nextIndex == total);
    }

    /**
     * Finish the interview session and compute a simple final summary.
     */
    public InterviewSessionResponse finishSession(Long sessionId) {
        InterviewSession session = getRequiredSession(sessionId);

        if (InterviewStatus.COMPLETED.equals(session.getStatus())) {
            return mapSessionResponse(
                    session,
                    interviewTurnRepository.findTopByInterviewSessionIdOrderByQuestionIndexDesc(sessionId).orElse(null),
                    true
            );
        }

        if (InterviewStatus.CANCELLED.equals(session.getStatus())) {
            throw InterviewSessionException.alreadyCancelled(sessionId);
        }

        List<InterviewTurn> turns = interviewTurnRepository.findByInterviewSessionIdOrderByQuestionIndexAsc(sessionId);

        int totalScore = 0;
        int scoredTurns = 0;
        int skipped = 0;
        int hintsUsed = 0;

        for (InterviewTurn turn : turns) {
            if (Boolean.TRUE.equals(turn.getSkipped())) {
                skipped++;
            }
            if (Boolean.TRUE.equals(turn.getHintUsed())) {
                hintsUsed++;
            }
            if (turn.getScore() != null) {
                totalScore += turn.getScore();
                scoredTurns++;
            }
        }

        Integer overallScore = scoredTurns > 0 ? Math.round((float) totalScore / scoredTurns) : 0;
        String feedbackSummary = buildFinalSummary(turns, overallScore, skipped, hintsUsed);

        session.markCompleted(overallScore, feedbackSummary);
        session = interviewSessionRepository.save(session);

        persistFinalEvaluation(session, overallScore, feedbackSummary, turns, skipped, hintsUsed);

        InterviewTurn currentTurn = turns.isEmpty() ? null : turns.get(turns.size() - 1);
        return mapSessionResponse(session, currentTurn, true);
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private InterviewSession getRequiredSession(Long sessionId) {
        return interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> InterviewSessionException.notFound(sessionId));
    }

    private InterviewSession getRequiredActiveSession(Long sessionId) {
        InterviewSession session = getRequiredSession(sessionId);

        if (InterviewStatus.COMPLETED.equals(session.getStatus())) {
            throw InterviewSessionException.alreadyCompleted(sessionId);
        }
        if (InterviewStatus.CANCELLED.equals(session.getStatus())) {
            throw InterviewSessionException.alreadyCancelled(sessionId);
        }
        if (InterviewStatus.EXPIRED.equals(session.getStatus())) {
            throw InterviewSessionException.expired(sessionId);
        }

        // Enforce session time limit (3-4 hours)
        LocalDateTime start = session.getStartedAt() != null ? session.getStartedAt() : session.getCreatedAt();
        if (start != null) {
            long hoursElapsed = Duration.between(start, LocalDateTime.now()).toHours();
            if (hoursElapsed >= SESSION_TIME_LIMIT_HOURS) {
                session.setStatus(InterviewStatus.EXPIRED);
                interviewSessionRepository.save(session);
                throw new InterviewSessionException(
                        "Session has exceeded the maximum time limit of " + SESSION_TIME_LIMIT_HOURS + " hours.",
                        sessionId,
                        "SESSION_EXPIRED"
                );
            }
        }

        if (!InterviewStatus.ACTIVE.equals(session.getStatus())) {
            throw InterviewSessionException.invalidState(
                    sessionId,
                    "SESSION_ACCESS",
                    String.valueOf(session.getStatus()),
                    InterviewStatus.ACTIVE.name()
            );
        }
        return session;
    }

    private InterviewTurn getCurrentTurnForAnswer(InterviewSession session) {
        return interviewTurnRepository
                .findTopByInterviewSessionIdOrderByQuestionIndexDesc(session.getId())
                .orElseThrow(() -> new InterviewSessionException(
                        "No interview turn available for session id: " + session.getId(),
                        session.getId(),
                        "SUBMIT_ANSWER"
                ));
    }

    private InterviewTurn resolveTurnForHint(InterviewSession session, Long turnId) {
        if (turnId != null) {
            return interviewTurnRepository.findById(turnId)
                    .filter(turn -> turn.getInterviewSession() != null
                            && session.getId().equals(turn.getInterviewSession().getId()))
                    .orElseThrow(() -> new InterviewSessionException(
                            "Interview turn not found for turn id: " + turnId,
                            session.getId(),
                            "HINT"
                    ));
        }

        return getCurrentTurnForAnswer(session);
    }

    private InterviewTurn createQuestionTurn(InterviewSession session, int questionIndex) {
        InterviewTurn turn = new InterviewTurn();
        turn.setInterviewSession(session);
        turn.setQuestionIndex(questionIndex);
        
        String resumeText = null;
        if (session.getResumeVersionId() != null) {
            resumeText = resumeVersionRepository.findById(session.getResumeVersionId())
                    .map(ResumeVersion::getRawText)
                    .orElse(null);
        }

        List<InterviewTurn> previousTurns = interviewTurnRepository.findByInterviewSessionIdOrderByQuestionIndexAsc(session.getId());
        List<String> previousQuestions = previousTurns.stream().map(InterviewTurn::getQuestion).toList();
        List<Map<String, Object>> history = new ArrayList<>();
        for (InterviewTurn pt : previousTurns) {
            Map<String, Object> turnMap = new HashMap<>();
            turnMap.put("question", pt.getQuestion());
            turnMap.put("answer", pt.getAnswer());
            history.add(turnMap);
        }

        InterviewQuestionResult result = interviewClient.generateNextQuestion(
                session.getId(),
                questionIndex,
                session.getInterviewType().name(),
                session.getRole(),
                session.getDomain(),
                resumeText,
                session.getJobDescription(),
                previousQuestions,
                history,
                session.getPreferredLanguage(),
                session.getDifficulty()
        );

        if (result.getQuestion() == null || result.getQuestion().isBlank()) {
            log.error("AI engine returned an empty interview question for session {}. Raw response={}", session.getId(), result.getRawResponse());
            throw AiIntegrationException.invalidResponse("NEXT_QUESTION");
        }

        // Loop Detection: Check if new question is too similar to any previous question
        // If a loop is detected, we try to generate the question again once before resorting to a session restart.
        if (isLoopDetected(result.getQuestion(), previousQuestions)) {
            log.warn("Interview loop detected for session {}. Retrying generation.", session.getId());
            result = interviewClient.generateNextQuestion(
                    session.getId(),
                    questionIndex,
                    session.getInterviewType().name(),
                    session.getRole(),
                    session.getDomain(),
                    resumeText,
                    session.getJobDescription(),
                    previousQuestions,
                    history,
                    session.getPreferredLanguage(),
                    session.getDifficulty()
            );

            if (isLoopDetected(result.getQuestion(), previousQuestions)) {
                log.warn("Interview loop still detected for session {} after retry. Restarting session.", session.getId());
                restartSession(session);
                // After restart, we recursively try to create the first question again
                return createQuestionTurn(session, 1);
            }
        }

        turn.setQuestion(result.getQuestion());
        turn.setQuestionType(result.getQuestionType());
        turn.setCategory(result.getCategory());
        turn.setDifficulty(result.getDifficulty());
        turn.setSourceSummary(buildQuestionSourceSummary(session));

        turn.setResumeBased(defaultIfNull(session.getResumeBased(), Boolean.FALSE));
        turn.setGithubBased(defaultIfNull(session.getGithubBased(), Boolean.FALSE));
        turn.setJobDescriptionBased(defaultIfNull(session.getJobDescriptionBased(), Boolean.FALSE));

        return interviewTurnRepository.save(turn);
    }

    private boolean isLoopDetected(String newQuestion, List<String> previousQuestions) {
        if (newQuestion == null || previousQuestions == null || previousQuestions.isEmpty()) {
            return false;
        }
        for (String oldQ : previousQuestions) {
            if (calculateSimilarity(newQuestion, oldQ) > LOOP_SIMILARITY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private double calculateSimilarity(String s1, String s2) {
        String normalized1 = s1.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        String normalized2 = s2.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        
        String[] words1 = normalized1.split("\\s+");
        String[] words2 = normalized2.split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    private void restartSession(InterviewSession session) {
        // Clear previous turns and reset index
        interviewTurnRepository.deleteByInterviewSessionId(session.getId());
        session.setCurrentQuestionIndex(0);
        session.touchActivity();
        interviewSessionRepository.save(session);
    }

    private String buildQuestionSourceSummary(InterviewSession session) {
        List<String> parts = new ArrayList<>();

        if (Boolean.TRUE.equals(session.getResumeBased())) {
            parts.add("resume context");
        }
        if (Boolean.TRUE.equals(session.getGithubBased())) {
            parts.add("GitHub/project context");
        }
        if (Boolean.TRUE.equals(session.getJobDescriptionBased())) {
            parts.add("job description context");
        }
        if (parts.isEmpty()) {
            parts.add("interview configuration");
        }

        return "Generated from " + String.join(", ", parts);
    }

    @SuppressWarnings("unchecked")
	private InterviewFeedbackResponse buildFeedbackForAnswer(
            InterviewSession session,
            InterviewTurn turn,
            InterviewAnswerRequest request
    ) {
        String answer = firstNonBlank(request.getAnswer(), request.getTranscript(), turn.getAnswer(), turn.getTranscript());
        
        InterviewEvaluationResult result = interviewClient.evaluateAnswer(
                session.getId(),
                turn.getId(),
                turn.getQuestion(),
                answer,
                request.getTranscript(),
                session.getInterviewMode().name(),
                turn.getQuestionType(),
                session.getDifficulty()
        );

        InterviewFeedbackResponse response = new InterviewFeedbackResponse();
        response.setSessionId(session.getId());
        response.setTurnId(turn.getId());
        response.setResponseType("ANSWER_FEEDBACK");
        response.setEvaluated(Boolean.TRUE);
        response.setHintUsed(defaultIfNull(turn.getHintUsed(), Boolean.FALSE));
        response.setSkipped(defaultIfNull(turn.getSkipped(), Boolean.FALSE));
        response.setGeneratedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(turn.getSkipped())) {
            response.setSummary("Question skipped");
            response.setFeedback("You skipped this question. In a real interview, try to communicate your partial thinking instead of remaining silent.");
            response.setOverallScore(20);
            return response;
        }

        response.setOverallScore(result.getOverallScore());
        response.setSummary(result.getSummary());
        response.setFeedback(result.getFeedback());
        response.setStrengths(result.getStrengths());
        response.setWeaknesses(result.getWeaknesses());
        response.setImprovementSuggestions(result.getSuggestions());
        response.setFollowUpQuestion(result.getFollowUpQuestion());

        // Fill other scores if available in raw response
        Map<String, Object> raw = result.getRawResponse();
        if (raw != null && raw.containsKey("dimension_scores")) {
            Map<String, Object> dims = (Map<String, Object>) raw.get("dimension_scores");
            response.setConfidenceScore(toInteger(dims.get("confidence")));
            response.setKnowledgeScore(toInteger(dims.get("correctness")));
            response.setCommunicationScore(toInteger(dims.get("communication")));
            response.setClarityScore(toInteger(dims.get("clarity")));
            response.setRelevanceScore(toInteger(dims.get("relevance")));
        }

        response.setReadyForNextQuestion(Boolean.TRUE);
        return response;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            return parseIntegerString(s);
        }
        return 0;
    }

    private Integer parseIntegerString(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void persistTurnEvaluation(
            InterviewSession session,
            InterviewTurn turn,
            InterviewFeedbackResponse feedback,
            String evaluationType,
            String evaluationMode,
            Boolean forcedReevaluation
    ) {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setInterviewSession(session);
        evaluation.setInterviewTurn(turn);
        evaluation.setEvaluationType(evaluationType);
        evaluation.setEvaluationMode(evaluationMode);
        evaluation.setForcedReevaluation(defaultIfNull(forcedReevaluation, Boolean.FALSE));

        evaluation.setOverallScore(feedback.getOverallScore());
        evaluation.setConfidenceScore(feedback.getConfidenceScore());
        evaluation.setKnowledgeScore(feedback.getKnowledgeScore());
        evaluation.setCommunicationScore(feedback.getCommunicationScore());
        evaluation.setClarityScore(feedback.getClarityScore());
        evaluation.setRelevanceScore(feedback.getRelevanceScore());
        evaluation.setEmotionalComposureScore(feedback.getEmotionalComposureScore());
        evaluation.setTechnicalDepthScore(feedback.getTechnicalDepthScore());
        evaluation.setProblemSolvingScore(feedback.getProblemSolvingScore());

        evaluation.setSummary(feedback.getSummary());
        evaluation.setFeedback(feedback.getFeedback());
        evaluation.setExplanation(feedback.getExplanation());
        evaluation.setStrengths(joinLines(feedback.getStrengths()));
        evaluation.setWeaknesses(joinLines(feedback.getWeaknesses()));
        evaluation.setImprovementSuggestions(joinLines(feedback.getImprovementSuggestions()));
        evaluation.setDetectedSkills(joinLines(feedback.getDetectedSkills()));
        evaluation.setMissingConcepts(joinLines(feedback.getMissingConcepts()));
        evaluation.setRubricNotes(joinLines(feedback.getRubricNotes()));
        evaluation.setNextStepSuggestion(feedback.getNextStepSuggestion());
        evaluation.setFollowUpQuestion(feedback.getFollowUpQuestion());
        evaluation.setReadyForNextQuestion(feedback.getReadyForNextQuestion());

        interviewEvaluationRepository.save(evaluation);
    }

    private void persistFinalEvaluation(
            InterviewSession session,
            Integer overallScore,
            String feedbackSummary,
            List<InterviewTurn> turns,
            int skipped,
            int hintsUsed
    ) {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setInterviewSession(session);
        evaluation.setInterviewTurn(null);
        evaluation.setEvaluationType("FINAL");
        evaluation.setEvaluationMode("FINAL");
        evaluation.setForcedReevaluation(Boolean.FALSE);
        evaluation.setOverallScore(overallScore);
        evaluation.setSummary("Final interview evaluation completed");
        evaluation.setFeedback(feedbackSummary);
        evaluation.setStrengths(joinLines(buildFinalStrengths(turns, overallScore)));
        evaluation.setWeaknesses(joinLines(buildFinalWeaknesses(turns, skipped, hintsUsed)));
        evaluation.setImprovementSuggestions(joinLines(buildFinalSuggestions(turns, overallScore, skipped)));
        evaluation.setReadyForNextQuestion(Boolean.FALSE);
        interviewEvaluationRepository.save(evaluation);
    }

    private InterviewSessionResponse mapSessionResponse(
            InterviewSession session,
            InterviewTurn currentTurn,
            boolean includeTurns
    ) {
        ensureInterviewToken(session);

        InterviewSessionResponse response = new InterviewSessionResponse();
        response.setSessionId(session.getId());
        response.setUserId(session.getUserId());
        response.setAdminId(session.getAdminId());
        response.setInterviewType(enumName(session.getInterviewType()));
        response.setInterviewMode(enumName(session.getInterviewMode()));
        response.setRole(session.getRole());
        response.setDomain(session.getDomain());
        response.setDifficulty(session.getDifficulty());
        response.setTotalQuestions(session.getTotalQuestions());
        response.setCurrentQuestionIndex(session.getCurrentQuestionIndex());
        response.setStatus(enumName(session.getStatus()));
        response.setCurrentQuestion(currentTurn != null
                ? mapQuestionResponse(
                        session,
                        currentTurn,
                        defaultIfNull(session.getCurrentQuestionIndex(), 0) >= defaultIfNull(session.getTotalQuestions(), 0)
                )
                : null);
        response.setAllowHints(session.getAllowHints());
        response.setResumeBased(session.getResumeBased());
        response.setResumeId(session.getResumeId());
        response.setResumeVersionId(session.getResumeVersionId());
        response.setJobDescription(session.getJobDescription());
        response.setGithubUrls(session.getGithubUrlList());
        response.setPreferredLanguage(session.getPreferredLanguage());
        response.setStartedAt(session.getStartedAt());
        response.setEndedAt(session.getEndedAt());
        response.setLastActivityAt(session.getLastActivityAt());
        response.setDurationSeconds(calculateSessionDurationSeconds(session));
        response.setProgressPercent(calculateProgressPercent(session));
        response.setOverallScore(session.getOverallScore());
        response.setFeedbackSummary(session.getFeedbackSummary());
        response.setInterviewToken(session.getInterviewToken());
        response.setToken(session.getInterviewToken());
        response.setMessage(buildSessionMessage(session));

        if (includeTurns) {
            response.setTurns(mapTurnResponses(session.getTurns()));
        }

        return response;
    }

    private List<InterviewSessionResponse.InterviewTurn> mapTurnResponses(List<InterviewTurn> turns) {
        List<InterviewSessionResponse.InterviewTurn> mapped = new ArrayList<>();
        if (turns == null) {
            return mapped;
        }

        for (InterviewTurn turn : turns) {
            InterviewSessionResponse.InterviewTurn item = new InterviewSessionResponse.InterviewTurn();
            item.setTurnId(turn.getId());
            item.setQuestion(turn.getQuestion());
            item.setAnswer(turn.getAnswer());
            item.setTranscript(turn.getTranscript());
            item.setScore(turn.getScore());
            item.setFeedback(turn.getFeedbackSummary());
            item.setSkipped(turn.getSkipped());
            item.setHintUsed(turn.getHintUsed());
            item.setDurationSeconds(turn.getDurationSeconds());
            item.setCreatedAt(turn.getCreatedAt());
            mapped.add(item);
        }
        return mapped;
    }

    private void ensureInterviewToken(InterviewSession session) {
        if (session.getInterviewToken() == null || session.getInterviewToken().isBlank()) {
            session.setInterviewToken(UUID.randomUUID().toString());
            interviewSessionRepository.save(session);
        }
    }

    private InterviewQuestionResponse mapQuestionResponse(
            InterviewSession session,
            InterviewTurn turn,
            boolean finalQuestion
    ) {
        InterviewQuestionResponse response = new InterviewQuestionResponse();
        response.setQuestionId(turn.getId());
        response.setSessionId(session.getId());
        response.setTurnId(turn.getId());
        response.setQuestion(turn.getQuestion());
        response.setTitle("Interview Question " + turn.getQuestionIndex());
        response.setQuestionType(turn.getQuestionType());
        response.setCategory(turn.getCategory());
        response.setDifficulty(turn.getDifficulty());
        response.setQuestionIndex(turn.getQuestionIndex());
        response.setTotalQuestions(session.getTotalQuestions());
        response.setExpectedAnswerTimeSeconds(resolveExpectedAnswerTimeSeconds(session));
        response.setHintAllowed(session.getAllowHints());
        response.setSampleAnswerAllowed(Boolean.TRUE.equals(isMockMode(session)));
        response.setResumeBased(turn.getResumeBased());
        response.setGithubBased(turn.getGithubBased());
        response.setJobDescriptionBased(turn.getJobDescriptionBased());
        response.setSourceSummary(turn.getSourceSummary());
        response.setTargetSkills(buildTargetSkills(session, turn));
        response.setFollowUpHint("Give a direct answer first, then support it with one example.");
        response.setMockGuidance(Boolean.TRUE.equals(isMockMode(session))
                ? "Use a structured answer with context, action, and outcome."
                : null);
        response.setSampleAnswerOutline(Boolean.TRUE.equals(isMockMode(session))
                ? List.of("Start with the main point", "Explain one real example", "End with outcome or learning")
                : new ArrayList<>());
        response.setTags(buildQuestionTags(session, turn));
        response.setFinalQuestion(finalQuestion);
        response.setGeneratedAt(turn.getCreatedAt());
        response.setMessage("Question generated successfully");
        return response;
    }

    private String buildSessionMessage(InterviewSession session) {
        if (InterviewStatus.COMPLETED.equals(session.getStatus())) {
            return "Interview session completed successfully";
        }
        if (InterviewStatus.ACTIVE.equals(session.getStatus())) {
            return "Interview session is active";
        }
        if (InterviewStatus.CANCELLED.equals(session.getStatus())) {
            return "Interview session was cancelled";
        }
        return "Interview session loaded successfully";
    }

    private String buildFinalSummary(
            List<InterviewTurn> turns,
            Integer overallScore,
            int skipped,
            int hintsUsed
    ) {
        return "Interview finished with an overall score of "
                + overallScore
                + "/100. Total questions: "
                + turns.size()
                + ", skipped: "
                + skipped
                + ", hints used: "
                + hintsUsed
                + ". Focus on clarity, relevance, and structured examples to improve future performance.";
    }

    private List<String> buildFinalStrengths(List<InterviewTurn> turns, Integer overallScore) {
        List<String> strengths = new ArrayList<>();
        if (overallScore != null && overallScore >= 75) {
            strengths.add("Maintained good overall interview performance");
            strengths.add("Demonstrated reasonable answer consistency");
        } else {
            strengths.add("Completed the interview flow");
        }

        if (!turns.isEmpty()) {
            strengths.add("Participated across " + turns.size() + " question(s)");
        }
        return strengths;
    }

    private List<String> buildFinalWeaknesses(List<InterviewTurn> turns, int skipped, int hintsUsed) {
        List<String> weaknesses = new ArrayList<>();
        if (skipped > 0) {
            weaknesses.add("Some questions were skipped");
        }
        if (hintsUsed > 0) {
            weaknesses.add("Relied on hints during the interview");
        }
        if (turns.isEmpty()) {
            weaknesses.add("No meaningful interview data was captured");
        }
        return weaknesses;
    }

    private List<String> buildFinalSuggestions(List<InterviewTurn> turns, Integer overallScore, int skipped) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Practice structured answers using problem -> action -> outcome");
        suggestions.add("Support technical answers with one concrete example");
        if (skipped > 0) {
            suggestions.add("Avoid skipping questions; explain partial thinking instead");
        }
        if (turns == null || turns.isEmpty()) {
            suggestions.add("Try to capture at least one complete interview turn during a session.");
        } else {
            suggestions.add("Review each completed turn and identify one improvement per answer.");
        }
        if (overallScore != null && overallScore < 70) {
            suggestions.add("Improve clarity and relevance before moving to advanced interview rounds");
        }
        return suggestions;
    }

    private Integer calculateProgressPercent(InterviewSession session) {
        int current = defaultIfNull(session.getCurrentQuestionIndex(), 0);
        int total = defaultIfNull(session.getTotalQuestions(), 0);
        if (total <= 0) {
            return 0;
        }
        return clamp(Math.round((current * 100f) / total), 0, 100);
    }

    private Long calculateSessionDurationSeconds(InterviewSession session) {
        LocalDateTime start = session.getStartedAt();
        LocalDateTime end = session.getEndedAt() != null ? session.getEndedAt() : LocalDateTime.now();

        if (start == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(start, end).getSeconds());
    }

    private Integer resolveExpectedAnswerTimeSeconds(InterviewSession session) {
        int difficulty = defaultIfNull(session.getDifficulty(), 3);
        return switch (difficulty) {
            case 1 -> 60;
            case 2 -> 75;
            case 3 -> 90;
            case 4 -> 120;
            case 5 -> 150;
            default -> 90;
        };
    }

    private List<String> buildTargetSkills(InterviewSession session, InterviewTurn turn) {
        List<String> skills = new ArrayList<>();
        if (turn.getCategory() != null) {
            skills.add(turn.getCategory());
        }
        if (session.getDomain() != null) {
            skills.add(session.getDomain());
        }
        if (session.getRole() != null) {
            skills.add(session.getRole());
        }
        return distinct(skills);
    }

    private List<String> buildQuestionTags(InterviewSession session, InterviewTurn turn) {
        List<String> tags = new ArrayList<>();
        tags.add(turn.getQuestionType());
        if (Boolean.TRUE.equals(turn.getResumeBased())) tags.add("resume");
        if (Boolean.TRUE.equals(turn.getGithubBased())) tags.add("github");
        if (Boolean.TRUE.equals(turn.getJobDescriptionBased())) tags.add("job-description");
        if (Boolean.TRUE.equals(isMockMode(session))) tags.add("mock");
        return distinct(tags);
    }

    private InterviewType parseInterviewType(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InterviewSessionException("Interview type is required");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("-", "_");

        if ("MOCK_INTERVIEW".equals(normalized)
                || "JOB_PROFILE_INTERVIEW".equals(normalized)
                || "JOB_PROFILE".equals(normalized)) {
            return InterviewType.MIXED;
        }

        if ("DOCUMENT_VERIFICATION".equals(normalized)
                || "DOCUMENT_VERIFICATION_INTERVIEW".equals(normalized)
                || normalized.startsWith("DOCUMENT")) {
            return InterviewType.HR;
        }

        try {
            return InterviewType.valueOf(normalized);
        } catch (Exception ex) {
            throw new InterviewSessionException("Invalid interview type: " + value);
        }
    }

    private InterviewMode parseInterviewMode(String value) {
        try {
            return InterviewMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new InterviewSessionException("Invalid interview mode: " + value);
        }
    }

    private Boolean isMockMode(InterviewSession session) {
        return session.getInterviewMode() == InterviewMode.MOCK;
    }

    private String enumName(Enum<?> value) {
        return value != null ? value.name() : null;
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

    @SafeVarargs
    private <T> T defaultIfNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String joinLines(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join("\n", values);
    }

    private List<String> distinct(List<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String trimmed = value.trim();
            if (!result.contains(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }
}