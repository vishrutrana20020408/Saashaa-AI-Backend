package backend.ai_interview.service.storage;

import backend.ai_interview.dto.response.FileDownloadResponse;
import backend.ai_interview.dto.response.FileUploadResponse;
import backend.ai_interview.exception.StorageOperationException;
import backend.ai_interview.service.resume.ResumeStorageService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * ResumeFileStorageService
 *
 * Resume-specific storage orchestration service built on top of ResumeStorageService.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - store uploaded original resume files
 * - store generated resume PDFs
 * - build storage paths/keys for resume assets
 * - provide download metadata
 * - delete stored resume assets
 *
 * -------------------------------------------------------------------------
 * IMPORTANT NOTES
 * -------------------------------------------------------------------------
 * 1. This service is resume-aware, unlike ResumeStorageService which is generic.
 * 2. This service does NOT contain storage credentials.
 * 3. Frontend should call backend endpoints only.
 * 4. Backend should use this service to manage resume file lifecycle.
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later integrate:
 * - ResumeFileAsset entity persistence
 * - database-backed file metadata
 * - versioned overwrite handling
 * - signed/private URL flow
 */
@Service
@SuppressWarnings("all")
public class ResumeFileStorageService {

    private static final String STORAGE_PROVIDER = "LOCAL_FILESYSTEM";
    private static final String ORIGINAL_CATEGORY = "RESUME_ORIGINAL";
    private static final String PDF_CATEGORY = "RESUME_PDF";
    private static final String SOURCE_MODULE = "USER_RESUME";

    private final ResumeStorageService resumeStorageService;

    public ResumeFileStorageService(ResumeStorageService resumeStorageService) {
        this.resumeStorageService = resumeStorageService;
    }

    /**
     * Store original uploaded resume file from multipart request.
     *
     * @param multipartFile uploaded file
     * @param userId user id
     * @param resumeId resume id
     * @param resumeVersionId resume version id
     * @return upload metadata
     */
    public FileUploadResponse storeOriginalResume(
            MultipartFile multipartFile,
            Long userId,
            Long resumeId,
            Long resumeVersionId
    ) {
        validateMultipartFile(multipartFile);

        try {
            String originalFileName = safeFileName(multipartFile.getOriginalFilename());
            String contentType = safeContentType(multipartFile.getContentType(), originalFileName);
            String storageKey = buildOriginalResumeKey(userId, resumeId, resumeVersionId, originalFileName);

            // Store using local ResumeStorageService
            backend.ai_interview.entity.ResumeFileAsset asset = resumeStorageService.storeOriginal(multipartFile, String.valueOf(userId));

            return buildUploadResponse(
                    originalFileName,
                    asset.getStoredFileName(),
                    extensionOf(originalFileName),
                    contentType,
                    asset.getFileSize(),
                    storageKey,
                    asset.getFileUrl(),
                    null,
                    ORIGINAL_CATEGORY,
                    SOURCE_MODULE,
                    "Original resume uploaded successfully"
            );
        } catch (Exception ex) {
            throw new StorageOperationException("Failed to store original resume file", ex);
        }
    }

    /**
     * Store generated resume PDF from bytes.
     *
     * @param pdfBytes generated pdf bytes
     * @param fileName final file name
     * @param userId user id
     * @param resumeId resume id
     * @param resumeVersionId resume version id
     * @return upload metadata
     */
    public FileUploadResponse storeGeneratedResumePdf(
            byte[] pdfBytes,
            String fileName,
            Long userId,
            Long resumeId,
            Long resumeVersionId
    ) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new StorageOperationException("Generated PDF bytes are empty");
        }

        String safeFileName = ensurePdfExtension(safeFileName(fileName));
        String contentType = MediaType.APPLICATION_PDF_VALUE;
        String storageKey = buildGeneratedPdfKey(userId, resumeId, resumeVersionId, safeFileName);

        try {
            // Store using local ResumeStorageService
            backend.ai_interview.entity.ResumeFileAsset asset = resumeStorageService.storeBytes(pdfBytes, safeFileName, contentType, "GENERATED", String.valueOf(userId));

            return buildUploadResponse(
                    safeFileName,
                    asset.getStoredFileName(),
                    "pdf",
                    contentType,
                    asset.getFileSize(),
                    storageKey,
                    asset.getFileUrl(),
                    asset.getFileUrl(),
                    PDF_CATEGORY,
                    SOURCE_MODULE,
                    "Generated resume PDF uploaded successfully"
            );
        } catch (Exception ex) {
            throw new StorageOperationException("Failed to store generated resume PDF", ex);
        }
    }

    /**
     * Store generated resume PDF from temporary file path.
     *
     * @param pdfPath temp pdf file path
     * @param fileName final file name
     * @param userId user id
     * @param resumeId resume id
     * @param resumeVersionId resume version id
     * @return upload metadata
     */
    public FileUploadResponse storeGeneratedResumePdf(
            Path pdfPath,
            String fileName,
            Long userId,
            Long resumeId,
            Long resumeVersionId
    ) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(pdfPath);
            return storeGeneratedResumePdf(bytes, fileName, userId, resumeId, resumeVersionId);
        } catch (IOException ex) {
            throw new StorageOperationException("Failed to read generated PDF file from path: " + pdfPath, ex);
        }
    }

    /**
     * Build download response metadata for a stored file.
     *
     * @param storageKey storage key
     * @param fileName file name
     * @param category category
     * @return download response metadata
     */
    public FileDownloadResponse buildDownloadResponse(
            String storageKey,
            String fileName,
            String category
    ) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new StorageOperationException("Storage key is required for download response");
        }

        String safeName = safeFileName(fileName);
        String contentType = safeContentType(null, safeName);
        // For local storage, construct download URL from storage key
        // Note: storage key uses "resumes/" prefix but actual folder is "files/resume/"
        String directUrl = "/files/resume/" + storageKey.replace("resumes/", "");

        FileDownloadResponse response = new FileDownloadResponse();
        response.setFileName(safeName);
        response.setOriginalFileName(safeName);
        response.setContentType(contentType);
        response.setStorageProvider(STORAGE_PROVIDER);
        response.setStorageKey(storageKey);
        response.setDownloadUrl(directUrl);
        response.setPreviewUrl(isPreviewable(contentType) ? directUrl : null);
        response.setPresignedUrl(null); // Not needed for local storage
        response.setDownloadable(Boolean.TRUE);
        response.setPreviewable(isPreviewable(contentType));
        response.setCategory(category);
        response.setSourceModule(SOURCE_MODULE);
        response.setRequestedAt(LocalDateTime.now());
        response.setMessage("Resume file download metadata prepared successfully");
        return response;
    }

    /**
     * Delete a stored resume file by storage key.
     *
     * @param storageKey storage key
     */
    public void deleteResumeFile(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new StorageOperationException("Storage key is required for deletion");
        }
        // For local storage, construct file path from storage key
        String relativePath = storageKey.replace("resumes/", "");
        // Assume format: user-{userId}/resume-{resumeId}/version-{resumeVersionId}/{type}/{filename}
        String[] parts = relativePath.split("/");
        if (parts.length >= 5) {
            String userId = parts[0].replace("user-", "");
            String type = parts[3];
            String filename = parts[4];
            Path assetDir = resumeStorageService.getAssetDirectory(type.toUpperCase(), userId);
            Path filePath = assetDir.resolve(filename);
            try {
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (IOException ex) {
                throw new StorageOperationException("Failed to delete file: " + storageKey, ex);
            }
        } else {
            throw new StorageOperationException("Invalid storage key format: " + storageKey);
        }
    }

    /**
     * Check if a resume file exists.
     *
     * @param storageKey storage key
     * @return true if exists
     */
    public boolean exists(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return false;
        }
        // For local storage, check if file exists
        String relativePath = storageKey.replace("resumes/", "");
        String[] parts = relativePath.split("/");
        if (parts.length >= 5) {
            String userId = parts[0].replace("user-", "");
            String type = parts[3];
            String filename = parts[4];
            Path assetDir = resumeStorageService.getAssetDirectory(type.toUpperCase(), userId);
            Path filePath = assetDir.resolve(filename);
            return java.nio.file.Files.exists(filePath);
        }
        return false;
    }

    /**
     * Download file bytes from storage.
     *
     * @param storageKey storage key
     * @return file bytes
     */
    public byte[] downloadResumeFile(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new StorageOperationException("Storage key is required for download");
        }
        // For local storage, read file bytes with decryption
        String relativePath = storageKey.replace("resumes/", "");
        String[] parts = relativePath.split("/");
        if (parts.length >= 5) {
            String userId = parts[0].replace("user-", "");
            String type = parts[3];
            String filename = parts[4];
            Path assetDir = resumeStorageService.getAssetDirectory(type.toUpperCase(), userId);
            Path filePath = assetDir.resolve(filename);
            try {
                return resumeStorageService.getDecryptedFileContentByPath(filePath, userId);
            } catch (Exception ex) {
                throw new StorageOperationException("Failed to read and decrypt file: " + storageKey, ex);
            }
        } else {
            throw new StorageOperationException("Invalid storage key format: " + storageKey);
        }
    }

    /**
     * Build structured storage key for original uploaded resume.
     */
    public String buildOriginalResumeKey(
            Long userId,
            Long resumeId,
            Long resumeVersionId,
            String originalFileName
    ) {
        String safeFileName = safeFileName(originalFileName);
        return "resumes/user-" + safeId(userId)
                + "/resume-" + safeId(resumeId)
                + "/version-" + safeId(resumeVersionId)
                + "/original/" + safeFileName;
    }

    /**
     * Build structured storage key for generated resume PDF.
     */
    public String buildGeneratedPdfKey(
            Long userId,
            Long resumeId,
            Long resumeVersionId,
            String fileName
    ) {
        String safeFileName = ensurePdfExtension(safeFileName(fileName));
        return "resumes/user-" + safeId(userId)
                + "/resume-" + safeId(resumeId)
                + "/version-" + safeId(resumeVersionId)
                + "/generated/" + safeFileName;
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private FileUploadResponse buildUploadResponse(
            String originalFileName,
            String storedFileName,
            String extension,
            String contentType,
            Long sizeBytes,
            String storageKey,
            String fileUrl,
            String previewUrl,
            String category,
            String sourceModule,
            String message
    ) {
        FileUploadResponse response = new FileUploadResponse();
        response.setOriginalFileName(originalFileName);
        response.setStoredFileName(storedFileName);
        response.setFileExtension(extension);
        response.setContentType(contentType);
        response.setFileSizeBytes(sizeBytes);
        response.setFileSizeLabel(humanReadableSize(sizeBytes));
        response.setStorageProvider(STORAGE_PROVIDER);
        response.setStorageKey(storageKey);
        response.setDownloadUrl(fileUrl);
        response.setPreviewUrl(previewUrl);
        response.setFileUrl(fileUrl);
        response.setUploaded(Boolean.TRUE);
        response.setPublicAccess(Boolean.FALSE); // safer default
        response.setCategory(category);
        response.setSourceModule(sourceModule);
        response.setUploadedAt(LocalDateTime.now());
        response.setMessage(message);
        return response;
    }

    private void validateMultipartFile(MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new StorageOperationException("Uploaded file must not be null");
        }
        if (multipartFile.isEmpty()) {
            throw new StorageOperationException("Uploaded file is empty");
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "resume-file";
        }

        String normalized = fileName.trim()
                .replace("\\", "/");

        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }

        normalized = normalized
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_");

        return normalized.isBlank() ? "resume-file" : normalized;
    }

    private String ensurePdfExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "resume.pdf";
        }
        return fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")
                ? fileName
                : fileName + ".pdf";
    }

    private String extensionOf(String fileName) {
        if (fileName == null || fileName.isBlank() || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String safeContentType(String declaredContentType, String fileName) {
        if (declaredContentType != null && !declaredContentType.isBlank()) {
            return declaredContentType;
        }

        String extension = extensionOf(fileName);
        return switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt" -> MediaType.TEXT_PLAIN_VALUE;
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    private boolean isPreviewable(String contentType) {
        return MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)
                || MediaType.TEXT_PLAIN_VALUE.equalsIgnoreCase(contentType);
    }

    private String safeId(Long value) {
        return value == null ? "unknown" : String.valueOf(value);
    }

    private String humanReadableSize(Long sizeBytes) {
        if (sizeBytes == null || sizeBytes < 0) {
            return null;
        }
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        double kb = sizeBytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.US, "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format(Locale.US, "%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format(Locale.US, "%.1f GB", gb);
    }
}