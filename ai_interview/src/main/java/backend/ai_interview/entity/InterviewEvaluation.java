package backend.ai_interview.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * InterviewEvaluation
 *
 * Entity representing detailed evaluation/scoring for an interview turn
 * or for a broader session-level assessment.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - stores aspect-wise scores for an answer/session
 * - stores evaluator feedback summary
 * - stores strengths, weaknesses, and improvement suggestions
 * - supports re-evaluation flow
 * - links evaluation to InterviewSession and optionally InterviewTurn
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Most evaluations will be turn-level (linked to InterviewTurn)
 * - Final session summary evaluation may exist without a specific turn
 * - Text-heavy fields use LONGTEXT for flexibility
 */
@Entity
@Table(
        name = "interview_evaluations",
        indexes = {
                @Index(name = "idx_interview_eval_session_id", columnList = "interview_session_id"),
                @Index(name = "idx_interview_eval_turn_id", columnList = "interview_turn_id"),
                @Index(name = "idx_interview_eval_type", columnList = "evaluation_type"),
                @Index(name = "idx_interview_eval_created_at", columnList = "created_at")
        }
)
@SuppressWarnings("all")
public class InterviewEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_evaluation_id")
    private Long id;

    /**
     * Parent interview session.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    /**
     * Related interview turn.
     * Nullable for final session-level evaluation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_turn_id")
    private InterviewTurn interviewTurn;

    /**
     * Evaluation type.
     * Example:
     * - TURN
     * - FINAL
     * - RE_EVALUATION
     * - HINT_REVIEW
     */
    @Column(name = "evaluation_type", nullable = false, length = 50)
    private String evaluationType = "TURN";

    /**
     * Evaluation mode.
     * Example:
     * - QUICK
     * - DETAILED
     * - FINAL
     */
    @Column(name = "evaluation_mode", length = 50)
    private String evaluationMode;

    /**
     * Whether this evaluation was forced re-evaluation.
     */
    @Column(name = "forced_reevaluation", nullable = false)
    private Boolean forcedReevaluation = Boolean.FALSE;

    /**
     * Overall score out of 100.
     */
    @Column(name = "overall_score")
    private Integer overallScore;

    /**
     * Aspect-wise scoring.
     */
    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "knowledge_score")
    private Integer knowledgeScore;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Column(name = "clarity_score")
    private Integer clarityScore;

    @Column(name = "relevance_score")
    private Integer relevanceScore;

    @Column(name = "emotional_composure_score")
    private Integer emotionalComposureScore;

    @Column(name = "technical_depth_score")
    private Integer technicalDepthScore;

    @Column(name = "problem_solving_score")
    private Integer problemSolvingScore;

    @Column(name = "professionalism_score")
    private Integer professionalismScore;

    @Column(name = "presence_score")
    private Integer presenceScore;

    /**
     * Short textual summary.
     */
    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * Main evaluation feedback.
     */
    @Lob
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    /**
     * Explanation or reasoning for score.
     */
    @Lob
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    /**
     * Strengths, weaknesses, suggestions, notes.
     * Stored as newline-separated text for simplicity.
     */
    @Lob
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Lob
    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Lob
    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Lob
    @Column(name = "detected_skills", columnDefinition = "TEXT")
    private String detectedSkills;

    @Lob
    @Column(name = "missing_concepts", columnDefinition = "TEXT")
    private String missingConcepts;

    @Lob
    @Column(name = "rubric_notes", columnDefinition = "TEXT")
    private String rubricNotes;

    /**
     * Optional next-step suggestion or follow-up.
     */
    @Lob
    @Column(name = "next_step_suggestion", columnDefinition = "TEXT")
    private String nextStepSuggestion;

    @Lob
    @Column(name = "follow_up_question", columnDefinition = "TEXT")
    private String followUpQuestion;

    /**
     * Whether candidate is ready to move ahead.
     */
    @Column(name = "ready_for_next_question")
    private Boolean readyForNextQuestion;

    /**
     * Optional grade / recommendation.
     * Example:
     * - A
     * - B+
     * - SELECT
     * - BORDERLINE
     * - NEEDS_IMPROVEMENT
     */
    @Column(name = "grade", length = 50)
    private String grade;

    @Column(name = "recommendation", length = 100)
    private String recommendation;

    /**
     * Optional metadata about evaluation source.
     * Example:
     * - AI_ENGINE
     * - MANUAL_REVIEW
     */
    @Column(name = "evaluation_source", length = 50)
    private String evaluationSource = "AI_ENGINE";

    /**
     * Optional latency/time taken for evaluation generation in ms.
     */
    @Column(name = "evaluation_latency_ms")
    private Long evaluationLatencyMs;

    /**
     * Client timestamp from frontend, if any.
     */
    @Column(name = "client_timestamp", length = 100)
    private String clientTimestamp;

    /**
     * Timestamps.
     */
    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public InterviewEvaluation() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.evaluatedAt == null) {
            this.evaluatedAt = now;
        }
        if (this.evaluationType == null || this.evaluationType.isBlank()) {
            this.evaluationType = "TURN";
        }
        if (this.forcedReevaluation == null) {
            this.forcedReevaluation = Boolean.FALSE;
        }
        if (this.readyForNextQuestion == null) {
            this.readyForNextQuestion = Boolean.FALSE;
        }
        if (this.evaluationSource == null || this.evaluationSource.isBlank()) {
            this.evaluationSource = "AI_ENGINE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markEvaluated(Integer overallScore, String summary, String feedback) {
        this.overallScore = overallScore;
        this.summary = summary;
        this.feedback = feedback;
        this.evaluatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewSession getInterviewSession() {
        return interviewSession;
    }

    public void setInterviewSession(InterviewSession interviewSession) {
        this.interviewSession = interviewSession;
    }

    public InterviewTurn getInterviewTurn() {
        return interviewTurn;
    }

    public void setInterviewTurn(InterviewTurn interviewTurn) {
        this.interviewTurn = interviewTurn;
    }

    public String getEvaluationType() {
        return evaluationType;
    }

    public void setEvaluationType(String evaluationType) {
        this.evaluationType = evaluationType;
    }

    public String getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(String evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    public Boolean getForcedReevaluation() {
        return forcedReevaluation;
    }

    public void setForcedReevaluation(Boolean forcedReevaluation) {
        this.forcedReevaluation = forcedReevaluation;
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

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(String improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    public String getDetectedSkills() {
        return detectedSkills;
    }

    public void setDetectedSkills(String detectedSkills) {
        this.detectedSkills = detectedSkills;
    }

    public String getMissingConcepts() {
        return missingConcepts;
    }

    public void setMissingConcepts(String missingConcepts) {
        this.missingConcepts = missingConcepts;
    }

    public String getRubricNotes() {
        return rubricNotes;
    }

    public void setRubricNotes(String rubricNotes) {
        this.rubricNotes = rubricNotes;
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

    public String getEvaluationSource() {
        return evaluationSource;
    }

    public void setEvaluationSource(String evaluationSource) {
        this.evaluationSource = evaluationSource;
    }

    public Long getEvaluationLatencyMs() {
        return evaluationLatencyMs;
    }

    public void setEvaluationLatencyMs(Long evaluationLatencyMs) {
        this.evaluationLatencyMs = evaluationLatencyMs;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
