package backend.ai_interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AdminProfile Entity
 *
 * Official database-backed admin profile used in the latest
 * backend-integrated project structure for:
 * - navbar profile summary
 * - admin profile page
 * - resume-to-profile sync flow
 * - manual profile editing
 *
 * Notes:
 * - Linked one-to-one with Admin
 * - Stores official profile data separately from resume version snapshot
 * - Resume-derived data can be synced here and later edited manually
 * - Keeps structure aligned with user/admin profile continuity flows
 */
@Entity
@Table(
        name = "admin_profiles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "admin_id")
        }
)
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfile {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_profile_id", nullable = false)
    private Long adminProfileId;

    /**
     * One profile per admin
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id", nullable = false, unique = true)
    private Admin admin;

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

    /**
     * Stored as JSON string for flexibility
     */
    @Column(name = "top_skills_json", columnDefinition = "TEXT")
    private String topSkillsJson;

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

    @Column(name = "profile_visible_in_dashboard", nullable = false)
    @Builder.Default
    private boolean profileVisibleInDashboard = true;

    @Column(name = "preferred_headline")
    private String preferredHeadline;

    @Column(name = "preferred_location")
    private String preferredLocation;

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

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

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

        if (this.profileSourceType == null || this.profileSourceType.isBlank()) {
            this.profileSourceType = "MANUAL";
        }

        if ((this.fullName == null || this.fullName.isBlank()) && this.admin != null) {
            String first = this.admin.getName() != null ? this.admin.getName().trim() : "";
            String last = this.admin.getSurname() != null ? this.admin.getSurname().trim() : "";
            String combined = (first + " " + last).trim();
            this.fullName = combined.isBlank() ? null : combined;
        }

        if ((this.email == null || this.email.isBlank()) && this.admin != null) {
            this.email = this.admin.getEmailAddress();
        }

        if ((this.phone == null || this.phone.isBlank()) && this.admin != null) {
            this.phone = this.admin.getMobileNumber();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.profileSourceType == null || this.profileSourceType.isBlank()) {
            this.profileSourceType = "MANUAL";
        }

        if ((this.fullName == null || this.fullName.isBlank()) && this.admin != null) {
            String first = this.admin.getName() != null ? this.admin.getName().trim() : "";
            String last = this.admin.getSurname() != null ? this.admin.getSurname().trim() : "";
            String combined = (first + " " + last).trim();
            this.fullName = combined.isBlank() ? null : combined;
        }

        if ((this.email == null || this.email.isBlank()) && this.admin != null) {
            this.email = this.admin.getEmailAddress();
        }

        if ((this.phone == null || this.phone.isBlank()) && this.admin != null) {
            this.phone = this.admin.getMobileNumber();
        }
    }
}