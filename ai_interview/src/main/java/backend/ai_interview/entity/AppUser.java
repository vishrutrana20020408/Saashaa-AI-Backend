package backend.ai_interview.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * AppUser Entity
 *
 * Stores:
 * - authentication/account data
 * - onboarding selections
 * - resume scan status
 * - official lightweight profile-related data
 *
 * Notes:
 * - sNo is the database primary key
 * - userId is the business/public identity
 * - role defaults to USER
 * - structure remains aligned with the latest backend-integrated project flow
 */
@Entity
@Table(
        name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id"),
                @UniqueConstraint(columnNames = "email_address"),
                @UniqueConstraint(columnNames = "share_id")
        }
)
@SuppressWarnings("all")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s_no", nullable = false)
    private Long sNo;

    @Column(name = "user_id", nullable = false, updatable = false, length = 36, unique = true)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "user_created_date", nullable = false)
    private LocalDate userCreatedDate;

    @Column(name = "user_created_time", nullable = false)
    private LocalTime userCreatedTime;

    @Column(name = "share_id", nullable = false, length = 36)
    private String shareId;

    @Column(name = "role", nullable = false)
    private String role;

    public Long getSNo() {
        return this.sNo;
    }

    public void setSNo(Long sNo) {
        this.sNo = sNo;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public java.time.LocalDate getUserCreatedDate() {
        return this.userCreatedDate;
    }

    public void setUserCreatedDate(java.time.LocalDate userCreatedDate) {
        this.userCreatedDate = userCreatedDate;
    }

    public java.time.LocalTime getUserCreatedTime() {
        return this.userCreatedTime;
    }

    public void setUserCreatedTime(java.time.LocalTime userCreatedTime) {
        this.userCreatedTime = userCreatedTime;
    }

    public String getShareId() {
        return this.shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isResumeScanned() {
        return this.resumeScanned;
    }

    public void setResumeScanned(boolean resumeScanned) {
        this.resumeScanned = resumeScanned;
    }

    public String getResumeFileName() {
        return this.resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public Integer getResumeScore() {
        return this.resumeScore;
    }

    public void setResumeScore(Integer resumeScore) {
        this.resumeScore = resumeScore;
    }

    public String getOnboardingDomain() {
        return this.onboardingDomain;
    }

    public void setOnboardingDomain(String onboardingDomain) {
        this.onboardingDomain = onboardingDomain;
    }

    public String getOnboardingSubDomainMode() {
        return this.onboardingSubDomainMode;
    }

    public void setOnboardingSubDomainMode(String onboardingSubDomainMode) {
        this.onboardingSubDomainMode = onboardingSubDomainMode;
    }

    public String getOnboardingSubDomainSingle() {
        return this.onboardingSubDomainSingle;
    }

    public void setOnboardingSubDomainSingle(String onboardingSubDomainSingle) {
        this.onboardingSubDomainSingle = onboardingSubDomainSingle;
    }

    public String getOnboardingSubDomainMulti() {
        return this.onboardingSubDomainMulti;
    }

    public void setOnboardingSubDomainMulti(String onboardingSubDomainMulti) {
        this.onboardingSubDomainMulti = onboardingSubDomainMulti;
    }

    public String getOnboardingJobTitles() {
        return this.onboardingJobTitles;
    }

    public void setOnboardingJobTitles(String onboardingJobTitles) {
        this.onboardingJobTitles = onboardingJobTitles;
    }

    public boolean isOnboardingDone() {
        return this.onboardingDone;
    }

    public void setOnboardingDone(boolean onboardingDone) {
        this.onboardingDone = onboardingDone;
    }

    public String getProfileFullName() {
        return this.profileFullName;
    }

    public void setProfileFullName(String profileFullName) {
        this.profileFullName = profileFullName;
    }

    public String getProfileHeadline() {
        return this.profileHeadline;
    }

    public void setProfileHeadline(String profileHeadline) {
        this.profileHeadline = profileHeadline;
    }

    public String getProfileLocation() {
        return this.profileLocation;
    }

    public void setProfileLocation(String profileLocation) {
        this.profileLocation = profileLocation;
    }

    public String getProfileSummary() {
        return this.profileSummary;
    }

    public void setProfileSummary(String profileSummary) {
        this.profileSummary = profileSummary;
    }

    public String getLinkedinUrl() {
        return this.linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getGithubUrl() {
        return this.githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getPortfolioUrl() {
        return this.portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public String getCurrentCompany() {
        return this.currentCompany;
    }

    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }

    public String getCurrentRole() {
        return this.currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public String getHighestEducation() {
        return this.highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public String getTopSkillsJson() {
        return this.topSkillsJson;
    }

    public void setTopSkillsJson(String topSkillsJson) {
        this.topSkillsJson = topSkillsJson;
    }

    public String getExperienceSummaryJson() {
        return this.experienceSummaryJson;
    }

    public void setExperienceSummaryJson(String experienceSummaryJson) {
        this.experienceSummaryJson = experienceSummaryJson;
    }

    public String getEducationSummaryJson() {
        return this.educationSummaryJson;
    }

    public void setEducationSummaryJson(String educationSummaryJson) {
        this.educationSummaryJson = educationSummaryJson;
    }

    public String getProfileSourceType() {
        return this.profileSourceType;
    }

    public void setProfileSourceType(String profileSourceType) {
        this.profileSourceType = profileSourceType;
    }

    public Long getSourceResumeVersionId() {
        return this.sourceResumeVersionId;
    }

    public void setSourceResumeVersionId(Long sourceResumeVersionId) {
        this.sourceResumeVersionId = sourceResumeVersionId;
    }

    public boolean isProfileCreated() {
        return this.profileCreated;
    }

    public void setProfileCreated(boolean profileCreated) {
        this.profileCreated = profileCreated;
    }

    public String getClass10MarksheetUrl() {
        return this.class10MarksheetUrl;
    }

    public void setClass10MarksheetUrl(String class10MarksheetUrl) {
        this.class10MarksheetUrl = class10MarksheetUrl;
    }

    public String getClass12MarksheetUrl() {
        return this.class12MarksheetUrl;
    }

    public void setClass12MarksheetUrl(String class12MarksheetUrl) {
        this.class12MarksheetUrl = class12MarksheetUrl;
    }

    public String getGraduationMarksheetUrl() {
        return this.graduationMarksheetUrl;
    }

    public void setGraduationMarksheetUrl(String graduationMarksheetUrl) {
        this.graduationMarksheetUrl = graduationMarksheetUrl;
    }

    public String getPostGraduationMarksheetUrl() {
        return this.postGraduationMarksheetUrl;
    }

    public void setPostGraduationMarksheetUrl(String postGraduationMarksheetUrl) {
        this.postGraduationMarksheetUrl = postGraduationMarksheetUrl;
    }

    public Integer getExperienceYears() {
        return this.experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getProfilePictureUrl() {
        return this.profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long sNo;
        private String userId;
        private String name;
        private String surname;
        private String emailAddress;
        private String mobileNumber;
        private String password;
        private java.time.LocalDate userCreatedDate;
        private java.time.LocalTime userCreatedTime;
        private String shareId;
        private String role;
        private boolean onboardingDone;
        private boolean profileCreated;

        public Builder sNo(Long sNo) {
            this.sNo = sNo;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder userCreatedDate(java.time.LocalDate userCreatedDate) {
            this.userCreatedDate = userCreatedDate;
            return this;
        }

        public Builder userCreatedTime(java.time.LocalTime userCreatedTime) {
            this.userCreatedTime = userCreatedTime;
            return this;
        }

        public Builder shareId(String shareId) {
            this.shareId = shareId;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder onboardingDone(boolean onboardingDone) {
            this.onboardingDone = onboardingDone;
            return this;
        }

        public Builder profileCreated(boolean profileCreated) {
            this.profileCreated = profileCreated;
            return this;
        }

        public AppUser build() {
            AppUser user = new AppUser();
            user.setSNo(this.sNo);
            user.setUserId(this.userId);
            user.setName(this.name);
            user.setSurname(this.surname);
            user.setEmailAddress(this.emailAddress);
            user.setMobileNumber(this.mobileNumber);
            user.setPassword(this.password);
            user.setUserCreatedDate(this.userCreatedDate);
            user.setUserCreatedTime(this.userCreatedTime);
            user.setShareId(this.shareId);
            user.setRole(this.role);
            user.setOnboardingDone(this.onboardingDone);
            user.setProfileCreated(this.profileCreated);
            return user;
        }
    }

    // ============================
    // Resume Scan Tracking
    // ============================

    @Column(name = "resume_scanned", nullable = false)
    private boolean resumeScanned = false;

    @Column(name = "resume_file_name")
    private String resumeFileName;

    @Column(name = "resume_score")
    private Integer resumeScore;

    // ============================
    // Onboarding Selections
    // ============================

    @Column(name = "onboarding_domain")
    private String onboardingDomain;

    @Column(name = "onb_subdomain_mode")
    private String onboardingSubDomainMode;

    @Column(name = "onb_subdomain_single")
    private String onboardingSubDomainSingle;

    @Column(name = "onb_subdomain_multi", columnDefinition = "TEXT")
    private String onboardingSubDomainMulti;

    @Column(name = "onb_job_titles", columnDefinition = "TEXT")
    private String onboardingJobTitles;

    @Column(name = "onb_done", nullable = false)
    private boolean onboardingDone = false;

    // ==========================================
    // Official User Profile Data
    // ==========================================

    @Column(name = "profile_full_name")
    private String profileFullName;

    @Column(name = "profile_headline")
    private String profileHeadline;

    @Column(name = "profile_location")
    private String profileLocation;

    @Column(name = "profile_summary", columnDefinition = "TEXT")
    private String profileSummary;

    @Column(name = "linkedin_url", length = 1000)
    private String linkedinUrl;

    @Column(name = "github_url", length = 1000)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 1000)
    private String portfolioUrl;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "current_job_role")
    private String currentRole;

    @Column(name = "highest_education")
    private String highestEducation;

    @Column(name = "top_skills_json", columnDefinition = "TEXT")
    private String topSkillsJson;

    @Column(name = "experience_summary_json", columnDefinition = "TEXT")
    private String experienceSummaryJson;

    @Column(name = "education_summary_json", columnDefinition = "TEXT")
    private String educationSummaryJson;

    @Column(name = "profile_source_type")
    private String profileSourceType;

    @Column(name = "source_resume_version_id")
    private Long sourceResumeVersionId;

    @Column(name = "profile_created", nullable = false)
    private boolean profileCreated = false;

    // ==========================================
    // Profile Verification & Documents
    // ==========================================

    @Column(name = "class_10_marksheet_url", length = 1000)
    private String class10MarksheetUrl;

    @Column(name = "class_12_marksheet_url", length = 1000)
    private String class12MarksheetUrl;

    @Column(name = "graduation_marksheet_url", length = 1000)
    private String graduationMarksheetUrl;

    @Column(name = "post_graduation_marksheet_url", length = 1000)
    private String postGraduationMarksheetUrl;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "profile_picture_url", length = 1000)
    private String profilePictureUrl;

    @PrePersist
    public void prePersist() {
        if (this.userId == null || this.userId.isBlank()) {
            this.userId = UUID.randomUUID().toString();
        }

        if (this.shareId == null || this.shareId.isBlank()) {
            this.shareId = UUID.randomUUID().toString();
        }

        if (this.userCreatedDate == null) {
            this.userCreatedDate = LocalDate.now();
        }

        if (this.userCreatedTime == null) {
            this.userCreatedTime = LocalTime.now().withNano(0);
        }

        normalizeFields();

        if (this.role == null || this.role.isBlank()) {
            this.role = "USER";
        }

        if (this.profileSourceType == null || this.profileSourceType.isBlank()) {
            this.profileSourceType = "MANUAL";
        }

        if (this.profileFullName == null || this.profileFullName.isBlank()) {
            String first = this.name != null ? this.name.trim() : "";
            String last = this.surname != null ? this.surname.trim() : "";
            String combined = (first + " " + last).trim();
            this.profileFullName = combined.isBlank() ? null : combined;
        }
    }

    @PreUpdate
    public void preUpdate() {
        normalizeFields();

        if (this.role == null || this.role.isBlank()) {
            this.role = "USER";
        }

        if (this.profileSourceType == null || this.profileSourceType.isBlank()) {
            this.profileSourceType = "MANUAL";
        }

        if ((this.profileFullName == null || this.profileFullName.isBlank())
                && ((this.name != null && !this.name.isBlank()) || (this.surname != null && !this.surname.isBlank()))) {
            String first = this.name != null ? this.name.trim() : "";
            String last = this.surname != null ? this.surname.trim() : "";
            String combined = (first + " " + last).trim();
            this.profileFullName = combined.isBlank() ? null : combined;
        }
    }

    private void normalizeFields() {
        this.name = trimToEmpty(this.name);
        this.surname = trimToEmpty(this.surname);
        this.emailAddress = normalizeEmail(this.emailAddress);
        this.mobileNumber = trimToEmpty(this.mobileNumber);
        this.password = trimToEmpty(this.password);

        this.role = normalizeRole(this.role);

        this.resumeFileName = trimToNull(this.resumeFileName);

        this.onboardingDomain = trimToEmpty(this.onboardingDomain);
        this.onboardingSubDomainMode = normalizeSubDomainMode(this.onboardingSubDomainMode);
        this.onboardingSubDomainSingle = trimToNull(this.onboardingSubDomainSingle);
        this.onboardingSubDomainMulti = trimToNull(this.onboardingSubDomainMulti);
        this.onboardingJobTitles = trimToNull(this.onboardingJobTitles);

        this.profileFullName = trimToNull(this.profileFullName);
        this.profileHeadline = trimToNull(this.profileHeadline);
        this.profileLocation = trimToNull(this.profileLocation);
        this.profileSummary = trimToNull(this.profileSummary);

        this.linkedinUrl = trimToNull(this.linkedinUrl);
        this.githubUrl = trimToNull(this.githubUrl);
        this.portfolioUrl = trimToNull(this.portfolioUrl);

        this.currentCompany = trimToNull(this.currentCompany);
        this.currentRole = trimToNull(this.currentRole);
        this.highestEducation = trimToNull(this.highestEducation);

        this.topSkillsJson = trimToNull(this.topSkillsJson);
        this.experienceSummaryJson = trimToNull(this.experienceSummaryJson);
        this.educationSummaryJson = trimToNull(this.educationSummaryJson);

        this.profileSourceType = normalizeProfileSourceType(this.profileSourceType);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase();
    }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            return "USER";
        }

        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return normalized;
    }

    private String normalizeSubDomainMode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value.trim().toLowerCase();

        if ("multiple".equals(normalized)) {
            return "multi";
        }

        if ("single".equals(normalized) || "multi".equals(normalized) || "any".equals(normalized)) {
            return normalized;
        }

        return normalized;
    }

    private String normalizeProfileSourceType(String value) {
        if (value == null || value.isBlank()) {
            return "MANUAL";
        }

        return value.trim().toUpperCase();
    }
}