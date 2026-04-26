package backend.ai_interview.exception;

/**
 * Job Application Exception
 *
 * Thrown when an error occurs during job application operations.
 *
 * Possible cases:
 * - Creating a job application fails
 * - Invalid job application request data
 * - Application not found
 * - Unauthorized access to another user's application
 * - Resume tailoring failure during application process
 */
@SuppressWarnings("all")
public class JobApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JobApplicationException(String message) {
        super(message);
    }

    public JobApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}