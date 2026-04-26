package backend.ai_interview.exception;

/**
 * Exception thrown when a requested resource
 * (User, Admin, etc.) is not found in the database.
 */
@SuppressWarnings("all")
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with custom message
     *
     * @param message error description
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with resource name and identifier
     *
     * Example:
     * new ResourceNotFoundException("User", "userId", "123")
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " not found with " + fieldName + " : " + fieldValue);
    }
}