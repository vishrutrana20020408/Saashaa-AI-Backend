package backend.ai_interview.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * AdminProfileUpdateRequest
 *
 * DTO used to update an admin's profile.
 * This supports manual editing of profile data that may have
 * originally been extracted from a resume or entered directly.
 */
@SuppressWarnings("all")
public class AdminProfileUpdateRequest {

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phone;

    @Size(max = 200, message = "Headline must not exceed 200 characters")
    private String headline;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    private String linkedinUrl;

    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    private String githubUrl;

    @Size(max = 500, message = "Portfolio URL must not exceed 500 characters")
    private String portfolioUrl;

    @Size(max = 2000, message = "Profile summary must not exceed 2000 characters")
    private String profileSummary;

    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;
    private String resumeUrl;
    private Boolean verified;

    /**
     * JSON string containing top skills.
     * Example:
     * ["Leadership","Hiring","Operations"]
     */
    private String topSkillsJson;

    public AdminProfileUpdateRequest() {
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

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}