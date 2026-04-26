package backend.ai_interview.exception;

/**
 * StorageOperationException
 *
 * Custom exception for handling failures related to file storage operations.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - AWS S3 upload/download/delete flows
 * - Local file storage (if fallback used)
 * - Resume storage and retrieval
 *
 * -------------------------------------------------------------------------
 * TYPICAL CAUSES
 * -------------------------------------------------------------------------
 * - Failed file upload to S3
 * - Failed file download / presigned URL generation
 * - File not found in storage
 * - Permission / credential issues
 * - Network or timeout issues
 *
 * -------------------------------------------------------------------------
 * DESIGN NOTES
 * -------------------------------------------------------------------------
 * - Keeps storage errors separate from business logic errors
 * - Helps in centralized exception handling (GlobalExceptionHandler)
 * - Can be extended later for retry logic / error codes
 */
@SuppressWarnings("all")
public class StorageOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Optional storage provider (AWS_S3, LOCAL, etc.)
     */
    private String provider;

    /**
     * Optional storage key/path involved in operation
     */
    private String storageKey;

    /**
     * Optional operation type
     * Example:
     * - UPLOAD
     * - DOWNLOAD
     * - DELETE
     */
    private String operation;

    public StorageOperationException(String message) {
        super(message);
    }

    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageOperationException(String message, String provider, String operation, String storageKey) {
        super(message);
        this.provider = provider;
        this.operation = operation;
        this.storageKey = storageKey;
    }

    public StorageOperationException(String message, Throwable cause, String provider, String operation, String storageKey) {
        super(message, cause);
        this.provider = provider;
        this.operation = operation;
        this.storageKey = storageKey;
    }

    /**
     * Factory method for upload failure
     */
    public static StorageOperationException uploadFailed(String storageKey, Throwable cause) {
        return new StorageOperationException(
                "Storage upload failed for key: " + storageKey,
                cause,
                "AWS_S3",
                "UPLOAD",
                storageKey
        );
    }

    /**
     * Factory method for download failure
     */
    public static StorageOperationException downloadFailed(String storageKey, Throwable cause) {
        return new StorageOperationException(
                "Storage download failed for key: " + storageKey,
                cause,
                "AWS_S3",
                "DOWNLOAD",
                storageKey
        );
    }

    /**
     * Factory method for delete failure
     */
    public static StorageOperationException deleteFailed(String storageKey, Throwable cause) {
        return new StorageOperationException(
                "Storage delete failed for key: " + storageKey,
                cause,
                "AWS_S3",
                "DELETE",
                storageKey
        );
    }

    /**
     * Factory method for file not found
     */
    public static StorageOperationException fileNotFound(String storageKey) {
        return new StorageOperationException(
                "File not found in storage for key: " + storageKey,
                "AWS_S3",
                "LOOKUP",
                storageKey
        );
    }

    public String getProvider() {
        return provider;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOperation() {
        return operation;
    }
}