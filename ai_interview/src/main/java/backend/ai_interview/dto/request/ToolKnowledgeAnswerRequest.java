package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Tool Knowledge Answer Request
 *
 * Used in the latest resume tailoring / job application flow when the user
 * answers whether they know a specific tool or technology extracted from
 * a job description.
 *
 * This DTO stays aligned with:
 * - `/api/user/resume/tailor/tool-answers`
 * - resume tailoring / AI-engine processing
 * - job application specific tool knowledge capture
 *
 * Example payload:
 * {
 *   "jobApplicationId": 15,
 *   "toolName": "Docker",
 *   "required": true,
 *   "userKnowsTool": true,
 *   "userExperienceLevel": "INTERMEDIATE",
 *   "notes": "Used in personal projects and internship work"
 * }
 */
@SuppressWarnings("all")
public class ToolKnowledgeAnswerRequest {

    /**
     * Related job application id.
     */
    @NotNull(message = "Job application id is required")
    private Long jobApplicationId;

    /**
     * Tool or technology name.
     */
    @NotBlank(message = "Tool name is required")
    @Size(max = 150, message = "Tool name must not exceed 150 characters")
    private String toolName;

    /**
     * Whether the tool is required for the job.
     */
    @NotNull(message = "Required flag is mandatory")
    private Boolean required;

    /**
     * Whether the user knows this tool.
     */
    @NotNull(message = "User knowledge flag is mandatory")
    private Boolean userKnowsTool;

    /**
     * User's experience level with the tool.
     * Example values: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
     */
    @Size(max = 50, message = "User experience level must not exceed 50 characters")
    private String userExperienceLevel;

    /**
     * Optional notes from user.
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public ToolKnowledgeAnswerRequest() {
    }

    public ToolKnowledgeAnswerRequest(
            Long jobApplicationId,
            String toolName,
            Boolean required,
            Boolean userKnowsTool,
            String userExperienceLevel,
            String notes
    ) {
        this.jobApplicationId = jobApplicationId;
        this.toolName = toolName;
        this.required = required;
        this.userKnowsTool = userKnowsTool;
        this.userExperienceLevel = userExperienceLevel;
        this.notes = notes;
    }

    public Long getJobApplicationId() {
        return jobApplicationId;
    }

    public void setJobApplicationId(Long jobApplicationId) {
        this.jobApplicationId = jobApplicationId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getUserKnowsTool() {
        return userKnowsTool;
    }

    public void setUserKnowsTool(Boolean userKnowsTool) {
        this.userKnowsTool = userKnowsTool;
    }

    public String getUserExperienceLevel() {
        return userExperienceLevel;
    }

    public void setUserExperienceLevel(String userExperienceLevel) {
        this.userExperienceLevel = userExperienceLevel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isRequired() {
        return Boolean.TRUE.equals(required);
    }

    public boolean doesUserKnowTool() {
        return Boolean.TRUE.equals(userKnowsTool);
    }

    @Override
    public String toString() {
        return "ToolKnowledgeAnswerRequest{" +
                "jobApplicationId=" + jobApplicationId +
                ", toolName='" + toolName + '\'' +
                ", required=" + required +
                ", userKnowsTool=" + userKnowsTool +
                ", userExperienceLevel='" + userExperienceLevel + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}