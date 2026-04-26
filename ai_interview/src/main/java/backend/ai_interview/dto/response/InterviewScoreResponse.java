package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InterviewScoreResponse
 *
 * Response DTO representing the overall interview scoring summary.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - GET /api/interview/sessions/{sessionId}/score
 * - final interview completion summary
 * - interview dashboard / analytics screens
 *
 * -------------------------------------------------------------------------
 * FRONTEND USE
 * -------------------------------------------------------------------------
 * - show total score out of 100
 * - show aspect-wise scoring breakdown
 * - render strengths / weaknesses / suggestions
 * - show recommendation/result summary
 * - compare interview performance over time
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Designed for both mock and real interview flows
 * - Supports detailed aspect-based scoring
 * - Keeps fields flexible for future analytics
 */
@SuppressWarnings("all")
public class InterviewScoreResponse {

    /**
     * Related interview session id.
     */
    private Long sessionId;

    /**
     * Optional user id.
     */
    private Long userId;

    /**
     * Optional admin id.
     */
    private Long adminId;

    /**
     * Interview type.
     * Example:
     * - TECHNICAL
     * - HR
     * - MIXED
     */
    private String interviewType;

    /**
     * Interview mode.
     * Example:
     * - MOCK
     * - REAL
     */
    private String interviewMode;

    /**
     * Session status.
     * Example:
     * - ACTIVE
     * - COMPLETED
     */
    private String status;

    /**
     * Total score out of 100.
     */
    private Integer overallScore;

    /**
     * Confidence score out of 100.
     */
    private Integer confidenceScore;

    /**
     * Knowledge score out of 100.
     */
    private Integer knowledgeScore;

    /**
     * Communication score out of 100.
     */
    private Integer communicationScore;

    /**
     * Clarity score out of 100.
     */
    private Integer clarityScore;

    /**
     * Relevance score out of 100.
     */
    private Integer relevanceScore;

    /**
     * Emotional composure score out of 100.
     */
    private Integer emotionalComposureScore;

    /**
     * Technical depth score out of 100.
     */
    private Integer technicalDepthScore;

    /**
     * Problem solving score out of 100.
     */
    private Integer problemSolvingScore;

    /**
     * Optional professionalism score.
     */
    private Integer professionalismScore;

    /**
     * Optional body language / presence proxy score.
     * Can remain null if not supported yet.
     */
    private Integer presenceScore;

    /**
     * Number of questions asked in total.
     */
    private Integer totalQuestions;

    /**
     * Number of questions answered.
     */
    private Integer answeredQuestions;

    /**
     * Number of skipped questions.
     */
    private Integer skippedQuestions;

    /**
     * Number of hints used during session.
     */
    private Integer hintsUsed;

    /**
     * Total interview duration in seconds.
     */
    private Long durationSeconds;

    /**
     * Average answer duration in seconds.
     */
    private Double averageAnswerDurationSeconds;

    /**
     * Optional percentile or grade label.
     * Example:
     * - A
     * - B+
     * - Excellent
     * - Needs Improvement
     */
    private String grade;

    /**
     * Recommendation summary.
     * Example:
     * - SELECT
     * - BORDERLINE
     * - REJECT
     * - STRONG MOCK PERFORMANCE
     */
    private String recommendation;

    /**
     * Short overall summary for the candidate.
     */
    private String overallSummary;

    /**
     * High-level strengths observed across the interview.
     */
    private List<String> strengths = new ArrayList<>();

    /**
     * High-level weaknesses/gaps observed across the interview.
     */
    private List<String> weaknesses = new ArrayList<>();

    /**
     * Actionable improvement suggestions.
     */
    private List<String> improvementSuggestions = new ArrayList<>();

    /**
     * Concepts/areas the candidate should revise.
     */
    private List<String> focusAreas = new ArrayList<>();

    /**
     * Optional per-question score breakdown.
     */
    private List<QuestionScore> questionScores = new ArrayList<>();

    /**
     * Optional scoring timestamp.
     */
    private LocalDateTime evaluatedAt;

    /**
     * Optional frontend message.
     */
    private String message;

    public InterviewScoreResponse() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void setAnsweredQuestions(Integer answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public Integer getSkippedQuestions() {
        return skippedQuestions;
    }

    public void setSkippedQuestions(Integer skippedQuestions) {
        this.skippedQuestions = skippedQuestions;
    }

    public Integer getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(Integer hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getAverageAnswerDurationSeconds() {
        return averageAnswerDurationSeconds;
    }

    public void setAverageAnswerDurationSeconds(Double averageAnswerDurationSeconds) {
        this.averageAnswerDurationSeconds = averageAnswerDurationSeconds;
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

    public String getOverallSummary() {
        return overallSummary;
    }

    public void setOverallSummary(String overallSummary) {
        this.overallSummary = overallSummary;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? strengths : new ArrayList<>();
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions != null ? improvementSuggestions : new ArrayList<>();
    }

    public List<String> getFocusAreas() {
        return focusAreas;
    }

    public void setFocusAreas(List<String> focusAreas) {
        this.focusAreas = focusAreas != null ? focusAreas : new ArrayList<>();
    }

    public List<QuestionScore> getQuestionScores() {
        return questionScores;
    }

    public void setQuestionScores(List<QuestionScore> questionScores) {
        this.questionScores = questionScores != null ? questionScores : new ArrayList<>();
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Per-question scoring summary.
     */
    public static class QuestionScore {

        private Long turnId;
        private Integer questionIndex;
        private String questionType;
        private String question;
        private Integer score;
        private Integer confidenceScore;
        private Integer knowledgeScore;
        private Integer communicationScore;
        private Integer clarityScore;
        private Integer relevanceScore;
        private Boolean skipped;
        private Boolean hintUsed;
        private Integer durationSeconds;
        private String summaryFeedback;

        public QuestionScore() {
        }

        public Long getTurnId() {
            return turnId;
        }

        public void setTurnId(Long turnId) {
            this.turnId = turnId;
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

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
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

        public String getSummaryFeedback() {
            return summaryFeedback;
        }

        public void setSummaryFeedback(String summaryFeedback) {
            this.summaryFeedback = summaryFeedback;
        }
    }
}