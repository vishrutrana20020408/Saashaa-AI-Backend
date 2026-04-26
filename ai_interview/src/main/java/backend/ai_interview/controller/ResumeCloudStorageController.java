package backend.ai_interview.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.ResumeCloudFile;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.resume.ResumeCloudStorageService;

/**
 * ResumeCloudStorageController
 *
 * REST endpoints for encrypted cloud storage of resume files
 * All operations require USER or ADMIN role and are owner-validated
 */
@RestController
@RequestMapping("/api/user/resume/cloud")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@SuppressWarnings("all")
public class ResumeCloudStorageController {

    private static final Logger log = LoggerFactory.getLogger(ResumeCloudStorageController.class);

    private final ResumeCloudStorageService cloudStorageService;
    private final UserRepository userRepository;

    // In-memory encryption keys storage (should be replaced with proper key management)
    private static final Map<String, SecretKey> encryptionKeys = new java.util.concurrent.ConcurrentHashMap<>();

    public ResumeCloudStorageController(
            ResumeCloudStorageService cloudStorageService,
            UserRepository userRepository
    ) {
        this.cloudStorageService = cloudStorageService;
        this.userRepository = userRepository;
    }

    private String requireAuthenticatedPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("User is not authenticated.");
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            throw new ApiException("Authenticated user could not be resolved.");
        }

        return principal.trim();
    }

    private AppUser resolveAuthenticatedUser(Authentication authentication) {
        String principal = requireAuthenticatedPrincipal(authentication);

        AppUser user = userRepository.findByUserId(principal).orElse(null);
        if (user == null) {
            user = userRepository.findByEmailAddress(principal).orElse(null);
        }

        if (user == null) {
            throw new ApiException("Authenticated user could not be resolved from database.");
        }

        return user;
    }

    /**
     * Initialize encryption key for user
     */
    @PostMapping("/init")
    public ResponseEntity<?> initializeEncryption(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            SecretKey key = cloudStorageService.generateEncryptionKey();
            encryptionKeys.put(user.getUserId(), key);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Encryption initialized for cloud storage");
            response.put("data", Map.of("userId", user.getUserId()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initializing encryption", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Upload file to cloud storage
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            SecretKey encryptionKey = encryptionKeys.computeIfAbsent(user.getUserId(),
                    k -> cloudStorageService.generateEncryptionKey());

            ResumeCloudFile cloudFile = cloudStorageService.uploadFile(file, user, encryptionKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded to cloud storage successfully");
            response.put("data", cloudStorageService.toDto(cloudFile));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading file", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Download file from cloud storage
     */
    @GetMapping("/file/{fileId}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable String fileId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            SecretKey encryptionKey = encryptionKeys.get(user.getUserId());
            if (encryptionKey == null) {
                encryptionKey = cloudStorageService.generateEncryptionKey();
                encryptionKeys.put(user.getUserId(), encryptionKey);
            }

            byte[] decryptedContent = cloudStorageService.downloadFile(fileId, user.getUserId(), encryptionKey);
            ResumeCloudFile metadata = cloudStorageService.getFileMetadata(fileId, user.getUserId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, metadata.getMimeType() != null ? metadata.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(decryptedContent);
        } catch (Exception e) {
            log.error("Error downloading file", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get file metadata
     */
    @GetMapping("/file/{fileId}/metadata")
    public ResponseEntity<?> getFileMetadata(
            @PathVariable String fileId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeCloudFile file = cloudStorageService.getFileMetadata(fileId, user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File metadata retrieved successfully");
            response.put("data", cloudStorageService.toDto(file));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving file metadata", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Update file content (text-based)
     */
    @PutMapping("/file/{fileId}/update-text")
    public ResponseEntity<?> updateFileContent(
            @PathVariable String fileId,
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            SecretKey encryptionKey = encryptionKeys.get(user.getUserId());
            if (encryptionKey == null) {
                encryptionKey = cloudStorageService.generateEncryptionKey();
                encryptionKeys.put(user.getUserId(), encryptionKey);
            }

            String newContent = payload.get("textContent");
            if (newContent == null || newContent.trim().isEmpty()) {
                throw new ApiException("Content cannot be empty");
            }

            ResumeCloudFile updatedFile = cloudStorageService.updateFileContent(
                    fileId,
                    user.getUserId(),
                    newContent.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    encryptionKey
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File content updated successfully");
            response.put("data", cloudStorageService.toDto(updatedFile));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating file content", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Update file (replace with new file)
     */
    @PutMapping("/file/{fileId}/update")
    public ResponseEntity<?> updateFile(
            @PathVariable String fileId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);

            cloudStorageService.deleteFile(fileId, user.getUserId());

            SecretKey encryptionKey = encryptionKeys.get(user.getUserId());
            if (encryptionKey == null) {
                encryptionKey = cloudStorageService.generateEncryptionKey();
                encryptionKeys.put(user.getUserId(), encryptionKey);
            }

            ResumeCloudFile newFile = cloudStorageService.uploadFile(file, user, encryptionKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File updated successfully");
            response.put("data", cloudStorageService.toDto(newFile));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating file", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete file from cloud storage
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            cloudStorageService.deleteFile(fileId, user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting file", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * List all files for authenticated user
     */
    @GetMapping("/files")
    public ResponseEntity<?> listFiles(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            List<ResumeCloudFile> files = cloudStorageService.listFiles(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Files retrieved successfully");
            response.put("data", cloudStorageService.toDtoList(files));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing files", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Verify file integrity
     */
    @GetMapping("/file/{fileId}/verify")
    public ResponseEntity<?> verifyFileIntegrity(
            @PathVariable String fileId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            SecretKey encryptionKey = encryptionKeys.get(user.getUserId());
            if (encryptionKey == null) {
                encryptionKey = cloudStorageService.generateEncryptionKey();
                encryptionKeys.put(user.getUserId(), encryptionKey);
            }

            boolean isValid = cloudStorageService.verifyFileIntegrity(fileId, user.getUserId(), encryptionKey);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isValid ? "File integrity verified" : "File integrity check failed");
            response.put("data", Map.of("isValid", isValid, "fileId", fileId));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying file integrity", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
