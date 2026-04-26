package backend.ai_interview.dto.response;

/**
 * ResumeProfileSnapshotResponse
 *
 * Represents a profile snapshot extracted from a specific resume version
 * in the latest backend-integrated project structure.
 *
 * This is NOT the official stored profile. It is parsed resume-derived data
 * used to support:
 * - resume preview pages
 * - resume editor pages
 * - profile sync flows
 * - broader resume-version and resume-tailoring pipelines
 */
@SuppressWarnings("all")
public class ResumeProfileSnapshotResponse {

    private boolean success;
    private String message;

    /**
     * Resume metadata.
     */
    private Long resumeId;
    private Long versionId;

    /**
     * Extracted identity fields.
     */
    private String fullName;
    private String email;
    private String phone;
    private String location;

    /**
     * Professional information.
     */
    private String headline;
    private String profileSummary;

    /**
     * Links.
     */
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    /**
     * Current career info.
     */
    private String currentCompany;
    private String currentRole;
    private String highestEducation;

    /**
     * JSON data extracted from resume.
     */
    private String topSkillsJson;
    private String experienceSummaryJson;
    private String educationSummaryJson;

    public ResumeProfileSnapshotResponse() {
    }

    public ResumeProfileSnapshotResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Success factory.
     */
    public static ResumeProfileSnapshotResponse ok(String message) {
        return new ResumeProfileSnapshotResponse(true, message);
    }

    /**
     * Failure factory.
     */
    public static ResumeProfileSnapshotResponse fail(String message) {
        return new ResumeProfileSnapshotResponse(false, message);
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

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getProfileSummary() {
        return profileSummary;
    }

    public void setProfileSummary(String profileSummary) {
        this.profileSummary = profileSummary;
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

    public String getCurrentCompany() {
        return currentCompany;
    }

    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public String getTopSkillsJson() {
        return topSkillsJson;
    }

    public void setTopSkillsJson(String topSkillsJson) {
        this.topSkillsJson = topSkillsJson;
    }

    public String getExperienceSummaryJson() {
        return experienceSummaryJson;
    }

    public void setExperienceSummaryJson(String experienceSummaryJson) {
        this.experienceSummaryJson = experienceSummaryJson;
    }

    public String getEducationSummaryJson() {
        return educationSummaryJson;
    }

    public void setEducationSummaryJson(String educationSummaryJson) {
        this.educationSummaryJson = educationSummaryJson;
    }

    @Override
    public String toString() {
        return "ResumeProfileSnapshotResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", resumeId=" + resumeId +
                ", versionId=" + versionId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", location='" + location + '\'' +
                ", headline='" + headline + '\'' +
                ", profileSummary='" + profileSummary + '\'' +
                ", linkedinUrl='" + linkedinUrl + '\'' +
                ", githubUrl='" + githubUrl + '\'' +
                ", portfolioUrl='" + portfolioUrl + '\'' +
                ", currentCompany='" + currentCompany + '\'' +
                ", currentRole='" + currentRole + '\'' +
                ", highestEducation='" + highestEducation + '\'' +
                ", topSkillsJson='" + topSkillsJson + '\'' +
                ", experienceSummaryJson='" + experienceSummaryJson + '\'' +
                ", educationSummaryJson='" + educationSummaryJson + '\'' +
                '}';
    }
}