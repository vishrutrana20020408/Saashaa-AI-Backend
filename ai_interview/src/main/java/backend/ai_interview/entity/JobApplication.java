package backend.ai_interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Job Application Entity
 *
 * Represents a job application submitted by a user
 * in the latest backend-integrated project structure.
 *
 * Design:
 * - Each application references a base resume version
 * - A tailored resume version may also be linked
 * - The original/base resume must never be modified
 *
 * Relationships:
 * AppUser       (1) -> (N) JobApplication
 * ResumeVersion (1) -> (N) JobApplication (base version)
 * ResumeVersion (1) -> (N) JobApplication (tailored version)
 */
@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "application_code")
        }
)
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    /**
     * Primary Key
     *
     * Kept as "id" to stay compatible with the current table structure.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Public unique identifier
     */
    @Column(name = "application_code", nullable = false, length = 36, unique = true)
    private String applicationCode;

    /**
     * User who applied
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private AppUser user;

    /**
     * Base resume version selected by the user
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "base_resume_version_id",
            referencedColumnName = "resume_version_id"
    )
    private ResumeVersion baseResumeVersion;

    /**
     * Tailored resume version generated for this job
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tailored_resume_version_id",
            referencedColumnName = "resume_version_id"
    )
    private ResumeVersion tailoredResumeVersion;

    /**
     * Company name
     */
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    /**
     * Job title / role
     */
    @Column(name = "job_title", nullable = false, length = 255)
    private String jobTitle;

    /**
     * Application source
     * Example: LinkedIn / Naukri / Company Portal
     */
    @Column(name = "application_source", length = 255)
    private String applicationSource;

    /**
     * Job description used for tailoring
     */
    @Lob
    @Column(name = "job_description", columnDefinition = "LONGTEXT")
    private String jobDescription;

    /**
     * Application status
     * CREATED | TAILORED | APPLIED | INTERVIEW | REJECTED | OFFER
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * ATS score before tailoring
     */
    @Column(name = "ats_score_before")
    private Integer atsScoreBefore;

    /**
     * ATS score after tailoring
     */
    @Column(name = "ats_score_after")
    private Integer atsScoreAfter;

    /**
     * Optional notes from the user
     */
    @Lob
    @Column(name = "notes", columnDefinition = "LONGTEXT")
    private String notes;

    /**
     * Tool answers linked to this application
     */
    @OneToMany(
            mappedBy = "jobApplication",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ToolRequirementAnswer> toolRequirementAnswers = new ArrayList<>();

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Compatibility getter so older code using getApplicationId() will still work.
     */
    public Long getApplicationId() {
        return this.id;
    }

    /**
     * Compatibility setter so older code using setApplicationId() will still work.
     */
    public void setApplicationId(Long applicationId) {
        this.id = applicationId;
    }

    /**
     * Helper to safely add a tool answer and maintain both sides of relation.
     */
    public void addToolRequirementAnswer(ToolRequirementAnswer answer) {
        if (answer == null) {
            return;
        }

        if (this.toolRequirementAnswers == null) {
            this.toolRequirementAnswers = new ArrayList<>();
        }

        if (!this.toolRequirementAnswers.contains(answer)) {
            this.toolRequirementAnswers.add(answer);
        }
        answer.setJobApplication(this);
    }

    /**
     * Helper to safely remove a tool answer and maintain both sides of relation.
     */
    public void removeToolRequirementAnswer(ToolRequirementAnswer answer) {
        if (answer == null || this.toolRequirementAnswers == null) {
            return;
        }

        this.toolRequirementAnswers.remove(answer);
        answer.setJobApplication(null);
    }

    /**
     * Auto-fill fields before insert
     */
    @PrePersist
    public void prePersist() {
        if (this.applicationCode == null || this.applicationCode.isBlank()) {
            this.applicationCode = UUID.randomUUID().toString();
        }

        this.companyName = trimToNull(this.companyName);
        this.jobTitle = trimToNull(this.jobTitle);
        this.applicationSource = trimToNull(this.applicationSource);
        this.jobDescription = trimToNull(this.jobDescription);
        this.notes = trimToNull(this.notes);

        this.status = normalizeStatus(this.status);

        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    /**
     * Auto-update timestamp
     */
    @PreUpdate
    public void preUpdate() {
        this.companyName = trimToNull(this.companyName);
        this.jobTitle = trimToNull(this.jobTitle);
        this.applicationSource = trimToNull(this.applicationSource);
        this.jobDescription = trimToNull(this.jobDescription);
        this.notes = trimToNull(this.notes);

        this.status = normalizeStatus(this.status);

        this.updatedAt = LocalDateTime.now();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "CREATED";
        }
        return value.trim().toUpperCase();
    }
}