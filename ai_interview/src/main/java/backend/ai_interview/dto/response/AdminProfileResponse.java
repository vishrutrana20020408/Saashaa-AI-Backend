package backend.ai_interview.dto.response;

/**
 * AdminProfileResponse
 *
 * Response DTO returned when fetching or updating
 * an admin's profile.
 *
 * This includes profile data that may come from:
 * - manual admin edits
 * - resume parsing
 * - profile sync from resume version
 */
@SuppressWarnings("all")
public class AdminProfileResponse {

    private boolean success;
    private String message;

    private String adminId;
    private String fullName;
    private String email;
    private String phone;
    private String headline;
    private String location;

    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    private String profileSummary;

    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;
    private String resumeUrl;
    private Boolean verified;
    private String profilePictureUrl;

    /**
     * JSON array string
     * Example: ["Leadership","Hiring","Operations"]
     */
    private String topSkillsJson;

    /**
     * Resume source metadata
     */
    private Long sourceResumeVersionId;
    private String profileSourceType;

    public AdminProfileResponse() {
    }

    public AdminProfileResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Success factory
     */
    public static AdminProfileResponse ok(String message) {
        return new AdminProfileResponse(true, message);
    }

    /**
     * Failure factory
     */
    public static AdminProfileResponse fail(String message) {
        return new AdminProfileResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public String getProfileSummary() {
        return profileSummary;
    }

    public void setProfileSummary(String profileSummary) {
        this.profileSummary = profileSummary;
    }

    public String getTopSkillsJson() {
        return topSkillsJson;
    }

    public void setTopSkillsJson(String topSkillsJson) {
        this.topSkillsJson = topSkillsJson;
    }

    public Long getSourceResumeVersionId() {
        return sourceResumeVersionId;
    }

    public void setSourceResumeVersionId(Long sourceResumeVersionId) {
        this.sourceResumeVersionId = sourceResumeVersionId;
    }

    public String getProfileSourceType() {
        return profileSourceType;
    }

    public void setProfileSourceType(String profileSourceType) {
        this.profileSourceType = profileSourceType;
    }
}