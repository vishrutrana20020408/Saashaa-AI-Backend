package backend.ai_interview.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resume Version Entity
 *
 * Represents one stored version of a resume
 * in the latest backend-integrated project structure.
 *
 * Examples:
 * - Original uploaded resume
 * - Parsed base resume
 * - Tailored resume for a job application
 *
 * Design:
 * - One Resume can have multiple ResumeVersion records
 * - A version can optionally have a parent version
 * - A version can contain multiple ResumeSection records
 *
 * Latest project update:
 * - Stores parsed profile snapshot for profile sync / navbar profile module
 * - Stores format metadata for resume preview + editor view
 */
@Entity
@SuppressWarnings("all")
@Table(name = "resume_versions")
public class ResumeVersion {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_version_id", nullable = false, updatable = false)
    private Long resumeVersionId;

    /**
     * Unique public/business identifier
     */
    @Column(name = "version_code", nullable = false, unique = true, length = 36)
    private String versionCode;

    /**
     * Human-readable name of this version
     */
    @Column(name = "version_name", nullable = false, length = 255)
    private String versionName;

    /**
     * Type of version
     * Examples: BASE, TAILORED, IMPORTED, GENERATED
     */
    @Column(name = "version_type", nullable = false, length = 50)
    private String versionType;

    /**
     * Marks whether this is the base/original version
     */
    @Column(name = "is_base_version", nullable = false)
    private Boolean baseVersion = Boolean.FALSE;

    /**
     * Stored file URL
     */
    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    /**
     * Preview URL
     */
    @Column(name = "preview_url", length = 1000)
    private String previewUrl;

    /**
     * Optional job application code if this version is tailored
     */
    @Column(name = "job_application_code", length = 255)
    private String jobApplicationCode;

    /**
     * Raw extracted full text from uploaded resume
     */
    @Lob
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    /**
     * Structured parsed content stored as JSON
     */
    @Lob
    @Column(name = "structured_content_json", columnDefinition = "TEXT")
    private String structuredContentJson;

    /**
     * Latest project update:
     * Parsed profile snapshot stored as JSON.
     * Used for:
     * - profile preview
     * - navbar profile sync
     * - user/admin profile generation
     */
    @Lob
    @Column(name = "profile_snapshot_json", columnDefinition = "TEXT")
    private String profileSnapshotJson;

    /**
     * Latest project update:
     * Resume format/layout/template metadata stored as JSON.
     * Used for:
     * - preview page
     * - editor page
     * - file format understanding
     */
    @Lob
    @Column(name = "format_metadata_json", columnDefinition = "TEXT")
    private String formatMetadataJson;

    /**
     * Optional ATS score for this version
     */
    @Column(name = "ats_score")
    private Integer atsScore;

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
     * Parent resume
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "resume_id",
            nullable = false,
            referencedColumnName = "resume_id"
    )
    private Resume resume;

    /**
     * Optional parent version
     * Useful for tracking derived/tailored versions
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_version_id",
            referencedColumnName = "resume_version_id"
    )
    private ResumeVersion parentVersion;

    /**
     * Child sections belonging to this version
     */
    @OneToMany(
            mappedBy = "resumeVersion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ResumeSection> sections = new ArrayList<>();

    public ResumeVersion() {
    }

    /**
     * Compatibility getter so old code using getId() still works.
     */
    public Long getId() {
        return this.resumeVersionId;
    }

    /**
     * Compatibility setter so old code using setId() still works.
     */
    public void setId(Long id) {
        this.resumeVersionId = id;
    }

    /**
     * Auto-fill fields before insert
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

        if (this.baseVersion == null) {
            this.baseVersion = Boolean.FALSE;
        }

        this.versionCode = trimToNull(this.versionCode);
        this.versionName = trimToNull(this.versionName);
        this.versionType = normalizeVersionType(this.versionType);
        this.fileUrl = trimToNull(this.fileUrl);
        this.previewUrl = trimToNull(this.previewUrl);
        this.jobApplicationCode = trimToNull(this.jobApplicationCode);
        this.rawText = trimToNull(this.rawText);
        this.structuredContentJson = trimToNull(this.structuredContentJson);
        this.profileSnapshotJson = trimToNull(this.profileSnapshotJson);
        this.formatMetadataJson = trimToNull(this.formatMetadataJson);

        if (this.sections == null) {
            this.sections = new ArrayList<>();
        }
    }

    /**
     * Auto-update timestamp
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        this.versionCode = trimToNull(this.versionCode);
        this.versionName = trimToNull(this.versionName);
        this.versionType = normalizeVersionType(this.versionType);
        this.fileUrl = trimToNull(this.fileUrl);
        this.previewUrl = trimToNull(this.previewUrl);
        this.jobApplicationCode = trimToNull(this.jobApplicationCode);
        this.rawText = trimToNull(this.rawText);
        this.structuredContentJson = trimToNull(this.structuredContentJson);
        this.profileSnapshotJson = trimToNull(this.profileSnapshotJson);
        this.formatMetadataJson = trimToNull(this.formatMetadataJson);

        if (this.baseVersion == null) {
            this.baseVersion = Boolean.FALSE;
        }

        if (this.sections == null) {
            this.sections = new ArrayList<>();
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeVersionType(String value) {
        if (value == null || value.isBlank()) {
            return "BASE";
        }
        return value.trim().toUpperCase();
    }

    public Long getResumeVersionId() {
        return resumeVersionId;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.resumeVersionId = resumeVersionId;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public Boolean getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(Boolean baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getJobApplicationCode() {
        return jobApplicationCode;
    }

    public void setJobApplicationCode(String jobApplicationCode) {
        this.jobApplicationCode = jobApplicationCode;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getStructuredContentJson() {
        return structuredContentJson;
    }

    public void setStructuredContentJson(String structuredContentJson) {
        this.structuredContentJson = structuredContentJson;
    }

    public String getProfileSnapshotJson() {
        return profileSnapshotJson;
    }

    public void setProfileSnapshotJson(String profileSnapshotJson) {
        this.profileSnapshotJson = profileSnapshotJson;
    }

    public String getFormatMetadataJson() {
        return formatMetadataJson;
    }

    public void setFormatMetadataJson(String formatMetadataJson) {
        this.formatMetadataJson = formatMetadataJson;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public ResumeVersion getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(ResumeVersion parentVersion) {
        this.parentVersion = parentVersion;
    }

    public List<ResumeSection> getSections() {
        return sections;
    }

    public void setSections(List<ResumeSection> sections) {
        this.sections = sections != null ? sections : new ArrayList<>();
    }

    /**
     * Helper method to keep both sides of relation in sync
     */
    public void addSection(ResumeSection section) {
        if (section != null) {
            if (this.sections == null) {
                this.sections = new ArrayList<>();
            }
            if (!this.sections.contains(section)) {
                this.sections.add(section);
            }
            section.setResumeVersion(this);
        }
    }

    /**
     * Helper method to keep both sides of relation in sync
     */
    public void removeSection(ResumeSection section) {
        if (section != null && this.sections != null) {
            this.sections.remove(section);
            section.setResumeVersion(null);
        }
    }
}
