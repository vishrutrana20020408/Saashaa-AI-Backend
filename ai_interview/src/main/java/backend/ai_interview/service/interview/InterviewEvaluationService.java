package backend.ai_interview.service.interview;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.ai_interview.dto.request.InterviewEvaluateRequest;
import backend.ai_interview.dto.response.InterviewFeedbackResponse;
import backend.ai_interview.dto.response.InterviewScoreResponse;
import backend.ai_interview.entity.InterviewEvaluation;
import backend.ai_interview.entity.InterviewSession;
import backend.ai_interview.entity.InterviewTurn;
import backend.ai_interview.entity.enums.InterviewStatus;
import backend.ai_interview.exception.InterviewSessionException;
import backend.ai_interview.repository.InterviewEvaluationRepository;
import backend.ai_interview.repository.InterviewSessionRepository;
import backend.ai_interview.repository.InterviewTurnRepository;
import backend.ai_interview.service.integration.ai.AiEngineClient;

/**
 * InterviewEvaluationService
 *
 * Handles:
 * - explicit answer evaluation
 * - re-evaluation flow
 * - final/current score aggregation for a session
 *
 * -------------------------------------------------------------------------
 * IMPORTANT NOTE
 * -------------------------------------------------------------------------
 * This implementation is backend-safe and works immediately with your current
 * interview entities/repositories.
 *
 * Later, you can replace the deterministic scoring logic with AI-engine calls.
 * The clean integration points are:
 * - buildEvaluationFeedback(...)
 * - aggregateSessionScore(...)
 */
@Service
@SuppressWarnings("all")
@Transactional
public class InterviewEvaluationService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewTurnRepository interviewTurnRepository;
    private final InterviewEvaluationRepository interviewEvaluationRepository;
    @SuppressWarnings("unused") // Reserved for future AI-powered answer evaluation integration
    private final AiEngineClient aiEngineClient;

    public InterviewEvaluationService(
            InterviewSessionRepository interviewSessionRepository,
            InterviewTurnRepository interviewTurnRepository,
            InterviewEvaluationRepository interviewEvaluationRepository,
            AiEngineClient aiEngineClient
    ) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewTurnRepository = interviewTurnRepository;
        this.interviewEvaluationRepository = interviewEvaluationRepository;
        this.aiEngineClient = aiEngineClient;
    }

    /**
     * Evaluate the latest or specified turn in a session.
     */
    public InterviewFeedbackResponse evaluateAnswer(Long sessionId, InterviewEvaluateRequest request) {
        InterviewSession session = getRequiredSession(sessionId);

        if (InterviewStatus.CANCELLED.equals(session.getStatus())) {
            throw InterviewSessionException.alreadyCancelled(sessionId);
        }
        if (InterviewStatus.EXPIRED.equals(session.getStatus())) {
            throw InterviewSessionException.expired(sessionId);
        }

        InterviewTurn turn = resolveTurnForEvaluation(session, request);

        if (turn == null) {
            throw new InterviewSessionException(
                    "No interview turn available to evaluate for session id: " + sessionId,
                    sessionId,
                    "EVALUATE"
            );
        }

        if (Boolean.TRUE.equals(turn.getEvaluated()) && !Boolean.TRUE.equals(request.getForceReevaluate())) {
            return buildFeedbackFromExistingEvaluation(session, turn)
                    .orElseGet(() -> buildAndPersistEvaluation(session, turn, request));
        }

        return buildAndPersistEvaluation(session, turn, request);
    }

    /**
     * Get aggregated score for a full interview session.
     */
    @Transactional(readOnly = true)
    public InterviewScoreResponse getScore(Long sessionId) {
        InterviewSession session = getRequiredSession(sessionId);

        List<InterviewTurn> turns = interviewTurnRepository.findByInterviewSessionIdOrderByQuestionIndexAsc(sessionId);
        List<InterviewEvaluation> evaluations =
                interviewEvaluationRepository.findByInterviewSessionIdOrderByCreatedAtAsc(sessionId);

        return aggregateSessionScore(session, turns, evaluations);
    }

    // ---------------------------------------------------------------------
    // Evaluation flow
    // ---------------------------------------------------------------------

    private InterviewFeedbackResponse buildAndPersistEvaluation(
            InterviewSession session,
            InterviewTurn turn,
            InterviewEvaluateRequest request
    ) {
        String question = firstNonBlank(request.getQuestionOverride(), turn.getQuestion());
        String answer = firstNonBlank(request.getAnswerOverride(), turn.getAnswer(), turn.getTranscript());
        String transcript = firstNonBlank(request.getTranscriptOverride(), turn.getTranscript());

        if (!hasText(question)) {
            throw new InterviewSessionException(
                    "Cannot evaluate because question content is missing for turn id: " + turn.getId(),
                    session.getId(),
                    "EVALUATE"
            );
        }

        boolean skipped = Boolean.TRUE.equals(turn.getSkipped());
        EvaluationBreakdown breakdown = calculateBreakdown(
                question,
                answer,
                transcript,
                turn,
                request.getEvaluationMode(),
                request.getStrictnessLevel(),
                skipped
        );

        InterviewFeedbackResponse response = new InterviewFeedbackResponse();
        response.setSessionId(session.getId());
        response.setTurnId(turn.getId());
        response.setQuestionId(turn.getId());
        response.setResponseType(Boolean.TRUE.equals(request.getForceReevaluate()) ? "RE_EVALUATION" : "EVALUATION");
        response.setEvaluated(Boolean.TRUE);
        response.setHintUsed(defaultIfNull(turn.getHintUsed(), Boolean.FALSE));
        response.setSkipped(skipped);
        response.setOverallScore(breakdown.overallScore);
        response.setConfidenceScore(breakdown.confidenceScore);
        response.setKnowledgeScore(breakdown.knowledgeScore);
        response.setCommunicationScore(breakdown.communicationScore);
        response.setClarityScore(breakdown.clarityScore);
        response.setRelevanceScore(breakdown.relevanceScore);
        response.setEmotionalComposureScore(breakdown.emotionalScore);
        response.setTechnicalDepthScore(breakdown.technicalDepthScore);
        response.setProblemSolvingScore(breakdown.problemSolvingScore);
        response.setSummary(buildSummaryText(breakdown.overallScore, skipped));
        response.setFeedback(buildFeedbackText(turn, breakdown));
        response.setExplanation(buildExplanationText(question, answer, breakdown));
        response.setStrengths(buildStrengths(turn, breakdown));
        response.setWeaknesses(buildWeaknesses(turn, breakdown));
        response.setImprovementSuggestions(buildImprovementSuggestions(turn, breakdown));
        response.setDetectedSkills(buildDetectedSkills(question, answer));
        response.setMissingConcepts(buildMissingConcepts(question, answer, breakdown));
        response.setRubricNotes(buildRubricNotes(request, breakdown));
        response.setNextStepSuggestion(buildNextStepSuggestion(breakdown));
        response.setFollowUpQuestion(buildFollowUpQuestion(question));
        response.setReadyForNextQuestion(Boolean.TRUE);
        response.setGeneratedAt(LocalDateTime.now());
        response.setMessage("Interview answer evaluated successfully");

        if (Boolean.TRUE.equals(request.getSaveResult())) {
            turn.setScore(breakdown.overallScore);
            turn.setFeedbackSummary(response.getSummary());
            turn.setEvaluated(Boolean.TRUE);
            turn.setEvaluatedAt(LocalDateTime.now());
            if (hasText(request.getAnswerOverride())) {
                turn.setAnswer(request.getAnswerOverride().trim());
            }
            if (hasText(request.getTranscriptOverride())) {
                turn.setTranscript(request.getTranscriptOverride().trim());
            }
            interviewTurnRepository.save(turn);

            InterviewEvaluation evaluation = new InterviewEvaluation();
            evaluation.setInterviewSession(session);
            evaluation.setInterviewTurn(turn);
            evaluation.setEvaluationType(Boolean.TRUE.equals(request.getForceReevaluate()) ? "RE_EVALUATION" : "TURN");
            evaluation.setEvaluationMode(firstNonBlank(request.getEvaluationMode(), "DETAILED"));
            evaluation.setForcedReevaluation(defaultIfNull(request.getForceReevaluate(), Boolean.FALSE));

            evaluation.setOverallScore(response.getOverallScore());
            evaluation.setConfidenceScore(response.getConfidenceScore());
            evaluation.setKnowledgeScore(response.getKnowledgeScore());
            evaluation.setCommunicationScore(response.getCommunicationScore());
            evaluation.setClarityScore(response.getClarityScore());
            evaluation.setRelevanceScore(response.getRelevanceScore());
            evaluation.setEmotionalComposureScore(response.getEmotionalComposureScore());
            evaluation.setTechnicalDepthScore(response.getTechnicalDepthScore());
            evaluation.setProblemSolvingScore(response.getProblemSolvingScore());

            evaluation.setSummary(response.getSummary());
            evaluation.setFeedback(response.getFeedback());
            evaluation.setExplanation(response.getExplanation());
            evaluation.setStrengths(joinLines(response.getStrengths()));
            evaluation.setWeaknesses(joinLines(response.getWeaknesses()));
            evaluation.setImprovementSuggestions(joinLines(response.getImprovementSuggestions()));
            evaluation.setDetectedSkills(joinLines(response.getDetectedSkills()));
            evaluation.setMissingConcepts(joinLines(response.getMissingConcepts()));
            evaluation.setRubricNotes(joinLines(response.getRubricNotes()));
            evaluation.setNextStepSuggestion(response.getNextStepSuggestion());
            evaluation.setFollowUpQuestion(response.getFollowUpQuestion());
            evaluation.setReadyForNextQuestion(response.getReadyForNextQuestion());
            evaluation.setClientTimestamp(trimToNull(request.getClientTimestamp()));

            interviewEvaluationRepository.save(evaluation);
        }

        session.setLastActivityAt(LocalDateTime.now());
        interviewSessionRepository.save(session);

        return response;
    }

    private InterviewTurn resolveTurnForEvaluation(InterviewSession session, InterviewEvaluateRequest request) {
        if (request.getTurnId() != null) {
            InterviewTurn turn = interviewTurnRepository.findById(request.getTurnId())
                    .orElseThrow(() -> new InterviewSessionException(
                            "Interview turn not found for id: " + request.getTurnId(),
                            session.getId(),
                            "EVALUATE"
                    ));

            if (turn.getInterviewSession() == null
                    || !session.getId().equals(turn.getInterviewSession().getId())) {
                throw new InterviewSessionException(
                        "Turn does not belong to session id: " + session.getId(),
                        session.getId(),
                        "EVALUATE"
                );
            }
            return turn;
        }

        return interviewTurnRepository
                .findTopByInterviewSessionIdOrderByQuestionIndexDesc(session.getId())
                .orElse(null);
    }

    private java.util.Optional<InterviewFeedbackResponse> buildFeedbackFromExistingEvaluation(
            InterviewSession session,
            InterviewTurn turn
    ) {
        return interviewEvaluationRepository
                .findTopByInterviewTurnIdOrderByCreatedAtDesc(turn.getId())
                .map(evaluation -> {
                    InterviewFeedbackResponse response = new InterviewFeedbackResponse();
                    response.setSessionId(session.getId());
                    response.setTurnId(turn.getId());
                    response.setQuestionId(turn.getId());
                    response.setResponseType("EVALUATION");
                    response.setEvaluated(Boolean.TRUE);
                    response.setHintUsed(defaultIfNull(turn.getHintUsed(), Boolean.FALSE));
                    response.setSkipped(defaultIfNull(turn.getSkipped(), Boolean.FALSE));

                    response.setOverallScore(evaluation.getOverallScore());
                    response.setConfidenceScore(evaluation.getConfidenceScore());
                    response.setKnowledgeScore(evaluation.getKnowledgeScore());
                    response.setCommunicationScore(evaluation.getCommunicationScore());
                    response.setClarityScore(evaluation.getClarityScore());
                    response.setRelevanceScore(evaluation.getRelevanceScore());
                    response.setEmotionalComposureScore(evaluation.getEmotionalComposureScore());
                    response.setTechnicalDepthScore(evaluation.getTechnicalDepthScore());
                    response.setProblemSolvingScore(evaluation.getProblemSolvingScore());

                    response.setSummary(evaluation.getSummary());
                    response.setFeedback(evaluation.getFeedback());
                    response.setExplanation(evaluation.getExplanation());
                    response.setStrengths(splitLines(evaluation.getStrengths()));
                    response.setWeaknesses(splitLines(evaluation.getWeaknesses()));
                    response.setImprovementSuggestions(splitLines(evaluation.getImprovementSuggestions()));
                    response.setDetectedSkills(splitLines(evaluation.getDetectedSkills()));
                    response.setMissingConcepts(splitLines(evaluation.getMissingConcepts()));
                    response.setRubricNotes(splitLines(evaluation.getRubricNotes()));
                    response.setNextStepSuggestion(evaluation.getNextStepSuggestion());
                    response.setFollowUpQuestion(evaluation.getFollowUpQuestion());
                    response.setReadyForNextQuestion(evaluation.getReadyForNextQuestion());
                    response.setGeneratedAt(evaluation.getEvaluatedAt());
                    response.setMessage("Existing interview evaluation loaded successfully");
                    return response;
                });
    }

    // ---------------------------------------------------------------------
    // Aggregate session score
    // ---------------------------------------------------------------------

    private InterviewScoreResponse aggregateSessionScore(
            InterviewSession session,
            List<InterviewTurn> turns,
            List<InterviewEvaluation> evaluations
    ) {
        InterviewScoreResponse response = new InterviewScoreResponse();
        response.setSessionId(session.getId());
        response.setUserId(session.getUserId());
        response.setAdminId(session.getAdminId());
        response.setInterviewType(session.getInterviewType() != null ? session.getInterviewType().name() : null);
        response.setInterviewMode(session.getInterviewMode() != null ? session.getInterviewMode().name() : null);
        response.setStatus(session.getStatus() != null ? session.getStatus().name() : null);

        int totalQuestions = turns.size();
        int answeredQuestions = 0;
        int skippedQuestions = 0;
        int hintsUsed = 0;

        int sumOverall = 0;
        int sumConfidence = 0;
        int sumKnowledge = 0;
        int sumCommunication = 0;
        int sumClarity = 0;
        int sumRelevance = 0;
        int sumEmotional = 0;
        int sumTechnicalDepth = 0;
        int sumProblemSolving = 0;

        int counted = 0;
        long durationSum = 0;

        List<InterviewScoreResponse.QuestionScore> questionScores = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> focusAreas = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        for (InterviewTurn turn : turns) {
            if (Boolean.TRUE.equals(turn.getSkipped())) {
                skippedQuestions++;
            } else if (hasText(turn.getAnswer()) || hasText(turn.getTranscript())) {
                answeredQuestions++;
            }

            if (Boolean.TRUE.equals(turn.getHintUsed())) {
                hintsUsed++;
            }

            if (turn.getDurationSeconds() != null) {
                durationSum += turn.getDurationSeconds();
            }

            InterviewEvaluation latestEval = interviewEvaluationRepository
                    .findTopByInterviewTurnIdOrderByCreatedAtDesc(turn.getId())
                    .orElse(null);

            InterviewScoreResponse.QuestionScore qs = new InterviewScoreResponse.QuestionScore();
            qs.setTurnId(turn.getId());
            qs.setQuestionIndex(turn.getQuestionIndex());
            qs.setQuestionType(turn.getQuestionType());
            qs.setQuestion(turn.getQuestion());
            qs.setSkipped(turn.getSkipped());
            qs.setHintUsed(turn.getHintUsed());
            qs.setDurationSeconds(turn.getDurationSeconds());

            if (latestEval != null) {
                qs.setScore(latestEval.getOverallScore());
                qs.setConfidenceScore(latestEval.getConfidenceScore());
                qs.setKnowledgeScore(latestEval.getKnowledgeScore());
                qs.setCommunicationScore(latestEval.getCommunicationScore());
                qs.setClarityScore(latestEval.getClarityScore());
                qs.setRelevanceScore(latestEval.getRelevanceScore());
                qs.setSummaryFeedback(latestEval.getSummary());

                sumOverall += nullSafe(latestEval.getOverallScore());
                sumConfidence += nullSafe(latestEval.getConfidenceScore());
                sumKnowledge += nullSafe(latestEval.getKnowledgeScore());
                sumCommunication += nullSafe(latestEval.getCommunicationScore());
                sumClarity += nullSafe(latestEval.getClarityScore());
                sumRelevance += nullSafe(latestEval.getRelevanceScore());
                sumEmotional += nullSafe(latestEval.getEmotionalComposureScore());
                sumTechnicalDepth += nullSafe(latestEval.getTechnicalDepthScore());
                sumProblemSolving += nullSafe(latestEval.getProblemSolvingScore());
                counted++;

                strengths.addAll(splitLines(latestEval.getStrengths()));
                weaknesses.addAll(splitLines(latestEval.getWeaknesses()));
                suggestions.addAll(splitLines(latestEval.getImprovementSuggestions()));
                focusAreas.addAll(splitLines(latestEval.getMissingConcepts()));
            } else {
                qs.setScore(turn.getScore());
                qs.setSummaryFeedback(turn.getFeedbackSummary());
            }

            questionScores.add(qs);
        }

        int overallScore = counted > 0 ? Math.round((float) sumOverall / counted) : defaultInt(session.getOverallScore(), 0);
        response.setOverallScore(overallScore);
        response.setConfidenceScore(counted > 0 ? Math.round((float) sumConfidence / counted) : 0);
        response.setKnowledgeScore(counted > 0 ? Math.round((float) sumKnowledge / counted) : 0);
        response.setCommunicationScore(counted > 0 ? Math.round((float) sumCommunication / counted) : 0);
        response.setClarityScore(counted > 0 ? Math.round((float) sumClarity / counted) : 0);
        response.setRelevanceScore(counted > 0 ? Math.round((float) sumRelevance / counted) : 0);
        response.setEmotionalComposureScore(counted > 0 ? Math.round((float) sumEmotional / counted) : 0);
        response.setTechnicalDepthScore(counted > 0 ? Math.round((float) sumTechnicalDepth / counted) : 0);
        response.setProblemSolvingScore(counted > 0 ? Math.round((float) sumProblemSolving / counted) : 0);

        response.setTotalQuestions(totalQuestions);
        response.setAnsweredQuestions(answeredQuestions);
        response.setSkippedQuestions(skippedQuestions);
        response.setHintsUsed(hintsUsed);
        response.setDurationSeconds(calculateSessionDurationSeconds(session));
        response.setAverageAnswerDurationSeconds(answeredQuestions > 0
                ? ((double) durationSum) / Math.max(1, answeredQuestions)
                : 0.0);

        response.setGrade(resolveGrade(overallScore));
        response.setRecommendation(resolveRecommendation(session, overallScore));
        response.setOverallSummary(buildOverallSummary(overallScore, skippedQuestions, hintsUsed));
        response.setStrengths(distinct(strengths));
        response.setWeaknesses(distinct(weaknesses));
        response.setImprovementSuggestions(distinct(suggestions));
        response.setFocusAreas(distinct(focusAreas));
        response.setQuestionScores(questionScores);
        response.setEvaluatedAt(resolveEvaluatedAt(evaluations));
        response.setMessage("Interview score fetched successfully");

        return response;
    }

    // ---------------------------------------------------------------------
    // Scoring helpers
    // ---------------------------------------------------------------------

    private EvaluationBreakdown calculateBreakdown(
            String question,
            String answer,
            String transcript,
            InterviewTurn turn,
            String evaluationMode,
            Integer strictnessLevel,
            boolean skipped
    ) {
        EvaluationBreakdown breakdown = new EvaluationBreakdown();

        if (skipped || !hasText(answer) && !hasText(transcript)) {
            breakdown.overallScore = 20;
            breakdown.confidenceScore = 20;
            breakdown.knowledgeScore = 20;
            breakdown.communicationScore = 25;
            breakdown.clarityScore = 20;
            breakdown.relevanceScore = 20;
            breakdown.emotionalScore = 40;
            breakdown.technicalDepthScore = 20;
            breakdown.problemSolvingScore = 20;
            return breakdown;
        }

        String effectiveAnswer = hasText(answer) ? answer.trim() : transcript.trim();
        int length = effectiveAnswer.length();
        int strictness = clamp(defaultIfNull(strictnessLevel, 3), 1, 5);

        int brevityPenalty = strictness >= 4 ? 12 : 8;
        int hintPenalty = Boolean.TRUE.equals(turn.getHintUsed()) ? (strictness >= 4 ? 8 : 5) : 0;
        int modePenalty = "FINAL".equalsIgnoreCase(trimToNull(evaluationMode)) ? 2 : 0;

        breakdown.confidenceScore = clamp(scoreFromLength(length, 25, 95) - hintPenalty, 0, 100);
        breakdown.knowledgeScore = clamp(scoreKnowledge(question, effectiveAnswer) - modePenalty, 0, 100);
        breakdown.communicationScore = clamp(scoreCommunication(effectiveAnswer) - hintPenalty, 0, 100);
        breakdown.clarityScore = clamp(scoreClarity(effectiveAnswer) - brevityPenalty / 2, 0, 100);
        breakdown.relevanceScore = clamp(scoreRelevance(question, effectiveAnswer), 0, 100);
        breakdown.emotionalScore = clamp(scoreEmotion(turn.getDurationSeconds()), 0, 100);
        breakdown.technicalDepthScore = clamp(scoreTechnicalDepth(turn.getQuestionType(), effectiveAnswer) - modePenalty, 0, 100);
        breakdown.problemSolvingScore = clamp(scoreProblemSolving(effectiveAnswer), 0, 100);

        breakdown.overallScore = Math.round(
                (breakdown.confidenceScore
                        + breakdown.knowledgeScore
                        + breakdown.communicationScore
                        + breakdown.clarityScore
                        + breakdown.relevanceScore
                        + breakdown.emotionalScore
                        + breakdown.technicalDepthScore
                        + breakdown.problemSolvingScore) / 8.0f
        );

        return breakdown;
    }

    private int scoreKnowledge(String question, String answer) {
        int base = scoreFromLength(answer.length(), 30, 92);
        if (hasQuestionOverlap(question, answer)) {
            base += 6;
        }
        if (containsAny(answer, "because", "design", "architecture", "trade-off", "optimized", "implemented")) {
            base += 6;
        }
        return clamp(base, 0, 100);
    }

    private int scoreCommunication(String answer) {
        int base = scoreFromLength(answer.length(), 35, 92);
        if (containsAny(answer, "first", "then", "finally")) {
            base += 6;
        }
        return clamp(base, 0, 100);
    }

    private int scoreClarity(String answer) {
        int base = scoreFromLength(answer.length(), 30, 90);
        if (containsAny(answer, "for example", "for instance", "in my project")) {
            base += 6;
        }
        return clamp(base, 0, 100);
    }

    private int scoreRelevance(String question, String answer) {
        int score = 35;
        if (hasQuestionOverlap(question, answer)) {
            score += 25;
        }
        score += Math.min(answer.length() / 12, 30);
        return clamp(score, 0, 100);
    }

    private int scoreEmotion(Integer durationSeconds) {
        int seconds = defaultIfNull(durationSeconds, 0);
        if (seconds <= 0) {
            return 65;
        }
        if (seconds < 10) {
            return 55;
        }
        if (seconds <= 120) {
            return 78;
        }
        return 72;
    }

    private int scoreTechnicalDepth(String questionType, String answer) {
        int base = scoreFromLength(answer.length(), 25, 90);
        if ("TECHNICAL".equalsIgnoreCase(questionType)
                || "CODING".equalsIgnoreCase(questionType)
                || "PROJECT".equalsIgnoreCase(questionType)
                || "RESUME".equalsIgnoreCase(questionType)) {
            if (containsAny(answer, "api", "database", "algorithm", "complexity", "spring", "java", "design")) {
                base += 8;
            }
        }
        return clamp(base, 0, 100);
    }

    private int scoreProblemSolving(String answer) {
        int base = scoreFromLength(answer.length(), 25, 88);
        if (containsAny(answer, "approach", "steps", "solution", "edge case", "challenge", "resolved")) {
            base += 8;
        }
        return clamp(base, 0, 100);
    }

    // ---------------------------------------------------------------------
    // Feedback content builders
    // ---------------------------------------------------------------------

    private String buildSummaryText(int overallScore, boolean skipped) {
        if (skipped) {
            return "Question skipped during evaluation";
        }
        if (overallScore >= 85) {
            return "Excellent evaluated answer";
        }
        if (overallScore >= 70) {
            return "Good evaluated answer";
        }
        if (overallScore >= 55) {
            return "Average evaluated answer";
        }
        return "Weak evaluated answer";
    }

    private String buildFeedbackText(InterviewTurn turn, EvaluationBreakdown breakdown) {
        if (Boolean.TRUE.equals(turn.getSkipped())) {
            return "The question was skipped. In future interviews, try stating your partial understanding instead of skipping entirely.";
        }

        if (breakdown.overallScore >= 85) {
            return "This answer is strong overall. It is reasonably clear, relevant, and demonstrates good confidence. Continue giving concrete examples and measurable outcomes.";
        }
        if (breakdown.overallScore >= 70) {
            return "This answer is good, but it can be improved with better technical depth, sharper structure, and stronger real-world examples.";
        }
        if (breakdown.overallScore >= 55) {
            return "This answer shows some understanding, but it needs more clarity, better relevance to the question, and a more structured explanation.";
        }
        return "This answer needs major improvement in clarity, depth, and direct relevance. Start with the main point, then support it with one real example and outcome.";
    }

    private String buildExplanationText(String question, String answer, EvaluationBreakdown breakdown) {
        StringBuilder sb = new StringBuilder();
        sb.append("Evaluation considered the quality of the answer against the question, including clarity, relevance, communication, confidence, and depth.");

        if (hasQuestionOverlap(question, answer)) {
            sb.append(" The answer included terms that aligned with the question.");
        } else {
            sb.append(" The answer could align more directly with the exact question asked.");
        }

        if (breakdown.technicalDepthScore < 60) {
            sb.append(" More technical details, trade-offs, or implementation insight would improve the response.");
        }

        return sb.toString();
    }

    private List<String> buildStrengths(InterviewTurn turn, EvaluationBreakdown breakdown) {
        List<String> strengths = new ArrayList<>();
        if (breakdown.confidenceScore >= 75) strengths.add("Reasonable confidence in delivery");
        if (breakdown.communicationScore >= 75) strengths.add("Clear communication flow");
        if (breakdown.relevanceScore >= 75) strengths.add("Answer stayed relevant to the question");
        if (breakdown.technicalDepthScore >= 75) strengths.add("Good technical depth");
        if (!Boolean.TRUE.equals(turn.getSkipped())) strengths.add("Candidate attempted the question");
        return distinct(strengths);
    }

    private List<String> buildWeaknesses(InterviewTurn turn, EvaluationBreakdown breakdown) {
        List<String> weaknesses = new ArrayList<>();
        if (Boolean.TRUE.equals(turn.getSkipped())) weaknesses.add("Question was skipped");
        if (breakdown.clarityScore < 60) weaknesses.add("Answer clarity was limited");
        if (breakdown.relevanceScore < 60) weaknesses.add("Answer could be more directly relevant");
        if (breakdown.technicalDepthScore < 60) weaknesses.add("Technical depth was limited");
        if (Boolean.TRUE.equals(turn.getHintUsed())) weaknesses.add("Answer depended on hint assistance");
        return distinct(weaknesses);
    }

    private List<String> buildImprovementSuggestions(InterviewTurn turn, EvaluationBreakdown breakdown) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Start with a direct answer before adding details");
        suggestions.add("Use one concrete example from your real project or experience");
        suggestions.add("End with the outcome, impact, or learning");
        if (turn != null && Boolean.TRUE.equals(turn.getSkipped())) {
            suggestions.add("If a question is skipped, explain partial understanding rather than staying silent.");
        }
        if (breakdown.technicalDepthScore < 65) {
            suggestions.add("Explain implementation details, trade-offs, or design decisions");
        }
        if (breakdown.relevanceScore < 65) {
            suggestions.add("Stay closer to the exact wording and intent of the question");
        }
        return distinct(suggestions);
    }

    private List<String> buildDetectedSkills(String question, String answer) {
        List<String> skills = new ArrayList<>();
        String combined = (defaultString(question) + " " + defaultString(answer)).toLowerCase();

        if (combined.contains("java")) skills.add("Java");
        if (combined.contains("spring")) skills.add("Spring Boot");
        if (combined.contains("database") || combined.contains("sql")) skills.add("Database");
        if (combined.contains("api")) skills.add("API Design");
        if (combined.contains("project")) skills.add("Project Communication");
        if (combined.contains("algorithm")) skills.add("Algorithms");
        if (combined.contains("system design")) skills.add("System Design");

        return distinct(skills);
    }

    private List<String> buildMissingConcepts(String question, String answer, EvaluationBreakdown breakdown) {
        List<String> missing = new ArrayList<>();

        if (!containsAny(answer, "example", "project", "experience")) {
            missing.add("Concrete example");
        }
        if (!containsAny(answer, "result", "impact", "outcome")) {
            missing.add("Outcome or impact");
        }
        if (breakdown.technicalDepthScore < 60 && containsAny(question, "technical", "design", "project", "coding")) {
            missing.add("Technical depth");
        }

        return distinct(missing);
    }

    private List<String> buildRubricNotes(InterviewEvaluateRequest request, EvaluationBreakdown breakdown) {
        List<String> notes = new ArrayList<>();
        notes.add("Evaluation mode: " + firstNonBlank(request.getEvaluationMode(), "DETAILED"));
        notes.add("Strictness level: " + defaultIfNull(request.getStrictnessLevel(), 3));
        notes.add("Overall score computed from aspect-wise scoring");
        if (breakdown != null) {
            notes.add("Breakdown confidence score: " + breakdown.confidenceScore);
        }
        if (Boolean.TRUE.equals(request.getForceReevaluate())) {
            notes.add("Result generated through re-evaluation flow");
        }
        return notes;
    }

    private String buildNextStepSuggestion(EvaluationBreakdown breakdown) {
        if (breakdown.overallScore >= 80) {
            return "Maintain the same structure and add slightly more measurable impact in future answers.";
        }
        if (breakdown.overallScore >= 60) {
            return "Focus on stronger examples and clearer explanation of your decisions.";
        }
        return "Practice answering in this order: direct point -> explanation -> example -> result.";
    }

    private String buildFollowUpQuestion(String question) {
        if (!hasText(question)) {
            return "Can you explain that with one concrete example?";
        }
        return "Can you support your answer to this question with one specific example from your experience?";
    }

    // ---------------------------------------------------------------------
    // Utility helpers
    // ---------------------------------------------------------------------

    private InterviewSession getRequiredSession(Long sessionId) {
        return interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> InterviewSessionException.notFound(sessionId));
    }

    private long calculateSessionDurationSeconds(InterviewSession session) {
        LocalDateTime start = session.getStartedAt();
        LocalDateTime end = session.getEndedAt() != null ? session.getEndedAt() : LocalDateTime.now();

        if (start == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(start, end).getSeconds());
    }

    private LocalDateTime resolveEvaluatedAt(List<InterviewEvaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return null;
        }
        InterviewEvaluation latest = evaluations.get(evaluations.size() - 1);
        return latest.getEvaluatedAt();
    }

    private String resolveGrade(int overallScore) {
        if (overallScore >= 90) return "A+";
        if (overallScore >= 80) return "A";
        if (overallScore >= 70) return "B";
        if (overallScore >= 60) return "C";
        if (overallScore >= 50) return "D";
        return "Needs Improvement";
    }

    private String resolveRecommendation(InterviewSession session, int overallScore) {
        boolean mock = session.getInterviewMode() != null && "MOCK".equalsIgnoreCase(session.getInterviewMode().name());

        if (mock) {
            if (overallScore >= 80) return "STRONG MOCK PERFORMANCE";
            if (overallScore >= 60) return "GOOD PRACTICE PERFORMANCE";
            return "NEEDS MORE PRACTICE";
        }

        if (overallScore >= 80) return "SELECT";
        if (overallScore >= 65) return "BORDERLINE";
        return "REJECT";
    }

    private String buildOverallSummary(int overallScore, int skippedQuestions, int hintsUsed) {
        StringBuilder sb = new StringBuilder();
        sb.append("Overall interview score: ").append(overallScore).append("/100. ");

        if (overallScore >= 80) {
            sb.append("Performance was strong overall. ");
        } else if (overallScore >= 65) {
            sb.append("Performance was decent but still has room for improvement. ");
        } else {
            sb.append("Performance needs significant improvement before stronger interview rounds. ");
        }

        sb.append("Skipped questions: ").append(skippedQuestions).append(". ");
        sb.append("Hints used: ").append(hintsUsed).append(".");

        return sb.toString();
    }

    private int scoreFromLength(int length, int min, int max) {
        if (length <= 0) {
            return min;
        }
        int scaled = min + (length / 10);
        return clamp(scaled, min, max);
    }

    private boolean hasQuestionOverlap(String question, String answer) {
        if (!hasText(question) || !hasText(answer)) {
            return false;
        }

        String q = question.toLowerCase();
        String a = answer.toLowerCase();

        String[] tokens = q.replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        int hits = 0;
        for (String token : tokens) {
            if (token.length() < 4) continue;
            if (a.contains(token)) hits++;
            if (hits >= 2) return true;
        }
        return false;
    }

    private boolean containsAny(String text, String... candidates) {
        if (!hasText(text) || candidates == null) {
            return false;
        }
        String lower = text.toLowerCase();
        for (String candidate : candidates) {
            if (candidate != null && lower.contains(candidate.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private int defaultInt(Integer... values) {
        Integer result = defaultIfNull(values);
        return result == null ? 0 : result;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
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

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private String joinLines(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join("\n", distinct(values));
    }

    private List<String> splitLines(String value) {
        List<String> result = new ArrayList<>();
        if (!hasText(value)) {
            return result;
        }

        String[] parts = value.split("\\r?\\n");
        for (String part : parts) {
            if (hasText(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private List<String> distinct(List<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            if (!hasText(value)) {
                continue;
            }
            String trimmed = value.trim();
            if (!result.contains(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Internal breakdown holder.
     */
    private static class EvaluationBreakdown {
        private int overallScore;
        private int confidenceScore;
        private int knowledgeScore;
        private int communicationScore;
        private int clarityScore;
        private int relevanceScore;
        private int emotionalScore;
        private int technicalDepthScore;
        private int problemSolvingScore;
    }
}