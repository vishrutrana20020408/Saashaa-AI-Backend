package backend.ai_interview.util.mapper;

import backend.ai_interview.dto.response.JobApplicationResponse;
import backend.ai_interview.entity.JobApplication;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.entity.ToolRequirementAnswer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Job Application Mapper
 *
 * Maps JobApplication entity to JobApplicationResponse DTO.
 *
 * Latest project alignment:
 * - keeps response compatible with resume tailoring flow
 * - exposes tailored resume/version metadata
 * - includes detected tools / keywords when later populated by service layer
 * - preserves tool-answer mapping for UI rendering
 */
@Component
@SuppressWarnings("all")
public class JobApplicationMapper {

    /**
     * Convert JobApplication entity to JobApplicationResponse DTO.
     */
    public JobApplicationResponse toResponse(JobApplication jobApplication) {
        if (jobApplication == null) {
            return JobApplicationResponse.fail("Job application not found");
        }

        ResumeVersion baseResumeVersion = jobApplication.getBaseResumeVersion();
        ResumeVersion tailoredResumeVersion = jobApplication.getTailoredResumeVersion();

        JobApplicationResponse response = JobApplicationResponse.of(
                jobApplication.getId(),
                jobApplication.getApplicationCode(),
                baseResumeVersion != null ? baseResumeVersion.getResumeVersionId() : null,
                tailoredResumeVersion != null ? tailoredResumeVersion.getResumeVersionId() : null,
                tailoredResumeVersion != null ? tailoredResumeVersion.getVersionCode() : null,
                jobApplication.getCompanyName(),
                jobApplication.getJobTitle(),
                jobApplication.getApplicationSource(),
                jobApplication.getStatus(),
                jobApplication.getAtsScoreBefore(),
                jobApplication.getAtsScoreAfter(),
                null,
                jobApplication.getCreatedAt(),
                jobApplication.getUpdatedAt()
        );

        response.setToolAnswers(mapToolAnswers(jobApplication.getToolRequirementAnswers()));
        response.setTailoredPreviewGenerated(
                tailoredResumeVersion != null && tailoredResumeVersion.getPreviewUrl() != null
                        && !tailoredResumeVersion.getPreviewUrl().trim().isEmpty()
        );

        return response;
    }

    private List<Map<String, Object>> mapToolAnswers(List<ToolRequirementAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (ToolRequirementAnswer answer : answers) {
            if (answer == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("toolName", answer.getToolName());
            item.put("required", Boolean.TRUE.equals(answer.getRequired()));
            item.put("userKnowsTool", Boolean.TRUE.equals(answer.getUserKnowsTool()));
            item.put("userExperienceLevel", answer.getUserExperienceLevel());
            item.put("notes", answer.getNotes());
            item.put("decision", buildDecision(answer));
            result.add(item);
        }

        return result;
    }

    private String buildDecision(ToolRequirementAnswer answer) {
        if (answer == null) {
            return "UNKNOWN";
        }

        if (Boolean.TRUE.equals(answer.getUserKnowsTool())) {
            return "INCLUDE_CONFIDENTLY";
        }

        if (Boolean.TRUE.equals(answer.getRequired())) {
            return "DO_NOT_CLAIM_REQUIRED_TOOL";
        }

        return "OPTIONAL_TOOL_NOT_INCLUDED";
    }
}