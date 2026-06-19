package backend.ai_interview.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * InterviewTurn
 *
 * Entity representing one question-answer turn inside an interview session.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - stores the generated question
 * - stores the user's submitted answer/transcript
 * - tracks hint usage / skip state
 * - stores per-turn feedback summary
 * - links to InterviewEvaluation
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - One InterviewSession contains many InterviewTurn records
 * - One turn may later have one or more evaluations depending on re-evaluation flow
 * - Text-heavy fields use LONGTEXT for flexibility
 */
@Entity
@Table(
        name = "interview_turns",
        indexes = {
                @Index(name = "idx_interview_turn_session_id", columnList = "interview_session_id"),
                @Index(name = "idx_interview_turn_question_index", columnList = "question_index"),
                @Index(name = "idx_interview_turn_created_at", columnList = "created_at"),
                @Index(name = "idx_interview_turn_question_type", columnList = "question_type")
        }
)
@SuppressWarnings("all")
public class InterviewTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_turn_id")
    private Long id;

    /**
     * Parent interview session.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    /**
     * 1-based question number within the session.
     */
    @Column(name = "question_index", nullable = false)
    private Integer questionIndex;

    /**
     * Question type.
     * Example:
     * - TECHNICAL
     * - HR
     * - MIXED
     * - PROJECT
     * - RESUME
     * - BEHAVIORAL
     * - CODING
     */
    @Column(name = "question_type", length = 50)
    private String questionType;

    /**
     * Optional category/subdomain.
     * Example:
     * - Java
     * - Spring Boot
     * - DBMS
     */
    @Column(name = "category", length = 255)
    private String category;

    /**
     * Difficulty of the specific question.
     */
    @Column(name = "difficulty")
    private Integer difficulty;

    /**
     * The AI-generated or system-generated question text.
     */
    @Lob
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    /**
     * Optional short source summary.
     * Example:
     * - Generated from resume project
     * - Generated from job description
     */
    @Column(name = "source_summary", length = 1000)
    private String sourceSummary;

    /**
     * Flags describing question origin.
     */
    @Column(name = "resume_based", nullable = false)
    private Boolean resumeBased = Boolean.FALSE;

    @Column(name = "github_based", nullable = false)
    private Boolean githubBased = Boolean.FALSE;

    @Column(name = "job_description_based", nullable = false)
    private Boolean jobDescriptionBased = Boolean.FALSE;

    /**
     * User answer text.
     */
    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    /**
     * Speech transcript if answer came from audio.
     */
    @Lob
    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;

    /**
     * Optional input language used by candidate.
     */
    @Column(name = "answer_language", length = 50)
    private String answerLanguage;

    /**
     * Whether answer was captured via speech flow.
     */
    @Column(name = "speech_based", nullable = false)
    private Boolean speechBased = Boolean.FALSE;

    /**
     * Whether candidate skipped the question.
     */
    @Column(name = "skipped", nullable = false)
    private Boolean skipped = Boolean.FALSE;

    /**
     * Whether a hint was requested/used for this turn.
     */
    @Column(name = "hint_used", nullable = false)
    private Boolean hintUsed = Boolean.FALSE;

    /**
     * Whether a sample answer was shown for this turn.
     * Mostly useful in mock mode.
     */
    @Column(name = "sample_answer_used", nullable = false)
    private Boolean sampleAnswerUsed = Boolean.FALSE;

    /**
     * Whether answer has already been evaluated.
     */
    @Column(name = "evaluated", nullable = false)
    private Boolean evaluated = Boolean.FALSE;

    /**
     * Duration taken by user to answer in seconds.
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Optional quick score for this turn.
     * Detailed scoring belongs in InterviewEvaluation.
     */
    @Column(name = "score")
    private Integer score;

    /**
     * Optional short feedback summary for quick UI rendering.
     */
    @Lob
    @Column(name = "feedback_summary", columnDefinition = "TEXT")
    private String feedbackSummary;

    /**
     * Optional hint text given for this turn.
     */
    @Lob
    @Column(name = "hint_text", columnDefinition = "TEXT")
    private String hintText;

    /**
     * Optional sample answer text shown to candidate.
     */
    @Lob
    @Column(name = "sample_answer", columnDefinition = "TEXT")
    private String sampleAnswer;

    /**
     * Optional follow-up question suggestion from AI.
     */
    @Lob
    @Column(name = "follow_up_question", columnDefinition = "TEXT")
    private String followUpQuestion;

    /**
     * Optional client timestamp string from frontend.
     */
    @Column(name = "client_timestamp", length = 100)
    private String clientTimestamp;

    /**
     * Timestamps.
     */
    @Column(name = "asked_at")
    private LocalDateTime askedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public InterviewTurn() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.askedAt == null) {
            this.askedAt = now;
        }
        if (this.resumeBased == null) {
            this.resumeBased = Boolean.FALSE;
        }
        if (this.githubBased == null) {
            this.githubBased = Boolean.FALSE;
        }
        if (this.jobDescriptionBased == null) {
            this.jobDescriptionBased = Boolean.FALSE;
        }
        if (this.speechBased == null) {
            this.speechBased = Boolean.FALSE;
        }
        if (this.skipped == null) {
            this.skipped = Boolean.FALSE;
        }
        if (this.hintUsed == null) {
            this.hintUsed = Boolean.FALSE;
        }
        if (this.sampleAnswerUsed == null) {
            this.sampleAnswerUsed = Boolean.FALSE;
        }
        if (this.evaluated == null) {
            this.evaluated = Boolean.FALSE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markAnswered(String answer, String transcript, Integer durationSeconds) {
        this.answer = answer;
        this.transcript = transcript;
        this.durationSeconds = durationSeconds;
        this.answeredAt = LocalDateTime.now();
    }

    public void markSkipped() {
        this.skipped = Boolean.TRUE;
        this.answeredAt = LocalDateTime.now();
    }

    public void markHintUsed(String hintText) {
        this.hintUsed = Boolean.TRUE;
        this.hintText = hintText;
    }

    public void markSampleAnswerUsed(String sampleAnswer) {
        this.sampleAnswerUsed = Boolean.TRUE;
        this.sampleAnswer = sampleAnswer;
    }

    public void markEvaluated(Integer score, String feedbackSummary) {
        this.evaluated = Boolean.TRUE;
        this.score = score;
        this.feedbackSummary = feedbackSummary;
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

    public Integer getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(Integer questionIndex) {
        this.questionIndex = questionIndex;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSourceSummary() {
        return sourceSummary;
    }

    public void setSourceSummary(String sourceSummary) {
        this.sourceSummary = sourceSummary;
    }

    public Boolean getResumeBased() {
        return resumeBased;
    }

    public void setResumeBased(Boolean resumeBased) {
        this.resumeBased = resumeBased;
    }

    public Boolean getGithubBased() {
        return githubBased;
    }

    public void setGithubBased(Boolean githubBased) {
        this.githubBased = githubBased;
    }

    public Boolean getJobDescriptionBased() {
        return jobDescriptionBased;
    }

    public void setJobDescriptionBased(Boolean jobDescriptionBased) {
        this.jobDescriptionBased = jobDescriptionBased;
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

    public String getAnswerLanguage() {
        return answerLanguage;
    }

    public void setAnswerLanguage(String answerLanguage) {
        this.answerLanguage = answerLanguage;
    }

    public Boolean getSpeechBased() {
        return speechBased;
    }

    public void setSpeechBased(Boolean speechBased) {
        this.speechBased = speechBased;
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

    public Boolean getSampleAnswerUsed() {
        return sampleAnswerUsed;
    }

    public void setSampleAnswerUsed(Boolean sampleAnswerUsed) {
        this.sampleAnswerUsed = sampleAnswerUsed;
    }

    public Boolean getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(Boolean evaluated) {
        this.evaluated = evaluated;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getFeedbackSummary() {
        return feedbackSummary;
    }

    public void setFeedbackSummary(String feedbackSummary) {
        this.feedbackSummary = feedbackSummary;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public String getSampleAnswer() {
        return sampleAnswer;
    }

    public void setSampleAnswer(String sampleAnswer) {
        this.sampleAnswer = sampleAnswer;
    }

    public String getFollowUpQuestion() {
        return followUpQuestion;
    }

    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
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
