package backend.ai_interview.exception;

/**
 * File Generation Exception
 *
 * Thrown when the backend fails to generate a file related to resumes.
 *
 * Possible cases:
 * - Failed to generate PDF resume
 * - Failed to generate preview document
 * - Template rendering failure
 * - File export process failure
 */
@SuppressWarnings("all")
public class FileGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileGenerationException(String message) {
        super(message);
    }

    public FileGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}