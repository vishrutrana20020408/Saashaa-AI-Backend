package backend.ai_interview.dto.response;

import java.time.LocalDateTime;

/**
 * FileDownloadResponse
 *
 * Response DTO used when a file is requested for download or preview.
 *
 * Typical usage:
 * - Resume download
 * - Resume preview (PDF)
 * - Any stored file retrieval (S3/local)
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * Frontend → Backend:
 *   GET /api/.../download
 *
 * Backend:
 *   - validates access
 *   - resolves storage key (S3/local)
 *   - optionally generates pre-signed URL
 *   - returns file metadata + download link
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * 1. Prefer backend-managed download URLs over exposing raw S3 URLs
 * 2. Pre-signed URLs can be used for secure direct downloads
 * 3. This DTO does NOT contain raw file bytes (use streaming endpoint for that)
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later add:
 * - expiration timestamps for pre-signed URLs
 * - download tokens
 * - access audit tracking
 */
@SuppressWarnings("all")
public class FileDownloadResponse {

    /**
     * Internal file id if tracked in DB.
     */
    private Long fileId;

    /**
     * File name to be shown to user.
     */
    private String fileName;

    /**
     * Original file name (if different).
     */
    private String originalFileName;

    /**
     * MIME/content type.
     * Example:
     * - application/pdf
     * - application/msword
     */
    private String contentType;

    /**
     * File size in bytes.
     */
    private Long fileSizeBytes;

    /**
     * Human-readable size label.
     */
    private String fileSizeLabel;

    /**
     * Storage provider.
     * Example:
     * - AWS_S3
     * - LOCAL
     */
    private String storageProvider;

    /**
     * Storage key/path.
     */
    private String storageKey;

    /**
     * Backend-managed download URL.
     */
    private String downloadUrl;

    /**
     * Preview URL (for inline viewing, e.g., PDF viewer).
     */
    private String previewUrl;

    /**
     * Optional direct/pre-signed URL.
     */
    private String presignedUrl;

    /**
     * Whether the file is downloadable.
     */
    private Boolean downloadable;

    /**
     * Whether preview is supported.
     */
    private Boolean previewable;

    /**
     * Optional expiration timestamp for pre-signed URL.
     */
    private LocalDateTime expiresAt;

    /**
     * Optional category/type.
     * Example:
     * - RESUME
     * - PROFILE
     */
    private String category;

    /**
     * Optional module/source.
     */
    private String sourceModule;

    /**
     * Optional message for frontend.
     */
    private String message;

    /**
     * Request timestamp.
     */
    private LocalDateTime requestedAt;

    public FileDownloadResponse() {
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getFileSizeLabel() {
        return fileSizeLabel;
    }

    public void setFileSizeLabel(String fileSizeLabel) {
        this.fileSizeLabel = fileSizeLabel;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public Boolean getDownloadable() {
        return downloadable;
    }

    public void setDownloadable(Boolean downloadable) {
        this.downloadable = downloadable;
    }

    public Boolean getPreviewable() {
        return previewable;
    }

    public void setPreviewable(Boolean previewable) {
        this.previewable = previewable;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}