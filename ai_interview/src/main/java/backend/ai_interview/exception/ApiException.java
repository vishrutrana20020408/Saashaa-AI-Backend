package backend.ai_interview.exception;

/**
 * Custom Runtime Exception for API-level errors.
 *
 * Used for:
 * - Invalid login credentials
 * - Duplicate email registration
 * - Business validation failures
 * - Resource not found errors
 */
@SuppressWarnings("all")
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with error message
     *
     * @param message error description
     */
    public ApiException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause
     *
     * @param message error description
     * @param cause   underlying exception
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}