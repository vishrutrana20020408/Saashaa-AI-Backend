package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InterviewQuestionResponse
 *
 * Response DTO representing a single interview question generated or returned
 * for an interview session.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - start interview response
 * - next question response
 * - fetch current session state
 *
 * -------------------------------------------------------------------------
 * FRONTEND USE
 * -------------------------------------------------------------------------
 * - render current AI question
 * - show question number and type
 * - display category/difficulty
 * - support hint/help availability
 * - optionally show expected answer guidance in mock mode
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - AI may understand user input in multiple Indian languages
 *   but question text should be delivered in English.
 * - This DTO is designed to support both strict interview mode
 *   and mock/practice interview mode.
 */
@SuppressWarnings("all")
public class InterviewQuestionResponse {

    /**
     * Unique question id or turn-linked question id.
     */
    private Long questionId;

    /**
     * Optional session id this question belongs to.
     */
    private Long sessionId;

    /**
     * Current interview turn id, if already created.
     */
    private Long turnId;

    /**
     * The main question text shown to the candidate.
     */
    private String question;

    /**
     * Optional shorter title/label for the question.
     */
    private String title;

    /**
     * Question type.
     * Example:
     * - TECHNICAL
     * - HR
     * - PROJECT
     * - RESUME
     * - BEHAVIORAL
     * - CODING
     */
    private String questionType;

    /**
     * Optional category/subcategory.
     * Example:
     * - Java
     * - Spring Boot
     * - DBMS
     * - Communication
     */
    private String category;

    /**
     * Difficulty level of this question.
     * Example: 1 to 5
     */
    private Integer difficulty;

    /**
     * 1-based index of this question in the interview flow.
     */
    private Integer questionIndex;

    /**
     * Total question count planned for the interview.
     */
    private Integer totalQuestions;

    /**
     * Optional estimated time to answer in seconds.
     */
    private Integer expectedAnswerTimeSeconds;

    /**
     * Whether hints are allowed for this question.
     */
    private Boolean hintAllowed;

    /**
     * Whether a sample answer is allowed.
     * Usually only in mock mode.
     */
    private Boolean sampleAnswerAllowed;

    /**
     * Whether this question was generated from resume context.
     */
    private Boolean resumeBased;

    /**
     * Whether this question was generated from GitHub/project context.
     */
    private Boolean githubBased;

    /**
     * Whether this question was generated from job description context.
     */
    private Boolean jobDescriptionBased;

    /**
     * Optional source summary.
     * Example:
     * - "Generated from resume project: AI Interview System"
     * - "Generated from job description keywords"
     */
    private String sourceSummary;

    /**
     * Optional keywords/topics the question targets.
     */
    private List<String> targetSkills = new ArrayList<>();

    /**
     * Optional follow-up suggestion if frontend wants to visualize progression.
     */
    private String followUpHint;

    /**
     * Optional mock-mode guidance.
     * This should not be shown in strict/real interview mode unless intended.
     */
    private String mockGuidance;

    /**
     * Optional sample answer outline for mock mode.
     */
    private List<String> sampleAnswerOutline = new ArrayList<>();

    /**
     * Optional tags for classification/filtering.
     */
    private List<String> tags = new ArrayList<>();

    /**
     * Whether this question is the final question.
     */
    private Boolean finalQuestion;

    /**
     * Timestamp when question was created/generated.
     */
    private LocalDateTime generatedAt;

    /**
     * Optional frontend message.
     */
    private String message;

    public InterviewQuestionResponse() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(Integer questionIndex) {
        this.questionIndex = questionIndex;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getExpectedAnswerTimeSeconds() {
        return expectedAnswerTimeSeconds;
    }

    public void setExpectedAnswerTimeSeconds(Integer expectedAnswerTimeSeconds) {
        this.expectedAnswerTimeSeconds = expectedAnswerTimeSeconds;
    }

    public Boolean getHintAllowed() {
        return hintAllowed;
    }

    public void setHintAllowed(Boolean hintAllowed) {
        this.hintAllowed = hintAllowed;
    }

    public Boolean getSampleAnswerAllowed() {
        return sampleAnswerAllowed;
    }

    public void setSampleAnswerAllowed(Boolean sampleAnswerAllowed) {
        this.sampleAnswerAllowed = sampleAnswerAllowed;
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

    public String getSourceSummary() {
        return sourceSummary;
    }

    public void setSourceSummary(String sourceSummary) {
        this.sourceSummary = sourceSummary;
    }

    public List<String> getTargetSkills() {
        return targetSkills;
    }

    public void setTargetSkills(List<String> targetSkills) {
        this.targetSkills = targetSkills != null ? targetSkills : new ArrayList<>();
    }

    public String getFollowUpHint() {
        return followUpHint;
    }

    public void setFollowUpHint(String followUpHint) {
        this.followUpHint = followUpHint;
    }

    public String getMockGuidance() {
        return mockGuidance;
    }

    public void setMockGuidance(String mockGuidance) {
        this.mockGuidance = mockGuidance;
    }

    public List<String> getSampleAnswerOutline() {
        return sampleAnswerOutline;
    }

    public void setSampleAnswerOutline(List<String> sampleAnswerOutline) {
        this.sampleAnswerOutline = sampleAnswerOutline != null ? sampleAnswerOutline : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public Boolean getFinalQuestion() {
        return finalQuestion;
    }

    public void setFinalQuestion(Boolean finalQuestion) {
        this.finalQuestion = finalQuestion;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}