package backend.ai_interview.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Resume Section Entity
 *
 * Represents one editable section inside a resume version
 * in the latest backend-integrated project structure.
 *
 * Examples:
 * - SUMMARY
 * - SKILLS
 * - EXPERIENCE
 * - PROJECTS
 * - EDUCATION
 * - CERTIFICATIONS
 *
 * Design:
 * - Each ResumeVersion can contain multiple ResumeSection records
 * - Sections are editable independently from the frontend
 * - contentJson stores structured section data
 * - plainText stores searchable / readable text
 * - structure stays aligned with resume editor, preview, and tailoring flows
 */
@Entity
@SuppressWarnings("all")
@Table(name = "resume_sections")
public class ResumeSection {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_section_id", nullable = false, updatable = false)
    private Long resumeSectionId;

    /**
     * Parent resume version
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "resume_version_id",
            nullable = false,
            referencedColumnName = "resume_version_id"
    )
    private ResumeVersion resumeVersion;

    /**
     * Section type:
     * SUMMARY | SKILLS | EXPERIENCE | PROJECTS | EDUCATION | CERTIFICATIONS
     */
    @Column(name = "section_type", nullable = false, length = 50)
    private String sectionType;

    /**
     * Section title shown in frontend editor
     */
    @Column(name = "section_title", length = 150)
    private String sectionTitle;

    /**
     * Order of section in the resume
     */
    @Column(name = "section_order")
    private Integer sectionOrder;

    /**
     * Structured JSON data for this section
     */
    @Lob
    @Column(name = "content_json", columnDefinition = "LONGTEXT")
    private String contentJson;

    /**
     * Plain text representation of the section
     */
    @Lob
    @Column(name = "plain_text", columnDefinition = "LONGTEXT")
    private String plainText;

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

    public ResumeSection() {
    }

    public ResumeSection(Long resumeSectionId, ResumeVersion resumeVersion, String sectionType,
                         String sectionTitle, Integer sectionOrder, String contentJson,
                         String plainText, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.resumeSectionId = resumeSectionId;
        this.resumeVersion = resumeVersion;
        this.sectionType = sectionType;
        this.sectionTitle = sectionTitle;
        this.sectionOrder = sectionOrder;
        this.contentJson = contentJson;
        this.plainText = plainText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Compatibility getter so old code using getId() still works.
     */
    public Long getId() {
        return this.resumeSectionId;
    }

    /**
     * Compatibility setter so old code using setId() still works.
     */
    public void setId(Long id) {
        this.resumeSectionId = id;
    }

    /**
     * Auto-fill fields before insert
     */
    @PrePersist
    public void prePersist() {
        this.sectionType = normalizeSectionType(this.sectionType);
        this.sectionTitle = normalizeSectionTitle(this.sectionTitle, this.sectionType);
        this.sectionOrder = normalizeSectionOrder(this.sectionOrder);
        this.contentJson = trimToNull(this.contentJson);
        this.plainText = trimToNull(this.plainText);

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
        this.sectionType = normalizeSectionType(this.sectionType);
        this.sectionTitle = normalizeSectionTitle(this.sectionTitle, this.sectionType);
        this.sectionOrder = normalizeSectionOrder(this.sectionOrder);
        this.contentJson = trimToNull(this.contentJson);
        this.plainText = trimToNull(this.plainText);

        this.updatedAt = LocalDateTime.now();
    }

    private String normalizeSectionType(String value) {
        if (value == null || value.isBlank()) {
            return "GENERAL";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeSectionTitle(String title, String fallbackType) {
        if (title == null || title.isBlank()) {
            return fallbackType;
        }
        String normalized = title.trim();
        return normalized.isEmpty() ? fallbackType : normalized;
    }

    private Integer normalizeSectionOrder(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getResumeSectionId() {
        return resumeSectionId;
    }

    public void setResumeSectionId(Long resumeSectionId) {
        this.resumeSectionId = resumeSectionId;
    }

    public ResumeVersion getResumeVersion() {
        return resumeVersion;
    }

    public void setResumeVersion(ResumeVersion resumeVersion) {
        this.resumeVersion = resumeVersion;
    }

    public String getSectionType() {
        return sectionType;
    }

    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public Integer getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(Integer sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    public String getContentJson() {
        return contentJson;
    }

    public void setContentJson(String contentJson) {
        this.contentJson = contentJson;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
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
}