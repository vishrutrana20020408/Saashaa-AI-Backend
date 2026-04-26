package backend.ai_interview.exception;

/**
 * Resume Storage Exception
 *
 * Thrown when the backend fails to store, retrieve,
 * or access a resume file in the storage system.
 *
 * Used in the latest backend-integrated project structure for:
 * - resume upload flow
 * - resume file asset handling
 * - resume preview/download file access
 * - generated/tailored resume storage operations
 *
 * Possible causes:
 * - file upload failure
 * - file system permission issues
 * - storage path errors
 * - cloud/object storage failure
 * - generated file persistence failure
 */
@SuppressWarnings("all")
public class ResumeStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResumeStorageException(String message) {
        super(message);
    }

    public ResumeStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}