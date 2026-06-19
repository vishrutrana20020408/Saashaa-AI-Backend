package backend.ai_interview.entity;

import backend.ai_interview.entity.enums.InterviewMode;
import backend.ai_interview.entity.enums.InterviewStatus;
import backend.ai_interview.entity.enums.InterviewType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * InterviewSession
 *
 * Entity representing one full AI interview session.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - stores session-level configuration
 * - tracks session lifecycle
 * - links interview turns and evaluations
 * - stores context references such as resume/job description/GitHub inputs
 *
 * -------------------------------------------------------------------------
 * SUPPORTED MODES
 * -------------------------------------------------------------------------
 * - MOCK
 * - REAL
 *
 * -------------------------------------------------------------------------
 * SUPPORTED TYPES
 * -------------------------------------------------------------------------
 * - TECHNICAL
 * - HR
 * - MIXED
 * - PROJECT
 * - RESUME
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - This entity is designed to work for both USER and ADMIN interview flows.
 * - If your project later separates User/Admin models differently, you can
 *   replace the primitive ids with entity relationships.
 * - GitHub URLs are stored as newline-separated text for simplicity.
 */
@Entity
@Table(
        name = "interview_sessions",
        indexes = {
                @Index(name = "idx_interview_session_user_id", columnList = "user_id"),
                @Index(name = "idx_interview_session_admin_id", columnList = "admin_id"),
                @Index(name = "idx_interview_session_status", columnList = "status"),
                @Index(name = "idx_interview_session_type", columnList = "interview_type"),
                @Index(name = "idx_interview_session_mode", columnList = "interview_mode"),
                @Index(name = "idx_interview_session_created_at", columnList = "created_at")
        }
)
@SuppressWarnings("all")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_session_id")
    private Long id;

    /**
     * Optional user id for user-side interviews.
     * Keep as scalar id for now to avoid forcing a specific relationship model.
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Optional admin id for admin-side interviews.
     */
    @Column(name = "admin_id")
    private Long adminId;

    /**
     * Unique token created for each interview session.
     * This token is used for per-interview section routing,
     * review and feedback operations.
     */
    @Column(name = "interview_token", length = 128, unique = true)
    private String interviewToken;

    /**
     * Optional linked resume id.
     */
    @Column(name = "resume_id")
    private Long resumeId;

    /**
     * Optional linked resume version id.
     */
    @Column(name = "resume_version_id")
    private Long resumeVersionId;

    /**
     * Interview type.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 50)
    private InterviewType interviewType;

    /**
     * Interview mode.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_mode", nullable = false, length = 50)
    private InterviewMode interviewMode;

    /**
     * Session status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private InterviewStatus status = InterviewStatus.CREATED;

    /**
     * Target role/job title.
     */
    @Column(name = "role", length = 255)
    private String role;

    /**
     * Optional domain/subdomain.
     */
    @Column(name = "domain", length = 255)
    private String domain;

    /**
     * Difficulty 1-5.
     */
    @Column(name = "difficulty")
    private Integer difficulty = 3;

    /**
     * Total planned questions.
     */
    @Column(name = "total_questions")
    private Integer totalQuestions = 10;

    /**
     * Current 1-based question index.
     */
    @Column(name = "current_question_index")
    private Integer currentQuestionIndex = 0;

    /**
     * Optional maximum duration in minutes.
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Whether hints are allowed in this session.
     */
    @Column(name = "allow_hints", nullable = false)
    private Boolean allowHints = Boolean.TRUE;

    /**
     * Whether behavioral questions are enabled.
     */
    @Column(name = "include_behavioral", nullable = false)
    private Boolean includeBehavioral = Boolean.TRUE;

    /**
     * Whether technical questions are enabled.
     */
    @Column(name = "include_technical", nullable = false)
    private Boolean includeTechnical = Boolean.TRUE;

    /**
     * Whether session uses resume context.
     */
    @Column(name = "resume_based", nullable = false)
    private Boolean resumeBased = Boolean.FALSE;

    /**
     * Whether session uses GitHub/project context.
     */
    @Column(name = "github_based", nullable = false)
    private Boolean githubBased = Boolean.FALSE;

    /**
     * Whether session uses job description context.
     */
    @Column(name = "job_description_based", nullable = false)
    private Boolean jobDescriptionBased = Boolean.FALSE;

    /**
     * Preferred spoken input language from candidate.
     * AI may understand multiple Indian languages but responds in English.
     */
    @Column(name = "preferred_language", length = 50)
    private String preferredLanguage;

    /**
     * Job description text if supplied.
     */
    @Lob
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    /**
     * Newline-separated GitHub URLs used as input context.
     */
    @Lob
    @Column(name = "github_urls", columnDefinition = "TEXT")
    private String githubUrls;

    /**
     * Optional short feedback summary after completion.
     */
    @Lob
    @Column(name = "feedback_summary", columnDefinition = "TEXT")
    private String feedbackSummary;

    /**
     * Final overall score out of 100, when available.
     */
    @Column(name = "overall_score")
    private Integer overallScore;

    /**
     * Whether final score has been computed.
     */
    @Column(name = "score_computed", nullable = false)
    private Boolean scoreComputed = Boolean.FALSE;

    /**
     * Whether session was cancelled by user/system.
     */
    @Column(name = "cancelled", nullable = false)
    private Boolean cancelled = Boolean.FALSE;

    /**
     * Optional cancel reason.
     */
    @Column(name = "cancel_reason", length = 1000)
    private String cancelReason;

    /**
     * Timestamps.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Related Q&A turns.
     */
    @OneToMany(
            mappedBy = "interviewSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("createdAt ASC")
    private List<InterviewTurn> turns = new ArrayList<>();

    /**
     * Related evaluations.
     */
    @OneToMany(
            mappedBy = "interviewSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("createdAt ASC")
    private List<InterviewEvaluation> evaluations = new ArrayList<>();

    public InterviewSession() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastActivityAt = now;
        if (this.status == null) {
            this.status = InterviewStatus.CREATED;
        }
        if (this.currentQuestionIndex == null) {
            this.currentQuestionIndex = 0;
        }
        if (this.totalQuestions == null) {
            this.totalQuestions = 10;
        }
        if (this.difficulty == null) {
            this.difficulty = 3;
        }
        if (this.allowHints == null) {
            this.allowHints = Boolean.TRUE;
        }
        if (this.includeBehavioral == null) {
            this.includeBehavioral = Boolean.TRUE;
        }
        if (this.includeTechnical == null) {
            this.includeTechnical = Boolean.TRUE;
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
        if (this.scoreComputed == null) {
            this.scoreComputed = Boolean.FALSE;
        }
        if (this.cancelled == null) {
            this.cancelled = Boolean.FALSE;
        }
        if (this.interviewToken == null || this.interviewToken.isBlank()) {
            this.interviewToken = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    public void markStarted() {
        this.status = InterviewStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = this.startedAt;
    }

    public void markCompleted(Integer overallScore, String feedbackSummary) {
        this.status = InterviewStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
        this.lastActivityAt = this.endedAt;
        this.overallScore = overallScore;
        this.feedbackSummary = feedbackSummary;
        this.scoreComputed = overallScore != null;
    }

    public void markCancelled(String reason) {
        this.status = InterviewStatus.CANCELLED;
        this.cancelled = Boolean.TRUE;
        this.cancelReason = reason;
        this.endedAt = LocalDateTime.now();
        this.lastActivityAt = this.endedAt;
    }

    public void incrementQuestionIndex() {
        if (this.currentQuestionIndex == null) {
            this.currentQuestionIndex = 1;
        } else {
            this.currentQuestionIndex++;
        }
    }

    public void touchActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public List<String> getGithubUrlList() {
        if (githubUrls == null || githubUrls.isBlank()) {
            return new ArrayList<>();
        }

        String[] lines = githubUrls.split("\\r?\\n");
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                result.add(line.trim());
            }
        }
        return result;
    }

    public void setGithubUrlList(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            this.githubUrls = null;
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String url : urls) {
            if (url != null && !url.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append("\n");
                }
                sb.append(url.trim());
            }
        }
        this.githubUrls = sb.isEmpty() ? null : sb.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getInterviewToken() {
        return interviewToken;
    }

    public void setInterviewToken(String interviewToken) {
        this.interviewToken = interviewToken;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getResumeVersionId() {
        return resumeVersionId;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.resumeVersionId = resumeVersionId;
    }

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(InterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public InterviewMode getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(InterviewMode interviewMode) {
        this.interviewMode = interviewMode;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(Integer currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Boolean getAllowHints() {
        return allowHints;
    }

    public void setAllowHints(Boolean allowHints) {
        this.allowHints = allowHints;
    }

    public Boolean getIncludeBehavioral() {
        return includeBehavioral;
    }

    public void setIncludeBehavioral(Boolean includeBehavioral) {
        this.includeBehavioral = includeBehavioral;
    }

    public Boolean getIncludeTechnical() {
        return includeTechnical;
    }

    public void setIncludeTechnical(Boolean includeTechnical) {
        this.includeTechnical = includeTechnical;
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

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getGithubUrls() {
        return githubUrls;
    }

    public void setGithubUrls(String githubUrls) {
        this.githubUrls = githubUrls;
    }

    public String getFeedbackSummary() {
        return feedbackSummary;
    }

    public void setFeedbackSummary(String feedbackSummary) {
        this.feedbackSummary = feedbackSummary;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Boolean getScoreComputed() {
        return scoreComputed;
    }

    public void setScoreComputed(Boolean scoreComputed) {
        this.scoreComputed = scoreComputed;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<InterviewTurn> getTurns() {
        return turns;
    }

    public void setTurns(List<InterviewTurn> turns) {
        this.turns = turns != null ? turns : new ArrayList<>();
    }

    public List<InterviewEvaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<InterviewEvaluation> evaluations) {
        this.evaluations = evaluations != null ? evaluations : new ArrayList<>();
    }
}
