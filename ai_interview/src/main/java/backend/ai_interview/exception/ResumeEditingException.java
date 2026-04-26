package backend.ai_interview.exception;

/**
 * Resume Editing Exception
 *
 * Thrown when the backend fails to edit or update
 * a resume version or its sections.
 *
 * Possible causes:
 * - Invalid section data
 * - Failed content update
 * - JSON parsing issues in structured resume content
 * - Unauthorized editing attempt
 */
@SuppressWarnings("all")
public class ResumeEditingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResumeEditingException(String message) {
        super(message);
    }

    public ResumeEditingException(String message, Throwable cause) {
        super(message, cause);
    }
}