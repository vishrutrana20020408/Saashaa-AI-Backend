package backend.ai_interview.exception;

/**
 * ResumePdfGenerationException
 *
 * Custom exception for handling failures related to resume PDF generation.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - ResumePdfGenerationService
 * - Resume creation from editor flow
 * - Resume export/download preparation
 * - Resume version PDF regeneration flow
 *
 * -------------------------------------------------------------------------
 * TYPICAL CAUSES
 * -------------------------------------------------------------------------
 * - Invalid structured resume content
 * - Missing required resume sections
 * - Template rendering failure
 * - HTML to PDF conversion failure
 * - Font/resource loading problem
 * - Generated PDF file is empty/corrupted
 *
 * -------------------------------------------------------------------------
 * DESIGN NOTES
 * -------------------------------------------------------------------------
 * - Keeps PDF-generation failures separate from storage and resume-edit logic
 * - Useful for centralized handling in GlobalExceptionHandler
 * - Can be extended later with template identifiers / error codes
 */
@SuppressWarnings("all")
public class ResumePdfGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Optional resume id related to the failure.
     */
    private Long resumeId;

    /**
     * Optional resume version id related to the failure.
     */
    private Long resumeVersionId;

    /**
     * Optional operation name.
     * Example:
     * - GENERATE
     * - REGENERATE
     * - EXPORT
     * - TEMPLATE_RENDER
     */
    private String operation;

    /**
     * Optional template name/id.
     */
    private String template;

    public ResumePdfGenerationException(String message) {
        super(message);
    }

    public ResumePdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResumePdfGenerationException(String message, Long resumeId, Long resumeVersionId, String operation) {
        super(message);
        this.resumeId = resumeId;
        this.resumeVersionId = resumeVersionId;
        this.operation = operation;
    }

    public ResumePdfGenerationException(
            String message,
            Long resumeId,
            Long resumeVersionId,
            String operation,
            String template
    ) {
        super(message);
        this.resumeId = resumeId;
        this.resumeVersionId = resumeVersionId;
        this.operation = operation;
        this.template = template;
    }

    public ResumePdfGenerationException(
            String message,
            Throwable cause,
            Long resumeId,
            Long resumeVersionId,
            String operation,
            String template
    ) {
        super(message, cause);
        this.resumeId = resumeId;
        this.resumeVersionId = resumeVersionId;
        this.operation = operation;
        this.template = template;
    }

    /**
     * Factory: generic generation failure.
     */
    public static ResumePdfGenerationException generationFailed(
            Long resumeId,
            Long resumeVersionId,
            Throwable cause
    ) {
        return new ResumePdfGenerationException(
                "Resume PDF generation failed for resumeId=" + resumeId +
                        ", resumeVersionId=" + resumeVersionId,
                cause,
                resumeId,
                resumeVersionId,
                "GENERATE",
                null
        );
    }

    /**
     * Factory: template rendering failure.
     */
    public static ResumePdfGenerationException templateRenderFailed(
            Long resumeId,
            Long resumeVersionId,
            String template,
            Throwable cause
    ) {
        return new ResumePdfGenerationException(
                "Resume PDF template rendering failed for template=" + template +
                        ", resumeId=" + resumeId +
                        ", resumeVersionId=" + resumeVersionId,
                cause,
                resumeId,
                resumeVersionId,
                "TEMPLATE_RENDER",
                template
        );
    }

    /**
     * Factory: empty/corrupt output.
     */
    public static ResumePdfGenerationException invalidOutput(
            Long resumeId,
            Long resumeVersionId
    ) {
        return new ResumePdfGenerationException(
                "Generated resume PDF is empty or invalid for resumeId=" + resumeId +
                        ", resumeVersionId=" + resumeVersionId,
                resumeId,
                resumeVersionId,
                "GENERATE"
        );
    }

    /**
     * Factory: missing content.
     */
    public static ResumePdfGenerationException missingContent(
            Long resumeId,
            Long resumeVersionId
    ) {
        return new ResumePdfGenerationException(
                "Resume PDF generation failed because structured resume content is missing for resumeId=" + resumeId +
                        ", resumeVersionId=" + resumeVersionId,
                resumeId,
                resumeVersionId,
                "GENERATE"
        );
    }

    /**
     * Factory: export failure.
     */
    public static ResumePdfGenerationException exportFailed(
            Long resumeId,
            Long resumeVersionId,
            Throwable cause
    ) {
        return new ResumePdfGenerationException(
                "Resume PDF export failed for resumeId=" + resumeId +
                        ", resumeVersionId=" + resumeVersionId,
                cause,
                resumeId,
                resumeVersionId,
                "EXPORT",
                null
        );
    }

    public Long getResumeId() {
        return resumeId;
    }

    public Long getResumeVersionId() {
        return resumeVersionId;
    }

    public String getOperation() {
        return operation;
    }

    public String getTemplate() {
        return template;
    }
}