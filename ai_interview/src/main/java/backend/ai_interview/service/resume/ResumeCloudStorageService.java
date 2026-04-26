package backend.ai_interview.service.resume;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.ResumeCloudFile;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.ResumeCloudFileRepository;

/**
 * ResumeCloudStorageService
 *
 * Handles encrypted cloud storage operations for resume files
 * Features:
 * - AES-256-GCM encryption/decryption
 * - SHA-256 checksums for integrity verification
 * - File versioning
 * - Owner-based access control
 */
@Service
@SuppressWarnings("all")
public class ResumeCloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(ResumeCloudStorageService.class);

    private final ResumeCloudFileRepository repository;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int AES_KEY_SIZE = 256;

    public ResumeCloudStorageService(ResumeCloudFileRepository repository) {
        this.repository = repository;
    }

    /**
     * Generate encryption key for user
     */
    public SecretKey generateEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            log.error("Error generating encryption key", e);
            throw new ApiException("Failed to generate encryption key");
        }
    }

    /**
     * Encrypt content using AES-256-GCM
     */
    public byte[] encryptContent(byte[] content, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encryptedContent = cipher.doFinal(content);
            byte[] result = new byte[iv.length + encryptedContent.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedContent, 0, result, iv.length, encryptedContent.length);

            return result;
        } catch (Exception e) {
            log.error("Error encrypting content", e);
            throw new ApiException("Failed to encrypt file content");
        }
    }

    /**
     * Decrypt content using AES-256-GCM
     */
    public byte[] decryptContent(byte[] encryptedData, SecretKey key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return cipher.doFinal(encryptedData, GCM_IV_LENGTH, encryptedData.length - GCM_IV_LENGTH);
        } catch (Exception e) {
            log.error("Error decrypting content", e);
            throw new ApiException("Failed to decrypt file content");
        }
    }

    /**
     * Calculate SHA-256 checksum
     */
    public String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calculating checksum", e);
            throw new ApiException("Failed to calculate file checksum");
        }
    }

    /**
     * Upload file to cloud storage
     */
    public ResumeCloudFile uploadFile(MultipartFile file, AppUser owner, SecretKey encryptionKey) {
        try {
            if (file.isEmpty()) {
                throw new ApiException("File is empty");
            }

            byte[] fileContent = file.getBytes();
            String checksum = calculateChecksum(fileContent);
            byte[] encryptedContent = encryptContent(fileContent, encryptionKey);

            ResumeCloudFile cloudFile = new ResumeCloudFile();
            cloudFile.setOwner(owner);
            cloudFile.setFileName(file.getOriginalFilename());
            cloudFile.setFileSize(file.getSize());
            cloudFile.setMimeType(file.getContentType());
            cloudFile.setEncryptedContent(encryptedContent);
            cloudFile.setChecksum(checksum);
            cloudFile.setVersion(1);
            cloudFile.setIsActive(true);

            ResumeCloudFile saved = repository.save(cloudFile);
            log.info("File uploaded to cloud storage: {}", saved.getFileId());

            return saved;
        } catch (IOException e) {
            log.error("Error uploading file", e);
            throw new ApiException("Failed to upload file to cloud storage");
        }
    }

    /**
     * Download file from cloud storage (decrypted)
     */
    public byte[] downloadFile(String fileId, String userId, SecretKey encryptionKey) {
        Optional<ResumeCloudFile> fileOpt = repository.findByFileIdAndOwnerUserId(fileId, userId);
        if (!fileOpt.isPresent()) {
            throw new ApiException("File not found");
        }

        ResumeCloudFile cloudFile = fileOpt.get();
        return decryptContent(cloudFile.getEncryptedContent(), encryptionKey);
    }

    /**
     * Update file content (creates new version)
     */
    public ResumeCloudFile updateFileContent(String fileId, String userId, byte[] newContent, SecretKey encryptionKey) {
        Optional<ResumeCloudFile> fileOpt = repository.findByFileIdAndOwnerUserId(fileId, userId);
        if (!fileOpt.isPresent()) {
            throw new ApiException("File not found");
        }

        ResumeCloudFile cloudFile = fileOpt.get();
        String newChecksum = calculateChecksum(newContent);
        byte[] encryptedContent = encryptContent(newContent, encryptionKey);

        cloudFile.setEncryptedContent(encryptedContent);
        cloudFile.setChecksum(newChecksum);
        cloudFile.setVersion(cloudFile.getVersion() + 1);

        ResumeCloudFile updated = repository.save(cloudFile);
        log.info("File updated: {}", fileId);

        return updated;
    }

    /**
     * Delete file from cloud storage
     */
    public void deleteFile(String fileId, String userId) {
        Optional<ResumeCloudFile> fileOpt = repository.findByFileIdAndOwnerUserId(fileId, userId);
        if (!fileOpt.isPresent()) {
            throw new ApiException("File not found");
        }

        repository.delete(fileOpt.get());
        log.info("File deleted: {}", fileId);
    }

    /**
     * Get file metadata
     */
    public ResumeCloudFile getFileMetadata(String fileId, String userId) {
        Optional<ResumeCloudFile> fileOpt = repository.findByFileIdAndOwnerUserId(fileId, userId);
        if (!fileOpt.isPresent()) {
            throw new ApiException("File not found");
        }

        return fileOpt.get();
    }

    /**
     * List all files for user
     */
    public List<ResumeCloudFile> listFiles(String userId) {
        return repository.findByOwnerUserIdAndIsActiveTrue(userId);
    }

    /**
     * Verify file integrity
     */
    public boolean verifyFileIntegrity(String fileId, String userId, SecretKey encryptionKey) {
        Optional<ResumeCloudFile> fileOpt = repository.findByFileIdAndOwnerUserId(fileId, userId);
        if (!fileOpt.isPresent()) {
            throw new ApiException("File not found");
        }

        ResumeCloudFile cloudFile = fileOpt.get();
        byte[] decryptedContent = decryptContent(cloudFile.getEncryptedContent(), encryptionKey);
        String calculatedChecksum = calculateChecksum(decryptedContent);

        boolean isValid = calculatedChecksum.equals(cloudFile.getChecksum());
        log.info("File integrity check for {}: {}", fileId, isValid ? "VALID" : "INVALID");

        return isValid;
    }

    /**
     * Convert entity to DTO
     */
    public java.util.Map<String, Object> toDto(ResumeCloudFile file) {
        java.util.Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("fileId", file.getFileId());
        dto.put("fileName", file.getFileName());
        dto.put("fileSize", file.getFileSize());
        dto.put("mimeType", file.getMimeType());
        dto.put("checksum", file.getChecksum());
        dto.put("version", file.getVersion());
        dto.put("uploadedAt", file.getUploadedAt());
        dto.put("modifiedAt", file.getModifiedAt());
        return dto;
    }

    /**
     * Convert list of entities to DTOs
     */
    public List<java.util.Map<String, Object>> toDtoList(List<ResumeCloudFile> files) {
        return files.stream().map(this::toDto).collect(Collectors.toList());
    }
}
