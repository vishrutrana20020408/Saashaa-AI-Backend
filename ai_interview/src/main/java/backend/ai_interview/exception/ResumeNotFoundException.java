package backend.ai_interview.exception;

/**
 * Resume Not Found Exception
 *
 * Thrown when a requested resume cannot be found in the system.
 * This may occur when:
 * - Resume ID does not exist
 * - Resume does not belong to the authenticated user
 * - Resume was deleted or archived
 */
@SuppressWarnings("all")
public class ResumeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResumeNotFoundException(String message) {
        super(message);
    }

    public ResumeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}