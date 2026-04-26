package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Resume Tailor Response
 *
 * Used in the latest backend-integrated resume tailoring flow for:
 * - extracted tools / keywords from a job description
 * - tailored resume generation response
 * - tool knowledge submission response
 *
 * This DTO stays aligned with:
 * - `/api/user/resume/tailor/extract-tools`
 * - `/api/user/resume/tailor/apply`
 * - `/api/user/resume/tailor/tool-answers`
 * - AI-engine driven resume tailoring workflow
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("all")
public class ResumeTailorResponse {

    private boolean success;
    private String message;

    private Long resumeVersionId;
    private String companyName;
    private String jobTitle;

    /**
     * Tools detected from the job description.
     */
    private List<String> detectedTools;

    /**
     * Extracted keywords relevant for ATS / tailoring.
     */
    private List<String> keywords;

    /**
     * Recommended skills inferred during the tailoring flow.
     */
    private List<String> recommendedSkills;

    /**
     * Generic flexible payload for tailored/extracted content.
     */
    private Map<String, Object> tailoredContent;

    private Integer atsScoreBefore;
    private Integer atsScoreAfter;

    /**
     * Whether preview was generated for the tailored output.
     */
    private Boolean previewGenerated;

    public ResumeTailorResponse() {
    }

    public ResumeTailorResponse(
            boolean success,
            String message,
            Long resumeVersionId,
            String companyName,
            String jobTitle,
            List<String> detectedTools,
            List<String> keywords,
            List<String> recommendedSkills,
            Map<String, Object> tailoredContent,
            Integer atsScoreBefore,
            Integer atsScoreAfter,
            Boolean previewGenerated
    ) {
        this.success = success;
        this.message = message;
        this.resumeVersionId = resumeVersionId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.detectedTools = detectedTools;
        this.keywords = keywords;
        this.recommendedSkills = recommendedSkills;
        this.tailoredContent = tailoredContent;
        this.atsScoreBefore = atsScoreBefore;
        this.atsScoreAfter = atsScoreAfter;
        this.previewGenerated = previewGenerated;
    }

    public static ResumeTailorResponse success(String message) {
        ResumeTailorResponse response = new ResumeTailorResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static ResumeTailorResponse fail(String message) {
        ResumeTailorResponse response = new ResumeTailorResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static ResumeTailorResponse of(
            Long resumeVersionId,
            String companyName,
            String jobTitle,
            List<String> detectedTools,
            List<String> keywords,
            List<String> recommendedSkills,
            Map<String, Object> tailoredContent,
            Integer atsScoreBefore,
            Integer atsScoreAfter,
            Boolean previewGenerated
    ) {
        return new ResumeTailorResponse(
                true,
                "Resume tailoring completed successfully",
                resumeVersionId,
                companyName,
                jobTitle,
                detectedTools,
                keywords,
                recommendedSkills,
                tailoredContent,
                atsScoreBefore,
                atsScoreAfter,
                previewGenerated
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getResumeVersionId() {
        return resumeVersionId;
    }

    public void setResumeVersionId(Long resumeVersionId) {
        this.resumeVersionId = resumeVersionId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public List<String> getDetectedTools() {
        return detectedTools;
    }

    public void setDetectedTools(List<String> detectedTools) {
        this.detectedTools = detectedTools;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getRecommendedSkills() {
        return recommendedSkills;
    }

    public void setRecommendedSkills(List<String> recommendedSkills) {
        this.recommendedSkills = recommendedSkills;
    }

    public Map<String, Object> getTailoredContent() {
        return tailoredContent;
    }

    public void setTailoredContent(Map<String, Object> tailoredContent) {
        this.tailoredContent = tailoredContent;
    }

    public Integer getAtsScoreBefore() {
        return atsScoreBefore;
    }

    public void setAtsScoreBefore(Integer atsScoreBefore) {
        this.atsScoreBefore = atsScoreBefore;
    }

    public Integer getAtsScoreAfter() {
        return atsScoreAfter;
    }

    public void setAtsScoreAfter(Integer atsScoreAfter) {
        this.atsScoreAfter = atsScoreAfter;
    }

    public Boolean getPreviewGenerated() {
        return previewGenerated;
    }

    public void setPreviewGenerated(Boolean previewGenerated) {
        this.previewGenerated = previewGenerated;
    }

    public boolean isPreviewGenerated() {
        return Boolean.TRUE.equals(previewGenerated);
    }

    @Override
    public String toString() {
        return "ResumeTailorResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", resumeVersionId=" + resumeVersionId +
                ", companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", detectedTools=" + detectedTools +
                ", keywords=" + keywords +
                ", recommendedSkills=" + recommendedSkills +
                ", tailoredContent=" + tailoredContent +
                ", atsScoreBefore=" + atsScoreBefore +
                ", atsScoreAfter=" + atsScoreAfter +
                ", previewGenerated=" + previewGenerated +
                '}';
    }
}