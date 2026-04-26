package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InterviewSessionResponse
 *
 * Response DTO representing the full state of an interview session.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - start interview response
 * - fetch session details
 * - finish interview response
 *
 * -------------------------------------------------------------------------
 * FRONTEND USE
 * -------------------------------------------------------------------------
 * - show current question
 * - display conversation history
 * - show progress (question count)
 * - show status (ACTIVE / COMPLETED)
 * - track session timing
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - This DTO is intentionally rich to avoid multiple API calls
 * - Can be trimmed later if needed
 */
@SuppressWarnings("all")
public class InterviewSessionResponse {

    /**
     * Unique session ID.
     */
    private Long sessionId;

    /**
     * Optional user ID.
     */
    private Long userId;

    /**
     * Optional admin ID (if admin interview mode exists).
     */
    private Long adminId;

    /**
     * Interview type:
     * - TECHNICAL
     * - HR
     * - MIXED
     */
    private String interviewType;

    /**
     * Interview mode:
     * - MOCK
     * - REAL
     */
    private String interviewMode;

    /**
     * Target role.
     */
    private String role;

    /**
     * Domain/subdomain.
     */
    private String domain;

    /**
     * Difficulty level (1–5).
     */
    private Integer difficulty;

    /**
     * Total number of questions planned.
     */
    private Integer totalQuestions;

    /**
     * Current question index (1-based).
     */
    private Integer currentQuestionIndex;

    /**
     * Session status:
     * - CREATED
     * - ACTIVE
     * - COMPLETED
     * - CANCELLED
     */
    private String status;

    /**
     * Current active question.
     */
    private InterviewQuestionResponse currentQuestion;

    /**
     * Full conversation history (optional).
     */
    private List<InterviewTurn> turns = new ArrayList<>();

    /**
     * Whether hints are allowed.
     */
    private Boolean allowHints;

    /**
     * Whether interview is based on resume.
     */
    private Boolean resumeBased;

    /**
     * Resume ID used (if any).
     */
    private Long resumeId;

    /**
     * Resume version ID used (if any).
     */
    private Long resumeVersionId;

    /**
     * Optional job description (trimmed or summarized).
     */
    private String jobDescription;

    /**
     * GitHub URLs used for context.
     */
    private List<String> githubUrls = new ArrayList<>();

    /**
     * Preferred language of user input.
     */
    private String preferredLanguage;

    /**
     * Start timestamp.
     */
    private LocalDateTime startedAt;

    /**
     * End timestamp.
     */
    private LocalDateTime endedAt;

    /**
     * Last activity timestamp.
     */
    private LocalDateTime lastActivityAt;

    /**
     * Optional session duration in seconds.
     */
    private Long durationSeconds;

    /**
     * Progress percentage (0–100).
     */
    private Integer progressPercent;

    /**
     * Optional overall score (if completed).
     */
    private Integer overallScore;

    /**
     * Optional feedback summary.
     */
    private String feedbackSummary;

    /**
     * Optional token specific to this interview session.
     */
    private String interviewToken;

    /**
     * Optional token alias used by older frontend flows.
     */
    private String token;

    /**
     * Optional message for frontend.
     */
    private String message;

    public InterviewSessionResponse() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public String getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(String interviewMode) {
        this.interviewMode = interviewMode;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InterviewQuestionResponse getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(InterviewQuestionResponse currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public List<InterviewTurn> getTurns() {
        return turns;
    }

    public void setTurns(List<InterviewTurn> turns) {
        this.turns = turns != null ? turns : new ArrayList<>();
    }

    public Boolean getAllowHints() {
        return allowHints;
    }

    public void setAllowHints(Boolean allowHints) {
        this.allowHints = allowHints;
    }

    public Boolean getResumeBased() {
        return resumeBased;
    }

    public void setResumeBased(Boolean resumeBased) {
        this.resumeBased = resumeBased;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public List<String> getGithubUrls() {
        return githubUrls;
    }

    public void setGithubUrls(List<String> githubUrls) {
        this.githubUrls = githubUrls != null ? githubUrls : new ArrayList<>();
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
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

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getFeedbackSummary() {
        return feedbackSummary;
    }

    public void setFeedbackSummary(String feedbackSummary) {
        this.feedbackSummary = feedbackSummary;
    }

    public String getInterviewToken() {
        return interviewToken;
    }

    public void setInterviewToken(String interviewToken) {
        this.interviewToken = interviewToken;
    }

    public String getToken() {
        return token != null ? token : interviewToken;
    }

    public void setToken(String token) {
        this.token = token;
        this.interviewToken = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Inner class representing one Q&A turn.
     */
    public static class InterviewTurn {

        private Long turnId;
        private String question;
        private String answer;
        private String transcript;
        private Integer score;
        private String feedback;
        private Boolean skipped;
        private Boolean hintUsed;
        private Integer durationSeconds;
        private LocalDateTime createdAt;

        public InterviewTurn() {
        }

        public Long getTurnId() {
            return turnId;
        }

        public void setTurnId(Long turnId) {
            this.turnId = turnId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
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

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
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

        public Integer getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}