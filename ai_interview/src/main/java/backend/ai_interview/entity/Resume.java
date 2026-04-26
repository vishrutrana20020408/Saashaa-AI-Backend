package backend.ai_interview.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Resume Entity
 *
 * Represents the main resume uploaded by a user
 * in the latest backend-integrated project structure.
 *
 * Design:
 * - This is the root resume record
 * - ResumeVersion stores editable / tailored / duplicate versions
 * - Original uploaded file should never be modified
 *
 * Relationships:
 * AppUser (1) -> (N) Resume
 * Resume  (1) -> (N) ResumeVersion
 */
@Entity
@SuppressWarnings("all")
@Table(name = "resumes")
public class Resume {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id", nullable = false, updatable = false)
    private Long resumeId;

    /**
     * Public unique resume identifier
     */
    @Column(name = "resume_code", nullable = false, length = 36)
    private String resumeCode;

    /**
     * Owner of the resume
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private AppUser user;

    /**
     * Resume title
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Optional description
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Status:
     * ACTIVE | ARCHIVED | DELETED
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * Original uploaded file name
     */
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    /**
     * Original file storage URL/path
     */
    @Column(name = "original_file_url", length = 1000)
    private String originalFileUrl;

    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setOriginalFileUrl(String originalFileUrl) {
        this.originalFileUrl = originalFileUrl;
    }

    public Long getResumeId() {
        return this.resumeId;
    }

    public String getResumeCode() {
        return this.resumeCode;
    }

    public AppUser getUser() {
        return this.user;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStatus() {
        return this.status;
    }

    public String getOriginalFileName() {
        return this.originalFileName;
    }

    public String getOriginalFileUrl() {
        return this.originalFileUrl;
    }

    public Integer getTotalVersions() {
        return this.totalVersions;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public String getCurrentBaseVersionCode() {
        return this.currentBaseVersionCode;
    }

    public void setTotalVersions(Integer totalVersions) {
        this.totalVersions = totalVersions;
    }

    /**
     * Code of the current base editable version
     */
    @Column(name = "current_base_version_code", length = 36)
    private String currentBaseVersionCode;

    /**
     * Number of versions created
     */
    @Column(name = "total_versions", nullable = false)
    private Integer totalVersions = 1;

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
     * Compatibility getter so existing code using getId() still works.
     */
    public Long getId() {
        return this.resumeId;
    }

    /**
     * Compatibility setter so existing code using setId() still works.
     */
    public void setId(Long id) {
        this.resumeId = id;
    }

    /**
     * Auto-fill fields before insert
     */
    @PrePersist
    public void prePersist() {
        if (this.resumeCode == null || this.resumeCode.isBlank()) {
            this.resumeCode = UUID.randomUUID().toString();
        }

        this.title = trimToNull(this.title);
        this.description = trimToNull(this.description);
        this.originalFileName = trimToNull(this.originalFileName);
        this.originalFileUrl = trimToNull(this.originalFileUrl);
        this.currentBaseVersionCode = trimToNull(this.currentBaseVersionCode);

        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        } else {
            this.status = this.status.trim().toUpperCase();
        }

        if (this.totalVersions == null || this.totalVersions < 1) {
            this.totalVersions = 1;
        }

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
        this.title = trimToNull(this.title);
        this.description = trimToNull(this.description);
        this.originalFileName = trimToNull(this.originalFileName);
        this.originalFileUrl = trimToNull(this.originalFileUrl);
        this.currentBaseVersionCode = trimToNull(this.currentBaseVersionCode);

        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        } else {
            this.status = this.status.trim().toUpperCase();
        }

        if (this.totalVersions == null || this.totalVersions < 1) {
            this.totalVersions = 1;
        }

        this.updatedAt = LocalDateTime.now();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}