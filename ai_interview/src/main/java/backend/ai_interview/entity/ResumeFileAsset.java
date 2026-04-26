package backend.ai_interview.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resume File Asset Entity
 *
 * Stores physical file metadata for resume-related files
 * in the latest backend-integrated project structure.
 *
 * Examples:
 * - original uploaded resume file
 * - generated tailored resume file
 * - preview file
 *
 * IMPORTANT DESIGN:
 * - Only metadata/path is stored here, not the full file binary
 * - Can be linked later with Resume or ResumeVersion as needed
 * - Supports broader resume/version/preview/tailoring file flows
 */
@Entity
@Table(
        name = "resume_file_assets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "asset_code")
        }
)
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeFileAsset {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_file_asset_id", nullable = false)
    private Long id;

    /**
     * Public unique asset identifier
     */
    @Column(name = "asset_code", nullable = false, length = 36)
    private String assetCode;

    /**
     * Original file name from upload or generated output
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /**
     * Stored file name on server/disk
     */
    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    /**
     * Absolute or relative file path
     */
    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    /**
     * Public/internal file URL for frontend access
     */
    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    public String getFileName() {
        return this.fileName;
    }

    public String getStoredFileName() {
        return this.storedFileName;
    }

    public String getFileUrl() {
        return this.fileUrl;
    }

    /**
     * MIME content type
     * Example: application/pdf
     */
    @Column(name = "content_type", length = 150)
    private String contentType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Optional file checksum/hash for integrity check
     */
    @Column(name = "checksum", length = 255)
    private String checksum;

    /**
     * Asset category/type
     * Example: ORIGINAL | GENERATED | PREVIEW | TEMP
     */
    @Column(name = "asset_type", nullable = false, length = 50)
    private String assetType;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Auto-fill fields before insert
     */
    @PrePersist
    public void prePersist() {
        if (this.assetCode == null || this.assetCode.isBlank()) {
            this.assetCode = UUID.randomUUID().toString();
        }

        this.fileName = trimToNull(this.fileName);
        this.storedFileName = trimToNull(this.storedFileName);
        this.filePath = trimToNull(this.filePath);
        this.fileUrl = trimToNull(this.fileUrl);
        this.contentType = trimToNull(this.contentType);
        this.checksum = trimToNull(this.checksum);

        if (this.assetType == null || this.assetType.isBlank()) {
            this.assetType = "ORIGINAL";
        } else {
            this.assetType = this.assetType.trim().toUpperCase();
        }

        if (this.fileSize == null || this.fileSize < 0) {
            this.fileSize = 0L;
        }

        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    /**
     * Auto-update timestamp
     */
    @PreUpdate
    public void preUpdate() {
        this.fileName = trimToNull(this.fileName);
        this.storedFileName = trimToNull(this.storedFileName);
        this.filePath = trimToNull(this.filePath);
        this.fileUrl = trimToNull(this.fileUrl);
        this.contentType = trimToNull(this.contentType);
        this.checksum = trimToNull(this.checksum);

        if (this.assetType == null || this.assetType.isBlank()) {
            this.assetType = "ORIGINAL";
        } else {
            this.assetType = this.assetType.trim().toUpperCase();
        }

        if (this.fileSize == null || this.fileSize < 0) {
            this.fileSize = 0L;
        }

        this.updatedAt = LocalDateTime.now();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}