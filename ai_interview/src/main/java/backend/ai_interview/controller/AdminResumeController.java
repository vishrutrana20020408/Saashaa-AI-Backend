package backend.ai_interview.controller;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.ResumeEditorResponse;
import backend.ai_interview.dto.response.ResumePreviewResponse;
import backend.ai_interview.dto.response.ResumeResponse;
import backend.ai_interview.dto.response.ResumeScanResponse;
import backend.ai_interview.dto.response.ResumeVersionResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeFileAsset;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.resume.ResumeEditorService;
import backend.ai_interview.service.resume.ResumePreviewService;
import backend.ai_interview.service.resume.ResumeService;
import backend.ai_interview.service.resume.ResumeStorageService;
import backend.ai_interview.service.resume.ResumeVersionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Resume Controller
 *
 * Handles admin-side resume inspection and management:
 * - fetch all resumes of a user
 * - fetch one resume
 * - fetch all versions of a resume
 * - fetch one resume version
 * - fetch editor data of a version
 * - fetch preview data of a version
 * - upload resume on behalf of a user
 * - optional health check
 *
 * Endpoints:
 * - GET /api/admin/resume/user/{userId}
 * - GET /api/admin/resume/{resumeId}
 * - GET /api/admin/resume/{resumeId}/versions
 * - GET /api/admin/resume/version/{versionId}
 * - GET /api/admin/resume/version/{versionId}/editor
 * - GET /api/admin/resume/version/{versionId}/preview
 * - POST /api/admin/resume/user/{userId}/upload
 * - GET /api/admin/resume/ping
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/admin/resume")
public class AdminResumeController {

    private static final Logger log = LoggerFactory.getLogger(AdminResumeController.class);

    private final ResumeService resumeService;
    private final ResumeVersionService resumeVersionService;
    private final ResumeEditorService resumeEditorService;
    private final ResumePreviewService resumePreviewService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeStorageService resumeStorageService;

    public AdminResumeController(
            ResumeService resumeService,
            ResumeVersionService resumeVersionService,
            ResumeEditorService resumeEditorService,
            ResumePreviewService resumePreviewService,
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            ResumeStorageService resumeStorageService
    ) {
        this.resumeService = resumeService;
        this.resumeVersionService = resumeVersionService;
        this.resumeEditorService = resumeEditorService;
        this.resumePreviewService = resumePreviewService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.resumeStorageService = resumeStorageService;
    }

    private String requireAuthenticatedPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("User is not authenticated.");
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            throw new ApiException("Authenticated admin could not be resolved.");
        }

        return principal.trim();
    }

    private AppUser resolveTargetUser(String userId) {
        AppUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            user = userRepository.findByEmailAddress(userId).orElse(null);
        }

        if (user == null) {
            throw new ApiException("Target user could not be found: " + userId);
        }

        return user;
    }

    private void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Resume file is required.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ApiException("Invalid file name.");
        }

        String lowerName = originalFilename.toLowerCase();
        boolean supported = lowerName.endsWith(".pdf")
                || lowerName.endsWith(".doc")
                || lowerName.endsWith(".docx")
                || lowerName.endsWith(".txt");

        if (!supported) {
            throw new ApiException("Only PDF, DOC, DOCX, and TXT files are supported.");
        }
    }

    /**
     * Serve stored resume file - admins only
     * GET /api/admin/resume/asset/{assetId}/download
     */
    @GetMapping(
            value = "/asset/{assetId}/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> serveResumeAsset(
            @PathVariable Long assetId,
            Authentication authentication
    ) {
        try {
            requireAuthenticatedPrincipal(authentication);

            ResumeFileAsset asset = resumeStorageService.getById(assetId);
            if (asset == null) {
                return ResponseEntity.notFound().build();
            }

            // Read and decrypt file from disk
            byte[] fileContent = resumeStorageService.getDecryptedFileContent(assetId);
            String filename = asset.getFileName() != null ? asset.getFileName() : "file";

            log.info("Admin downloading asset assetId={}: {}", assetId, filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(fileContent);

        } catch (ApiException ex) {
            log.warn("Admin file download failed for assetId={}: {}", assetId, ex.getMessage());
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error downloading assetId={}", assetId, ex);
            return ResponseEntity.internalServerError().body("Failed to download file");
        }
    }

    /**
     * Admin upload resume on behalf of a user
     * POST /api/admin/resume/user/{userId}/upload
     */
    @PostMapping(
            value = "/user/{userId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumeScanResponse> adminUploadResumeForUser(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            requireAuthenticatedPrincipal(authentication);
            validateResumeFile(file);

            AppUser targetUser = resolveTargetUser(userId);

            // Separate storage for admins and users
            String rolePrefix = "users/";
            if (targetUser.getRole() != null && targetUser.getRole().equalsIgnoreCase("ADMIN")) {
                rolePrefix = "admins/";
            }
            String ownerId = rolePrefix + targetUser.getUserId();

            ResumeFileAsset asset = resumeStorageService.storeOriginal(file, ownerId);

            Resume resume = resumeRepository.findByUser_UserIdOrderByCreatedAtDesc(targetUser.getUserId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (resume == null) {
                resume = new Resume();
                resume.setUser(targetUser);
                resume.setTitle("Resume");
                resume.setStatus("ACTIVE");
                resume.setOriginalFileName(asset.getFileName());
                resume.setOriginalFileUrl(asset.getFileUrl());
                resume.setTotalVersions(1);
            } else {
                resume.setOriginalFileName(asset.getFileName());
                resume.setOriginalFileUrl(asset.getFileUrl());
                resume.setStatus("ACTIVE");
            }

            resumeRepository.save(resume);

            Map<String, Object> payload = new HashMap<>();
            payload.put("fileUrl", asset.getFileUrl());
            payload.put("storedFileName", asset.getStoredFileName());
            payload.put("originalFileName", asset.getFileName());

            log.info("Admin uploaded resume for userId={}: {}", targetUser.getUserId(), asset.getFileName());

            return ResponseEntity.ok(
                    new ResumeScanResponse(
                            true,
                            "Resume uploaded locally for user.",
                            payload,
                            null
                    )
            );
        } catch (ApiException ex) {
            log.warn("Admin upload failed for userId={}: {}", userId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResumeScanResponse(false, ex.getMessage(), null, null));
        } catch (Exception ex) {
            log.error("Unexpected error during admin resume upload for userId={}", userId, ex);
            return ResponseEntity.internalServerError()
                    .body(new ResumeScanResponse(
                            false,
                            "Resume upload failed. Please try again.",
                            null,
                            null
                    ));
        }
    }

    /**
     * Fetch all resumes of a specific user
     * GET /api/admin/resume/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getUserResumes(@PathVariable String userId) {
        try {
            log.debug("Fetching resumes for userId={}", userId);

            List<ResumeResponse> resumes = resumeService.getResumesByUser(userId);
            return ResponseEntity.ok(
                    ApiResponse.success("User resumes fetched successfully", resumes)
            );

        } catch (ApiException ex) {
            log.warn("Failed to fetch user resumes for userId={}: {}", userId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error while fetching user resumes for userId={}", userId, ex);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load user resumes."));
        }
    }

    /**
     * Fetch a single resume by resumeId
     * GET /api/admin/resume/{resumeId}
     */
    @GetMapping("/{resumeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumeResponse> getResume(@PathVariable Long resumeId) {
        try {
            log.debug("Fetching resume resumeId={}", resumeId);

            ResumeResponse response = resumeService.getById(resumeId);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Failed to fetch resume resumeId={}: {}", resumeId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResumeResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error while fetching resume resumeId={}", resumeId, ex);
            return ResponseEntity.internalServerError()
                    .body(ResumeResponse.fail("Failed to load resume."));
        }
    }

    /**
     * Fetch all versions for a resume
     * GET /api/admin/resume/{resumeId}/versions
     */
    @GetMapping("/{resumeId}/versions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResumeVersionResponse>>> getResumeVersions(@PathVariable Long resumeId) {
        try {
            log.debug("Fetching resume versions for resumeId={}", resumeId);

            List<ResumeVersionResponse> versions =
                    resumeVersionService.getVersionsByResumeForAdmin(resumeId);

            return ResponseEntity.ok(
                    ApiResponse.success("Resume versions fetched successfully", versions)
            );

        } catch (ApiException ex) {
            log.warn("Failed to fetch resume versions for resumeId={}: {}", resumeId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error while fetching resume versions for resumeId={}", resumeId, ex);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load resume versions."));
        }
    }

    /**
     * Fetch one resume version by versionId
     * GET /api/admin/resume/version/{versionId}
     */
    @GetMapping("/version/{versionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumeVersionResponse> getVersion(@PathVariable Long versionId) {
        try {
            log.debug("Fetching resume version versionId={}", versionId);

            ResumeVersionResponse response = resumeVersionService.getVersionForAdmin(versionId);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Failed to fetch resume version versionId={}: {}", versionId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResumeVersionResponse.fail(ex.getMessage()));

        } catch (backend.ai_interview.exception.ResumeNotFoundException ex) {
            log.warn("Resume version not found versionId={}: {}", versionId, ex.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ResumeVersionResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error while fetching resume version versionId={}", versionId, ex);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResumeVersionResponse.fail("Unexpected error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
        }
    }

    /**
     * Fetch editor data for one version
     * GET /api/admin/resume/version/{versionId}/editor
     */
    @GetMapping("/version/{versionId}/editor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumeEditorResponse> getEditorData(@PathVariable Long versionId) {
        try {
            log.debug("Fetching admin editor data for versionId={}", versionId);

            ResumeEditorResponse response = resumeEditorService.getEditorDataForAdmin(versionId);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Failed to fetch editor data for versionId={}: {}", versionId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResumeEditorResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error while fetching editor data for versionId={}", versionId, ex);
            return ResponseEntity.internalServerError()
                    .body(ResumeEditorResponse.fail("Failed to load resume editor data."));
        }
    }

    /**
     * Fetch preview data for one version
     * GET /api/admin/resume/version/{versionId}/preview
     */
    @GetMapping("/version/{versionId}/preview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumePreviewResponse> preview(@PathVariable Long versionId) {
        try {
            log.debug("Fetching admin preview data for versionId={}", versionId);

            ResumePreviewResponse response = resumePreviewService.getPreviewForAdmin(versionId);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Failed to fetch preview data for versionId={}: {}", versionId, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResumePreviewResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error while fetching preview data for versionId={}", versionId, ex);
            return ResponseEntity.internalServerError()
                    .body(ResumePreviewResponse.fail("Failed to load resume preview."));
        }
    }

    /**
     * Health check
     * GET /api/admin/resume/ping
     */
    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Admin resume module is working", "OK")
        );
    }
}