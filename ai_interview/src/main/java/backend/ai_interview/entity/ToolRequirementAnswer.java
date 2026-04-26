package backend.ai_interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tool Requirement Answer Entity
 *
 * Stores the user's response for tools / technologies required by a job
 * in the latest backend-integrated project structure.
 *
 * Design:
 * - Linked to one JobApplication
 * - Helps tailoring logic avoid claiming tools the user does not know
 * - Supports resume tailoring / AI-engine alignment
 * - Can later support ATS optimization and audit trail
 */
@Entity
@SuppressWarnings("all")
@Table(name = "tool_requirement_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRequirementAnswer {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tool_requirement_answer_id", nullable = false, updatable = false)
    private Long toolRequirementAnswerId;

    /**
     * Related job application
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_application_id", nullable = false, referencedColumnName = "id")
    private JobApplication jobApplication;

    /**
     * Tool or technology name
     */
    @Column(name = "tool_name", nullable = false, length = 150)
    private String toolName;

    /**
     * Whether the tool is required by the job description
     */
    @Column(name = "required_flag", nullable = false)
    @Builder.Default
    private Boolean required = false;

    /**
     * Whether the user knows this tool
     */
    @Column(name = "user_knows_tool", nullable = false)
    @Builder.Default
    private Boolean userKnowsTool = false;

    /**
     * User experience level
     * Example: NONE | BEGINNER | INTERMEDIATE | ADVANCED | EXPERT
     */
    @Column(name = "user_experience_level", length = 50)
    private String userExperienceLevel;

    /**
     * Optional notes provided by the user
     */
    @Lob
    @Column(name = "notes", columnDefinition = "LONGTEXT")
    private String notes;

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
     * Compatibility getter so older code using getId() still works.
     */
    public Long getId() {
        return this.toolRequirementAnswerId;
    }

    /**
     * Compatibility setter so older code using setId() still works.
     */
    public void setId(Long id) {
        this.toolRequirementAnswerId = id;
    }

    /**
     * Auto-fill fields before insert
     */
    @PrePersist
    public void prePersist() {
        this.toolName = trimToNull(this.toolName);
        this.notes = trimToNull(this.notes);

        if (this.required == null) {
            this.required = false;
        }

        if (this.userKnowsTool == null) {
            this.userKnowsTool = false;
        }

        if (this.userExperienceLevel == null || this.userExperienceLevel.isBlank()) {
            this.userExperienceLevel = Boolean.TRUE.equals(this.userKnowsTool)
                    ? "INTERMEDIATE"
                    : "NONE";
        } else {
            this.userExperienceLevel = this.userExperienceLevel.trim().toUpperCase();
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
        this.toolName = trimToNull(this.toolName);
        this.notes = trimToNull(this.notes);

        if (this.required == null) {
            this.required = false;
        }

        if (this.userKnowsTool == null) {
            this.userKnowsTool = false;
        }

        if (this.userExperienceLevel == null || this.userExperienceLevel.isBlank()) {
            this.userExperienceLevel = Boolean.TRUE.equals(this.userKnowsTool)
                    ? "INTERMEDIATE"
                    : "NONE";
        } else {
            this.userExperienceLevel = this.userExperienceLevel.trim().toUpperCase();
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