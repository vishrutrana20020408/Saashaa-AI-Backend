package backend.ai_interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * UserProfile Entity
 *
 * Official database-backed user profile used by:
 * - navbar profile summary
 * - user profile page
 * - resume-to-profile sync flow
 * - manual profile editing
 *
 * Notes:
 * - Linked one-to-one with AppUser
 * - Stores official profile data separately from resume version snapshot
 * - Resume-derived data can be synced here and later edited manually
 * - Structure remains aligned with the latest backend-integrated project update
 */
@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        }
)
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    /**
     * One profile per user
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private AppUser user;

    /**
     * Official profile fields
     */
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "headline")
    private String headline;

    @Column(name = "location")
    private String location;

    @Column(name = "linkedin_url", length = 1000)
    private String linkedinUrl;

    @Column(name = "github_url", length = 1000)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 1000)
    private String portfolioUrl;

    @Column(name = "profile_summary", columnDefinition = "TEXT")
    private String profileSummary;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "current_job_role")
    private String currentRole;

    @Column(name = "highest_education")
    private String highestEducation;

    /**
     * Stored as JSON strings for flexibility
     */
    @Column(name = "top_skills_json", columnDefinition = "TEXT")
    private String topSkillsJson;

    @Column(name = "experience_summary_json", columnDefinition = "TEXT")
    private String experienceSummaryJson;

    @Column(name = "education_summary_json", columnDefinition = "TEXT")
    private String educationSummaryJson;

    /**
     * Profile state / sync metadata
     * Example values:
     * - MANUAL
     * - RESUME
     * - MIXED
     */
    @Column(name = "profile_source_type")
    private String profileSourceType;

    /**
     * Resume version that last synced this profile
     */
    @Column(name = "source_resume_version_id")
    private Long sourceResumeVersionId;

    /**
     * Profile preferences
     */
    @Column(name = "auto_sync_from_resume", nullable = false)
    @Builder.Default
    private boolean autoSyncFromResume = false;

    @Column(name = "allow_resume_overwrite", nullable = false)
    @Builder.Default
    private boolean allowResumeOverwrite = true;

    @Column(name = "profile_visible_to_admin", nullable = false)
    @Builder.Default
    private boolean profileVisibleToAdmin = true;

    @Column(name = "profile_visible_in_dashboard", nullable = false)
    @Builder.Default
    private boolean profileVisibleInDashboard = true;

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

    @Column(name = "resume_url", length = 1000)
    private String resumeUrl;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "profile_picture_url", length = 1000)
    private String profilePictureUrl;

    @Column(name = "preferred_headline")
    private String preferredHeadline;

    @Column(name = "preferred_location")
    private String preferredLocation;

    /**
     * Audit fields
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

        normalizeFields();

        if (this.profileSourceType == null || this.profileSourceType.isBlank()) {
            this.profileSourceType = "MANUAL";
        }

        if ((this.fullName == null || this.fullName.isBlank()) && this.user != null) {
            String first = this.user.getName() != null ? this.user.getName().trim() : "";
            String last = this.user.getSurname() != null ? this.user.getSurname().trim() : "";
            String combined = (first + " " + last).trim();
            this.fullName = combined.isBlank() ? null : combined;
        }

        if ((this.email == null || this.email.isBlank()) && this.user != null) {
            this.email = this.user.getEmailAddress();
        }

        if ((this.phone == null || this.phone.isBlank()) && this.user != null) {
            this.phone = this.user.getMobileNumber();
        }
    }

    @PreUpdate
    public void preUpdate() {
        normalizeFields();

        if (this.profileSourceType == null || this.profileSourceType.isBlank()) {
            this.profileSourceType = "MANUAL";
        }

        if ((this.fullName == null || this.fullName.isBlank()) && this.user != null) {
            String first = this.user.getName() != null ? this.user.getName().trim() : "";
            String last = this.user.getSurname() != null ? this.user.getSurname().trim() : "";
            String combined = (first + " " + last).trim();
            this.fullName = combined.isBlank() ? null : combined;
        }

        if ((this.email == null || this.email.isBlank()) && this.user != null) {
            this.email = this.user.getEmailAddress();
        }

        if ((this.phone == null || this.phone.isBlank()) && this.user != null) {
            this.phone = this.user.getMobileNumber();
        }

        this.updatedAt = LocalDateTime.now();
    }

    private void normalizeFields() {
        this.fullName = trimToNull(this.fullName);
        this.email = normalizeEmail(this.email);
        this.phone = trimToNull(this.phone);
        this.headline = trimToNull(this.headline);
        this.location = trimToNull(this.location);

        this.linkedinUrl = trimToNull(this.linkedinUrl);
        this.githubUrl = trimToNull(this.githubUrl);
        this.portfolioUrl = trimToNull(this.portfolioUrl);

        this.profileSummary = trimToNull(this.profileSummary);

        this.currentCompany = trimToNull(this.currentCompany);
        this.currentRole = trimToNull(this.currentRole);
        this.highestEducation = trimToNull(this.highestEducation);

        this.topSkillsJson = trimToNull(this.topSkillsJson);
        this.experienceSummaryJson = trimToNull(this.experienceSummaryJson);
        this.educationSummaryJson = trimToNull(this.educationSummaryJson);

        this.profileSourceType = normalizeProfileSourceType(this.profileSourceType);

        this.preferredHeadline = trimToNull(this.preferredHeadline);
        this.preferredLocation = trimToNull(this.preferredLocation);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String normalizeProfileSourceType(String value) {
        if (value == null || value.isBlank()) {
            return "MANUAL";
        }
        return value.trim().toUpperCase();
    }
}