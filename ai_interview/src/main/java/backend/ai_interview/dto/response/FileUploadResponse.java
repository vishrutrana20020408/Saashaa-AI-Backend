package backend.ai_interview.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FileUploadResponse
 *
 * Generic response DTO returned after a successful file upload operation.
 *
 * Intended usages:
 * - resume original file upload
 * - generated PDF upload
 * - supporting document upload
 * - future media/file storage responses
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * 1. Backend should preferably expose backend-managed download/preview URLs
 *    instead of raw cloud URLs whenever possible.
 * 2. This DTO is generic so it can be reused across resume and other modules.
 * 3. For resume-specific flows, this response can be embedded inside other DTOs.
 */
@SuppressWarnings("all")
public class FileUploadResponse {

    /**
     * Internal file asset id if stored in database.
     */
    private Long fileId;

    /**
     * Original uploaded filename from client/device.
     */
    private String originalFileName;

    /**
     * Final stored filename used by backend/storage layer.
     */
    private String storedFileName;

    /**
     * File extension.
     * Example:
     * - pdf
     * - docx
     * - png
     */
    private String fileExtension;

    /**
     * MIME/content type.
     * Example:
     * - application/pdf
     * - application/vnd.openxmlformats-officedocument.wordprocessingml.document
     */
    private String contentType;

    /**
     * File size in bytes.
     */
    private Long fileSizeBytes;

    /**
     * Human-readable size string.
     * Example:
     * - 245 KB
     * - 1.7 MB
     */
    private String fileSizeLabel;

    /**
     * Storage provider used.
     * Example:
     * - AWS_S3
     * - LOCAL
     */
    private String storageProvider;

    /**
     * Bucket/container name if relevant.
     */
    private String bucketName;

    /**
     * Internal storage key/path.
     * Example:
     * - resumes/user-12/base/resume-v1.pdf
     */
    private String storageKey;

    /**
     * Backend-managed download URL.
     */
    private String downloadUrl;

    /**
     * Backend-managed preview URL.
     */
    private String previewUrl;

    /**
     * Optional direct file URL if your architecture allows it.
     * Prefer backend proxy/download endpoints where possible.
     */
    private String fileUrl;

    /**
     * Whether upload was completed successfully.
     */
    private Boolean uploaded;

    /**
     * Whether file is publicly accessible.
     */
    private Boolean publicAccess;

    /**
     * Optional checksum/hash for verification.
     */
    private String checksum;

    /**
     * Optional tag/category.
     * Example:
     * - RESUME_ORIGINAL
     * - RESUME_PDF
     * - PROFILE_IMAGE
     */
    private String category;

    /**
     * Optional module/source that triggered the upload.
     * Example:
     * - USER_RESUME
     * - ADMIN_RESUME
     * - RESUME_PDF_GENERATION
     */
    private String sourceModule;

    /**
     * Optional success/info message.
     */
    private String message;

    /**
     * Upload timestamp.
     */
    private LocalDateTime uploadedAt;

    /**
     * Optional warnings encountered during upload.
     */
    private List<String> warnings = new ArrayList<>();

    public FileUploadResponse() {
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Boolean getPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }
}