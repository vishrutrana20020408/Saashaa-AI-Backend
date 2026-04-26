package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resume Upload Request
 *
 * Metadata DTO used with the resume upload flow in the latest
 * backend-integrated project structure.
 *
 * The actual file is typically sent separately as MultipartFile
 * in the controller request.
 *
 * Example use cases:
 * - resume title from frontend
 * - optional description / notes
 * - mark upload as base resume
 * - align uploaded resume with broader resume/version workflows
 */
@SuppressWarnings("all")
public class ResumeUploadRequest {

    /**
     * Display title for the uploaded resume.
     */
    @NotBlank(message = "Resume title is required")
    @Size(max = 150, message = "Resume title must not exceed 150 characters")
    private String title;

    /**
     * Optional description or notes for the uploaded resume.
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Whether the uploaded resume should be treated as the base resume.
     */
    private Boolean baseResume;

    public ResumeUploadRequest() {
        this.baseResume = Boolean.TRUE;
    }

    public ResumeUploadRequest(String title, String description, Boolean baseResume) {
        this.title = title;
        this.description = description;
        this.baseResume = baseResume != null ? baseResume : Boolean.TRUE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getBaseResume() {
        return baseResume;
    }

    public void setBaseResume(Boolean baseResume) {
        this.baseResume = baseResume;
    }

    public boolean isBaseResume() {
        return Boolean.TRUE.equals(baseResume);
    }

    @Override
    public String toString() {
        return "ResumeUploadRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", baseResume=" + baseResume +
                '}';
    }
}