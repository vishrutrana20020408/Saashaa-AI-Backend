package backend.ai_interview.dto.response;

/**
 * UserProfileResponse
 *
 * Response DTO returned when fetching or updating
 * a user's profile in the latest backend-integrated project structure.
 *
 * This includes profile data that may come from:
 * - manual user edits
 * - resume parsing
 * - profile sync from a resume version
 * - profile extraction flows aligned with resume preview/editor modules
 */
@SuppressWarnings("all")
public class UserProfileResponse {

    private boolean success;
    private String message;

    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String headline;
    private String location;

    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    private String profileSummary;

    private String currentCompany;
    private String currentRole;
    private String highestEducation;

    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;
    private Integer experienceYears;
    private boolean verified;
    private String profilePictureUrl;

    /**
     * JSON array string.
     * Example: ["Java","Spring Boot","MySQL"]
     */
    private String topSkillsJson;

    /**
     * JSON summary of experience history.
     */
    private String experienceSummaryJson;

    /**
     * JSON summary of education history.
     */
    private String educationSummaryJson;

    /**
     * Resume source metadata.
     */
    private Long sourceResumeVersionId;
    private String profileSourceType;

    public UserProfileResponse() {
    }

    public UserProfileResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Success factory.
     */
    public static UserProfileResponse ok(String message) {
        return new UserProfileResponse(true, message);
    }

    /**
     * Failure factory.
     */
    public static UserProfileResponse fail(String message) {
        return new UserProfileResponse(false, message);
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getClass10MarksheetUrl() {
        return class10MarksheetUrl;
    }

    public void setClass10MarksheetUrl(String class10MarksheetUrl) {
        this.class10MarksheetUrl = class10MarksheetUrl;
    }

    public String getClass12MarksheetUrl() {
        return class12MarksheetUrl;
    }

    public void setClass12MarksheetUrl(String class12MarksheetUrl) {
        this.class12MarksheetUrl = class12MarksheetUrl;
    }

    public String getGraduationMarksheetUrl() {
        return graduationMarksheetUrl;
    }

    public void setGraduationMarksheetUrl(String graduationMarksheetUrl) {
        this.graduationMarksheetUrl = graduationMarksheetUrl;
    }

    public String getPostGraduationMarksheetUrl() {
        return postGraduationMarksheetUrl;
    }

    public void setPostGraduationMarksheetUrl(String postGraduationMarksheetUrl) {
        this.postGraduationMarksheetUrl = postGraduationMarksheetUrl;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
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

    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", headline='" + headline + '\'' +
                ", location='" + location + '\'' +
                ", linkedinUrl='" + linkedinUrl + '\'' +
                ", githubUrl='" + githubUrl + '\'' +
                ", portfolioUrl='" + portfolioUrl + '\'' +
                ", profileSummary='" + profileSummary + '\'' +
                ", currentCompany='" + currentCompany + '\'' +
                ", currentRole='" + currentRole + '\'' +
                ", highestEducation='" + highestEducation + '\'' +
                ", class10MarksheetUrl='" + class10MarksheetUrl + '\'' +
                ", class12MarksheetUrl='" + class12MarksheetUrl + '\'' +
                ", graduationMarksheetUrl='" + graduationMarksheetUrl + '\'' +
                ", postGraduationMarksheetUrl='" + postGraduationMarksheetUrl + '\'' +
                ", experienceYears=" + experienceYears +
                ", verified=" + verified +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", topSkillsJson='" + topSkillsJson + '\'' +
                ", experienceSummaryJson='" + experienceSummaryJson + '\'' +
                ", educationSummaryJson='" + educationSummaryJson + '\'' +
                ", sourceResumeVersionId=" + sourceResumeVersionId +
                ", profileSourceType='" + profileSourceType + '\'' +
                '}';
    }
}