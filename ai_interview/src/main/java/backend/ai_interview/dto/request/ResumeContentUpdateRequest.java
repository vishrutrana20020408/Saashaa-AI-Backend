package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Resume Content Update Request
 *
 * Used by the resume editor flow to update the complete structured
 * content of a resume version in the latest backend-integrated project structure.
 *
 * This DTO stays aligned with:
 * - resume version editor endpoints
 * - preview regeneration flow
 * - AI-engine/resume-tailoring compatibility through structured + raw text content
 *
 * Example structure received from frontend editor:
 *
 * {
 *   "structuredContent": {
 *     "summary": "Backend developer with 3 years experience...",
 *     "skills": ["Java", "Spring Boot", "MySQL"],
 *     "experience": [...],
 *     "projects": [...],
 *     "education": [...]
 *   },
 *   "rawText": "Full plain text resume...",
 *   "regeneratePreview": true
 * }
 */
@SuppressWarnings("all")
public class ResumeContentUpdateRequest {

    /**
     * Structured resume content coming from the frontend editor.
     * Stored as JSON in backend.
     */
    @NotNull(message = "Structured content is required")
    private Map<String, Object> structuredContent;

    /**
     * Optional raw text representation of the resume.
     * Useful for ATS scoring, preview support, or AI processing.
     */
    @Size(max = 20000, message = "Raw text exceeds allowed size")
    private String rawText;

    /**
     * Optional flag to regenerate preview after update.
     */
    private Boolean regeneratePreview;

    public ResumeContentUpdateRequest() {
        this.regeneratePreview = Boolean.TRUE;
    }

    public ResumeContentUpdateRequest(
            Map<String, Object> structuredContent,
            String rawText,
            Boolean regeneratePreview
    ) {
        this.structuredContent = structuredContent;
        this.rawText = rawText;
        this.regeneratePreview = regeneratePreview != null ? regeneratePreview : Boolean.TRUE;
    }

    public Map<String, Object> getStructuredContent() {
        return structuredContent;
    }

    public void setStructuredContent(Map<String, Object> structuredContent) {
        this.structuredContent = structuredContent;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public Boolean getRegeneratePreview() {
        return regeneratePreview;
    }

    public void setRegeneratePreview(Boolean regeneratePreview) {
        this.regeneratePreview = regeneratePreview;
    }

    public boolean shouldRegeneratePreview() {
        return Boolean.TRUE.equals(regeneratePreview);
    }

    @Override
    public String toString() {
        return "ResumeContentUpdateRequest{" +
                "structuredContent=" + structuredContent +
                ", rawTextLength=" + (rawText != null ? rawText.length() : 0) +
                ", regeneratePreview=" + regeneratePreview +
                '}';
    }
}