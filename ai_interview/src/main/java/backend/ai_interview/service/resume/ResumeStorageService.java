package backend.ai_interview.service.resume;

import backend.ai_interview.entity.ResumeFileAsset;
import backend.ai_interview.exception.ResumeStorageException;
import backend.ai_interview.repository.ResumeFileAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Resume Storage Service
 *
 * Handles physical file storage for resume-related files in the latest
 * backend-integrated project structure.
 *
 * Responsibilities:
 * - store uploaded original resume files
 * - store generated/tailored resume files
 * - store preview files
 * - store temporary processing files
 * - load stored file paths
 * - delete stored files
 * - persist file metadata in ResumeFileAsset table
 *
 * Supported asset types:
 * - ORIGINAL
 * - GENERATED
 * - PREVIEW
 * - TEMP
 *
 * Latest project alignment:
 * - supports resume upload flow
 * - supports resume version / preview file handling
 * - supports generated tailored resume storage
 * - keeps file metadata consistent with ResumeFileAsset entity updates
 */
@Service
@SuppressWarnings("all")
public class ResumeStorageService {

    private static final String TYPE_ORIGINAL = "ORIGINAL";
    private static final String TYPE_GENERATED = "GENERATED";
    private static final String TYPE_PREVIEW = "PREVIEW";
    private static final String TYPE_TEMP = "TEMP";

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final ResumeFileAssetRepository resumeFileAssetRepository;

    @Value("${app.resume.storage.root:files/resume}")
    private String storageRoot;

    @Value("${app.resume.storage.original-dir:original}")
    private String originalDir;

    @Value("${app.resume.storage.generated-dir:generated}")
    private String generatedDir;

    @Value("${app.resume.storage.preview-dir:preview}")
    private String previewDir;

    @Value("${app.resume.storage.temp-dir:temp}")
    private String tempDir;

    @Value("${app.resume.storage.public-prefix:/files/resume}")
    private String publicPrefix;

    @Value("${app.resume.encryption.key:}")
    private String encryptionKey;

    private final SecureRandom secureRandom = new SecureRandom();

    public ResumeStorageService(ResumeFileAssetRepository resumeFileAssetRepository) {
        this.resumeFileAssetRepository = resumeFileAssetRepository;
    }

    /**
     * Get or generate encryption key for a user
     */
    private SecretKey getUserEncryptionKey(String ownerId) {
        try {
            String keySource = encryptionKey.isEmpty() ?
                "SaaShaaAI2024DefaultKey" + (ownerId != null ? ownerId : "default") :
                encryptionKey + ownerId;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(keySource.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, 0, 32, "AES");
        } catch (Exception ex) {
            throw new ResumeStorageException("Failed to generate encryption key", ex);
        }
    }

    /**
     * Encrypt file content
     */
    private byte[] encryptContent(byte[] content, String ownerId) throws Exception {
        SecretKey key = getUserEncryptionKey(ownerId);
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] encryptedContent = cipher.doFinal(content);

        // Prepend IV to encrypted content
        byte[] result = new byte[GCM_IV_LENGTH + encryptedContent.length];
        System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedContent, 0, result, GCM_IV_LENGTH, encryptedContent.length);

        return result;
    }

    /**
     * Decrypt file content
     */
    private byte[] decryptContent(byte[] encryptedContent, String ownerId) throws Exception {
        if (encryptedContent.length < GCM_IV_LENGTH) {
            throw new ResumeStorageException("Invalid encrypted content");
        }

        SecretKey key = getUserEncryptionKey(ownerId);

        // Extract IV from the beginning
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedContent, 0, iv, 0, GCM_IV_LENGTH);

        // Extract encrypted content
        byte[] content = new byte[encryptedContent.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedContent, GCM_IV_LENGTH, content, 0, content.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return cipher.doFinal(content);
    }

    /**
     * Store an uploaded original resume file.
     */
    @Transactional
    public ResumeFileAsset storeOriginal(MultipartFile file) {
        return storeMultipartFile(null, file, TYPE_ORIGINAL);
    }

    /**
     * Store an uploaded original resume file under a specific owner folder.
     */
    @Transactional
    public ResumeFileAsset storeOriginal(MultipartFile file, String ownerId) {
        return storeMultipartFile(ownerId, file, TYPE_ORIGINAL);
    }

    /**
     * Store an uploaded/generated file under GENERATED asset type.
     */
    @Transactional
    public ResumeFileAsset storeGenerated(MultipartFile file) {
        return storeMultipartFile(null, file, TYPE_GENERATED);
    }

    /**
     * Store an uploaded/generated file under GENERATED asset type for an owner.
     */
    @Transactional
    public ResumeFileAsset storeGenerated(MultipartFile file, String ownerId) {
        return storeMultipartFile(ownerId, file, TYPE_GENERATED);
    }

    /**
     * Store an uploaded/generated file under PREVIEW asset type.
     */
    @Transactional
    public ResumeFileAsset storePreview(MultipartFile file) {
        return storeMultipartFile(null, file, TYPE_PREVIEW);
    }

    @Transactional
    public ResumeFileAsset storePreview(MultipartFile file, String ownerId) {
        return storeMultipartFile(ownerId, file, TYPE_PREVIEW);
    }

    /**
     * Store an uploaded/generated file under TEMP asset type.
     */
    @Transactional
    public ResumeFileAsset storeTemp(MultipartFile file) {
        return storeMultipartFile(null, file, TYPE_TEMP);
    }

    @Transactional
    public ResumeFileAsset storeTemp(MultipartFile file, String ownerId) {
        return storeMultipartFile(ownerId, file, TYPE_TEMP);
    }

    /**
     * Store raw bytes as a file asset.
     */
    @Transactional
    public ResumeFileAsset storeBytes(
            byte[] content,
            String originalFileName,
            String contentType,
            String assetType
    ) {
        return storeBytes(content, originalFileName, contentType, assetType, null);
    }

    /**
     * Store raw bytes as a file asset for a specific owner (with encryption).
     */
    @Transactional
    public ResumeFileAsset storeBytes(
            byte[] content,
            String originalFileName,
            String contentType,
            String assetType,
            String ownerId
    ) {
        validateAssetType(assetType);

        if (content == null || content.length == 0) {
            throw new ResumeStorageException("File content is empty");
        }

        String normalizedAssetType = normalizeAssetType(assetType);
        String safeFileName = sanitizeFileName(originalFileName);
        String storedFileName = buildStoredFileName(safeFileName);
        Path targetDirectory = resolveAssetDirectory(normalizedAssetType, ownerId);
        Path targetFile = targetDirectory.resolve(storedFileName).normalize();

        ensureTargetInsideDirectory(targetDirectory, targetFile);

        try {
            Files.createDirectories(targetDirectory);

            // Encrypt content before storing
            byte[] encryptedContent = encryptContent(content, ownerId);

            Files.write(
                    targetFile,
                    encryptedContent,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            ResumeFileAsset asset = ResumeFileAsset.builder()
                    .fileName(safeFileName)
                    .storedFileName(storedFileName)
                    .filePath(targetFile.toString())
                    .fileUrl(buildPublicUrl(normalizedAssetType, storedFileName, ownerId))
                    .contentType(normalizeContentType(contentType))
                    .fileSize((long) content.length) // Store original size, not encrypted size
                    .checksum(calculateChecksum(content)) // Calculate checksum on original content
                    .assetType(normalizedAssetType)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            try {
                return resumeFileAssetRepository.save(asset);
            } catch (Exception ex) {
                Files.deleteIfExists(targetFile);
                throw new ResumeStorageException("Failed to persist file metadata", ex);
            }

        } catch (Exception ex) {
            throw new ResumeStorageException("Failed to store encrypted file", ex);
        }
    }

    /**
     * Store plain text content as a file.
     */
    public ResumeFileAsset storeText(
            String content,
            String fileName,
            String contentType,
            String assetType
    ) {
        String safeContent = content == null ? "" : content;
        return storeBytes(
                safeContent.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                fileName,
                contentType,
                assetType
        );
    }

    /**
     * Get decrypted file content for an asset
     */
    @Transactional(readOnly = true)
    public byte[] getDecryptedFileContent(Long assetId) {
        ResumeFileAsset asset = getById(assetId);
        Path filePath = getFilePath(assetId);

        if (!Files.exists(filePath)) {
            throw new ResumeStorageException("Stored file not found on disk");
        }

        try {
            byte[] encryptedContent = Files.readAllBytes(filePath);
            String ownerId = getOwnerIdForAsset(asset);
            return decryptContent(encryptedContent, ownerId);
        } catch (Exception ex) {
            throw new ResumeStorageException("Failed to decrypt and read file content", ex);
        }
    }

    /**
     * Get decrypted file content as InputStream for an asset
     */
    @Transactional(readOnly = true)
    public java.io.InputStream getDecryptedFileStream(Long assetId) {
        byte[] decryptedContent = getDecryptedFileContent(assetId);
        return new java.io.ByteArrayInputStream(decryptedContent);
    }

    /**
     * Get decrypted file content by file path and owner ID.
     * Used by services that work with file paths directly.
     */
    public byte[] getDecryptedFileContentByPath(Path filePath, String ownerId) {
        if (!Files.exists(filePath)) {
            throw new ResumeStorageException("File not found on disk: " + filePath);
        }

        try {
            byte[] encryptedContent = Files.readAllBytes(filePath);
            return decryptContent(encryptedContent, ownerId);
        } catch (Exception ex) {
            throw new ResumeStorageException("Failed to decrypt and read file content from path: " + filePath, ex);
        }
    }

    /**
     * Find an asset by id.
     */
    @Transactional(readOnly = true)
    public ResumeFileAsset getById(Long assetId) {
        if (assetId == null) {
            throw new ResumeStorageException("Asset id is required");
        }

        return resumeFileAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResumeStorageException("Stored file asset not found"));
    }

    /**
     * Find an asset by public code.
     */
    @Transactional(readOnly = true)
    public ResumeFileAsset getByAssetCode(String assetCode) {
        if (assetCode == null || assetCode.trim().isEmpty()) {
            throw new ResumeStorageException("Asset code is required");
        }

        return resumeFileAssetRepository.findByAssetCode(assetCode.trim())
                .orElseThrow(() -> new ResumeStorageException("Stored file asset not found"));
    }

    /**
     * Resolve the file path of an asset.
     */
    @Transactional(readOnly = true)
    public Path getFilePath(Long assetId) {
        ResumeFileAsset asset = getById(assetId);

        if (asset.getFilePath() == null || asset.getFilePath().trim().isEmpty()) {
            throw new ResumeStorageException("Stored file path is missing");
        }

        try {
            return Paths.get(asset.getFilePath()).toAbsolutePath().normalize();
        } catch (InvalidPathException ex) {
            throw new ResumeStorageException("Stored file path is invalid", ex);
        }
    }

    /**
     * Check whether an asset file exists on disk.
     */
    @Transactional(readOnly = true)
    public boolean existsOnDisk(Long assetId) {
        return Files.exists(getFilePath(assetId));
    }

    /**
     * Delete file from disk and remove metadata record.
     */
    @Transactional
    public void deleteAsset(Long assetId) {
        ResumeFileAsset asset = getById(assetId);

        try {
            if (asset.getFilePath() != null && !asset.getFilePath().isBlank()) {
                Files.deleteIfExists(Paths.get(asset.getFilePath()).toAbsolutePath().normalize());
            }
            resumeFileAssetRepository.delete(asset);
        } catch (IOException ex) {
            throw new ResumeStorageException("Failed to delete stored file", ex);
        } catch (InvalidPathException ex) {
            throw new ResumeStorageException("Stored file path is invalid", ex);
        }
    }

    /**
     * Delete file from disk only, keep metadata.
     */
    @Transactional
    public void deletePhysicalFile(Long assetId) {
        ResumeFileAsset asset = getById(assetId);

        try {
            if (asset.getFilePath() == null || asset.getFilePath().isBlank()) {
                throw new ResumeStorageException("Stored file path is missing");
            }

            Files.deleteIfExists(Paths.get(asset.getFilePath()).toAbsolutePath().normalize());
        } catch (IOException ex) {
            throw new ResumeStorageException("Failed to delete physical file", ex);
        } catch (InvalidPathException ex) {
            throw new ResumeStorageException("Stored file path is invalid", ex);
        }
    }

    /**
     * Create required root folders if they do not exist.
     */
    public void initializeStorageDirectories() {
        try {
            Files.createDirectories(resolveAssetDirectory(TYPE_ORIGINAL, null));
            Files.createDirectories(resolveAssetDirectory(TYPE_GENERATED, null));
            Files.createDirectories(resolveAssetDirectory(TYPE_PREVIEW, null));
            Files.createDirectories(resolveAssetDirectory(TYPE_TEMP, null));
        } catch (IOException ex) {
            throw new ResumeStorageException("Failed to initialize storage directories", ex);
        }
    }

    private ResumeFileAsset storeMultipartFile(String ownerId, MultipartFile file, String assetType) {
        validateAssetType(assetType);

        if (file == null) {
            throw new ResumeStorageException("File is required");
        }
        if (file.isEmpty()) {
            throw new ResumeStorageException("Uploaded file is empty");
        }

        String normalizedAssetType = normalizeAssetType(assetType);
        String safeFileName = sanitizeFileName(file.getOriginalFilename());
        String storedFileName = buildStoredFileName(safeFileName);
        Path targetDirectory = resolveAssetDirectory(normalizedAssetType, ownerId);
        Path targetFile = targetDirectory.resolve(storedFileName).normalize();

        ensureTargetInsideDirectory(targetDirectory, targetFile);

        try {
            Files.createDirectories(targetDirectory);

            byte[] bytes = file.getBytes();

            // Encrypt content before storing
            byte[] encryptedBytes = encryptContent(bytes, ownerId);

            Files.write(
                    targetFile,
                    encryptedBytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            ResumeFileAsset asset = ResumeFileAsset.builder()
                    .fileName(safeFileName)
                    .storedFileName(storedFileName)
                    .filePath(targetFile.toString())
                    .fileUrl(buildPublicUrl(normalizedAssetType, storedFileName, ownerId))
                    .contentType(normalizeContentType(file.getContentType()))
                    .fileSize((long) bytes.length) // Store original size, not encrypted size
                    .checksum(calculateChecksum(bytes)) // Calculate checksum on original content
                    .assetType(normalizedAssetType)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            try {
                return resumeFileAssetRepository.save(asset);
            } catch (Exception ex) {
                Files.deleteIfExists(targetFile);
                throw new ResumeStorageException("Failed to persist file metadata", ex);
            }

        } catch (Exception ex) {
            throw new ResumeStorageException("Failed to store encrypted uploaded file", ex);
        }
    }

    /**
     * Get the asset directory path for a given type and owner.
     */
    public Path getAssetDirectory(String assetType, String ownerId) {
        return resolveAssetDirectory(assetType, ownerId);
    }

    private Path resolveAssetDirectory(String assetType, String ownerId) {
        String root = safeStorageRoot();
        String ownerSegment = sanitizeOwnerId(ownerId);
        String subDirectory = switch (assetType.toUpperCase()) {
            case TYPE_ORIGINAL -> sanitizeFolderName(originalDir, "original");
            case TYPE_GENERATED -> sanitizeFolderName(generatedDir, "generated");
            case TYPE_PREVIEW -> sanitizeFolderName(previewDir, "preview");
            case TYPE_TEMP -> sanitizeFolderName(tempDir, "temp");
            default -> throw new ResumeStorageException("Unsupported asset type: " + assetType);
        };

        try {
            Path base = ownerSegment.isBlank() ? Paths.get(root) : Paths.get(root, ownerSegment);
            return base.resolve(subDirectory).toAbsolutePath().normalize();
        } catch (InvalidPathException ex) {
            throw new ResumeStorageException("Invalid storage directory configuration", ex);
        }
    }

    private String buildPublicUrl(String assetType, String storedFileName, String ownerId) {
        String ownerSegment = sanitizeOwnerId(ownerId);
        String subDirectory = switch (assetType.toUpperCase()) {
            case TYPE_ORIGINAL -> sanitizeFolderName(originalDir, "original");
            case TYPE_GENERATED -> sanitizeFolderName(generatedDir, "generated");
            case TYPE_PREVIEW -> sanitizeFolderName(previewDir, "preview");
            case TYPE_TEMP -> sanitizeFolderName(tempDir, "temp");
            default -> throw new ResumeStorageException("Unsupported asset type: " + assetType);
        };

        String prefix = (publicPrefix == null || publicPrefix.trim().isEmpty())
                ? "/uploads/resumes"
                : publicPrefix.trim();

        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        if (ownerSegment.isBlank()) {
            return prefix + "/" + subDirectory + "/" + storedFileName;
        }

        return prefix + "/" + ownerSegment + "/" + subDirectory + "/" + storedFileName;
    }

    public void deleteOriginalFilesForOwner(String ownerId) {
        String ownerSegment = sanitizeOwnerId(ownerId);
        Path originalDir = resolveAssetDirectory(TYPE_ORIGINAL, ownerSegment);

        if (!Files.exists(originalDir)) {
            return;
        }

        try (Stream<Path> walked = Files.walk(originalDir)) {
            walked.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new ResumeStorageException("Failed to delete previous original files for owner: " + ownerSegment, ex);
                        }
                    });
        } catch (IOException ex) {
            throw new ResumeStorageException("Failed to delete previous original files for owner: " + ownerSegment, ex);
        }
    }

    public boolean isAssetOwnedBy(Long assetId, String ownerId) {
        if (assetId == null) {
            return false;
        }
        if (ownerId == null) {
            return false;
        }

        ResumeFileAsset asset = getById(assetId);
        String assetOwner = getOwnerIdForAsset(asset);
        return assetOwner != null && !assetOwner.isBlank()
                && assetOwner.equals(sanitizeOwnerId(ownerId));
    }

    public String getOwnerIdForAsset(ResumeFileAsset asset) {
        if (asset == null || asset.getFilePath() == null || asset.getFilePath().isBlank()) {
            return "";
        }

        Path assetPath;
        try {
            assetPath = Paths.get(asset.getFilePath()).toAbsolutePath().normalize();
        } catch (InvalidPathException ex) {
            return "";
        }

        Path rootPath;
        try {
            rootPath = Paths.get(safeStorageRoot()).toAbsolutePath().normalize();
        } catch (InvalidPathException ex) {
            return "";
        }

        if (!assetPath.startsWith(rootPath)) {
            return "";
        }

        Path relativePath = rootPath.relativize(assetPath);
        
        // Expected structure: {role}/{userId}/{assetType}/{fileName}
        // or: {userId}/{assetType}/{fileName}
        
        if (relativePath.getNameCount() < 2) {
            return "";
        }

        String firstSegment = relativePath.getName(0).toString();
        if (firstSegment.equals("users") || firstSegment.equals("admins")) {
            if (relativePath.getNameCount() < 3) {
                return sanitizeOwnerId(firstSegment);
            }
            return sanitizeOwnerId(firstSegment + "/" + relativePath.getName(1).toString());
        }

        return sanitizeOwnerId(firstSegment);
    }

    private String sanitizeOwnerId(String ownerId) {
        if (ownerId == null) {
            return "";
        }

        String safe = ownerId.trim();
        if (safe.isBlank()) {
            return "";
        }

        // Allow / for role-based subfolders (e.g. users/123, admins/456)
        // but still block other dangerous characters and directory traversal
        safe = safe.replaceAll("[\\\\:*?\"<>|]+", "_");
        safe = safe.replaceAll("\\s+", "_");
        
        // Block directory traversal
        if (safe.contains("..")) {
            safe = safe.replace("..", "__");
        }

        if (safe.isBlank()) {
            return "";
        }

        return safe;
    }

    private String sanitizeFileName(String originalFileName) {
        String fallback = "resume-file";
        String candidate = Objects.requireNonNullElse(originalFileName, fallback).trim();

        if (candidate.isBlank()) {
            candidate = fallback;
        }

        try {
            candidate = Paths.get(candidate).getFileName().toString();
        } catch (InvalidPathException ex) {
            candidate = fallback;
        }

        candidate = candidate.replaceAll("[\\\\/:*?\"<>|]+", "_");
        candidate = candidate.replaceAll("\\s+", "_");

        if (candidate.isBlank()) {
            candidate = fallback;
        }

        return candidate;
    }

    private String buildStoredFileName(String originalFileName) {
        String extension = "";
        String baseName = originalFileName;

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            extension = originalFileName.substring(dotIndex);
            baseName = originalFileName.substring(0, dotIndex);
        }

        if (baseName.isBlank()) {
            baseName = "resume-file";
        }

        String uniquePart = UUID.randomUUID().toString().replace("-", "");
        return baseName + "_" + uniquePart + extension;
    }

    private String normalizeContentType(String contentType) {
        return (contentType == null || contentType.trim().isEmpty())
                ? "application/octet-stream"
                : contentType.trim();
    }

    private String normalizeAssetType(String assetType) {
        return assetType == null ? null : assetType.trim().toUpperCase();
    }

    private void validateAssetType(String assetType) {
        if (assetType == null || assetType.trim().isEmpty()) {
            throw new ResumeStorageException("Asset type is required");
        }

        String normalized = assetType.trim().toUpperCase();
        if (!normalized.equals(TYPE_ORIGINAL)
                && !normalized.equals(TYPE_GENERATED)
                && !normalized.equals(TYPE_PREVIEW)
                && !normalized.equals(TYPE_TEMP)) {
            throw new ResumeStorageException("Invalid asset type: " + assetType);
        }
    }

    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new ResumeStorageException("Failed to calculate file checksum", ex);
        }
    }

    private String safeStorageRoot() {
        String root = (storageRoot == null || storageRoot.trim().isEmpty())
                ? "uploads/resumes"
                : storageRoot.trim();

        if (root.contains("\0")) {
            throw new ResumeStorageException("Invalid storage root configuration");
        }

        return root;
    }

    private String sanitizeFolderName(String configuredName, String fallback) {
        String value = (configuredName == null || configuredName.trim().isEmpty())
                ? fallback
                : configuredName.trim();

        value = value.replace("\\", "/");
        value = value.replaceAll("^/+", "");
        value = value.replaceAll("/+$", "");

        if (value.contains("..")) {
            throw new ResumeStorageException("Invalid storage directory configuration");
        }

        if (value.isBlank()) {
            return fallback;
        }

        return value;
    }

    private void ensureTargetInsideDirectory(Path targetDirectory, Path targetFile) {
        if (!targetFile.startsWith(targetDirectory)) {
            throw new ResumeStorageException("Resolved file path is outside the storage directory");
        }
    }
}