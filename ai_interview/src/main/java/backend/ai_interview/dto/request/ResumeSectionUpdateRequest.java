package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Resume Section Update Request
 *
 * Used by the resume editor flow to update a single section
 * of a resume version in the latest backend-integrated project structure.
 *
 * This DTO stays aligned with:
 * - resume version editor endpoints
 * - structured resume editing flows
 * - preview regeneration support through updated section content
 * - broader AI-engine/resume processing compatibility
 *
 * Example:
 * {
 *   "sectionType": "SKILLS",
 *   "sectionTitle": "Technical Skills",
 *   "sectionOrder": 2,
 *   "plainText": "Java, Spring Boot, MySQL, React",
 *   "content": {
 *     "items": ["Java", "Spring Boot", "MySQL", "React"]
 *   }
 * }
 */
@SuppressWarnings("all")
public class ResumeSectionUpdateRequest {

    /**
     * Logical section type such as SUMMARY, SKILLS, EXPERIENCE, PROJECTS, EDUCATION.
     */
    @NotBlank(message = "Section type is required")
    @Size(max = 50, message = "Section type must not exceed 50 characters")
    private String sectionType;

    /**
     * Optional display title for the section.
     */
    @Size(max = 150, message = "Section title must not exceed 150 characters")
    private String sectionTitle;

    /**
     * Optional ordering of the section in the resume layout.
     */
    private Integer sectionOrder;

    /**
     * Optional flattened text form of the section.
     * Useful for preview support, search, or text-based processing.
     */
    @Size(max = 10000, message = "Plain text exceeds allowed size")
    private String plainText;

    /**
     * Structured section content stored as JSON-compatible data.
     */
    @NotNull(message = "Section content is required")
    private Map<String, Object> content;

    public ResumeSectionUpdateRequest() {
    }

    public ResumeSectionUpdateRequest(
            String sectionType,
            String sectionTitle,
            Integer sectionOrder,
            String plainText,
            Map<String, Object> content
    ) {
        this.sectionType = sectionType;
        this.sectionTitle = sectionTitle;
        this.sectionOrder = sectionOrder;
        this.plainText = plainText;
        this.content = content;
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

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ResumeSectionUpdateRequest{" +
                "sectionType='" + sectionType + '\'' +
                ", sectionTitle='" + sectionTitle + '\'' +
                ", sectionOrder=" + sectionOrder +
                ", plainTextLength=" + (plainText != null ? plainText.length() : 0) +
                ", content=" + content +
                '}';
    }
}