package backend.ai_interview.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import backend.ai_interview.dto.response.ResumeResponse;
import backend.ai_interview.dto.response.ResumeScanResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeFileAsset;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.ResumeNotFoundException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.integration.ai.AtsClient;
import backend.ai_interview.service.resume.ResumeAtsService;
import backend.ai_interview.service.resume.ResumeService;
import backend.ai_interview.service.resume.ResumeStorageService;

/**
 * User Resume Controller
 *
 * Handles user resume-module operations in the latest
 * backend-integrated project structure.
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/user/resume")
public class UserResumeController {

    private static final Logger log = LoggerFactory.getLogger(UserResumeController.class);

    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ResumeStorageService resumeStorageService;
    private final ResumeAtsService resumeAtsService;

    public UserResumeController(
            ResumeService resumeService,
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            AdminRepository adminRepository,
            ResumeStorageService resumeStorageService,
            ResumeAtsService resumeAtsService
    ) {
        this.resumeService = resumeService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.resumeStorageService = resumeStorageService;
        this.resumeAtsService = resumeAtsService;
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
        
        // Try resolving as AppUser first
        AppUser user = userRepository.findByUserId(principal).orElse(null);
        if (user == null) {
            user = userRepository.findByEmailAddress(principal).orElse(null);
        }

        if (user == null) {
            // Try resolving as Admin and create a proxy AppUser if found
            Admin admin = adminRepository.findByAdminId(principal).orElse(null);
            if (admin == null) {
                admin = adminRepository.findByEmailAddress(principal).orElse(null);
            }

            if (admin != null) {
                // Check if an AppUser already exists for this admin email
                user = userRepository.findByEmailAddress(admin.getEmailAddress()).orElse(null);
                
                if (user == null) {
                    // Create a mirror AppUser record for the admin so they can have a resume
                    user = new AppUser();
                    user.setUserId(admin.getAdminId());
                    user.setName(admin.getName());
                    user.setSurname(admin.getSurname());
                    user.setEmailAddress(admin.getEmailAddress());
                    user.setMobileNumber(admin.getMobileNumber());
                    user.setPassword(admin.getPassword()); // Mirrors same hash
                    user.setShareId(admin.getShareId()); // Important for DB constraint
                    user.setRole("ADMIN");
                    user.setOnboardingDone(true);
                    user.setProfileCreated(true);
                    user.setUserCreatedDate(java.time.LocalDate.now());
                    user.setUserCreatedTime(java.time.LocalTime.now());
                    userRepository.save(user);
                    userRepository.flush(); // Ensure it is flushed immediately for following service calls
                }
            }
        }

        if (user == null) {
            throw new ApiException("User not found");
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
     * Serve stored resume file locally
     * GET /api/user/resume/asset/{assetId}/download
     * 
     * Users can only download their own assets.
     * Admins can download any asset.
     */
    @GetMapping(
            value = "/asset/{assetId}/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> serveResumeAsset(
            @PathVariable Long assetId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeFileAsset asset = resumeStorageService.getById(assetId);

            if (asset == null) {
                return ResponseEntity.notFound().build();
            }

            // Check permissions: user must own the asset, or requester must be admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                // For non-admin users, verify they own the asset
                String rolePrefix = (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) ? "admins/" : "users/";
                String ownerId = rolePrefix + user.getUserId();
                
                boolean ownsAsset = resumeStorageService.isAssetOwnedBy(assetId, ownerId);
                if (!ownsAsset) {
                    return ResponseEntity.status(403).body("Access denied");
                }
            }

            // Read and decrypt file from disk
            byte[] fileContent = resumeStorageService.getDecryptedFileContent(assetId);
            String filename = asset.getFileName() != null ? asset.getFileName() : "file";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(fileContent);

        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to download file");
        }
    }

    /**
     * Resume Scan
     * POST /api/user/resume/scan
     */
    @PostMapping(
            value = "/scan",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeScanResponse> scanResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            requireAuthenticatedPrincipal(authentication);
            validateResumeFile(file);

            return ResponseEntity.ok(
                    new ResumeScanResponse(
                            true,
                            "Resume file received successfully.",
                            file.getOriginalFilename(),
                            null
                    )
            );
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(new ResumeScanResponse(false, ex.getMessage(), null, null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ResumeScanResponse(
                            false,
                            "Resume scan failed. Please try again.",
                            null,
                            null
                    ));
        }
    }

    /**
     * Resume Upload
     * POST /api/user/resume/upload
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeScanResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            validateResumeFile(file);

            // Separate storage for admins and users
            String rolePrefix = "users/";
            if (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) {
                rolePrefix = "admins/";
            }
            String ownerId = rolePrefix + user.getUserId();

            ResumeFileAsset asset = resumeStorageService.storeOriginal(file, ownerId);

            Resume resume = resumeRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (resume == null) {
                resume = new Resume();
                resume.setUser(user);
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

            Integer atsScore = null;
            String improvedText = null;
            String atsAvailabilityMessage = null;
            String originalFileName = asset.getFileName() != null ? asset.getFileName().toLowerCase() : "";

            if (originalFileName.endsWith(".txt")) {
                try {
                    String resumeText = new String(file.getBytes(), StandardCharsets.UTF_8);
                    AtsClient.AtsScoreResult atsResult = resumeAtsService.processResumeAts(user, resumeText, null, resume.getTitle());
                    atsScore = atsResult.getScore();
                    improvedText = atsResult.getImprovedResumeText();
                } catch (ApiException | IOException ex) {
                    log.warn("ATS scoring unavailable during resume upload for user {}.", user.getUserId(), ex);
                    atsAvailabilityMessage = ex instanceof ApiException ? ex.getMessage() : "ATS scoring is unavailable for this upload.";
                }
            } else {
                atsAvailabilityMessage = "ATS scoring is available only for plain text uploads. Resume was saved successfully.";
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("fileUrl", asset.getFileUrl());
            payload.put("storedFileName", asset.getStoredFileName());
            payload.put("originalFileName", asset.getFileName());
            if (atsScore != null) {
                payload.put("atsScore", atsScore);
            }
            if (improvedText != null) {
                payload.put("improvedText", improvedText);
            }
            if (atsAvailabilityMessage != null) {
                payload.put("atsMessage", atsAvailabilityMessage);
            }

            return ResponseEntity.ok(
                    new ResumeScanResponse(
                            true,
                            atsScore != null
                                    ? "Resume uploaded locally."
                                    : "Resume uploaded locally. " + atsAvailabilityMessage,
                            payload,
                            atsScore
                    )
            );
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(new ResumeScanResponse(false, ex.getMessage(), null, null));
        } catch (Exception ex) {
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
     * Get all resumes of current user
     * GET /api/user/resume
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getMyResumes(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            List<ResumeResponse> resumes = resumeService.getResumesByUser(user.getUserId());
            return ResponseEntity.ok(resumes);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load resumes."));
        }
    }

    /**
     * Get a specific resume by ID for the current user
     * GET /api/user/resume/{resumeId}
     */
    @GetMapping(value = "/{resumeId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getResumeById(
            @PathVariable Long resumeId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getById(user.getUserId(), resumeId);
            return ResponseEntity.ok(ApiResponse.success("Resume retrieved successfully", resume));
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to fetch resume by id."));
        }
    }

    /**
     * Update a specific resume's content by ID
     * PUT /api/user/resume/{resumeId}/content
     */
    @PutMapping(
            value = "/{resumeId:\\d+}/content",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> updateResumeContentById(
            @PathVariable Long resumeId,
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getById(user.getUserId(), resumeId);

            Object rawText = request.get("rawText");
            if (rawText == null || rawText.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail("Resume content is required."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Resume content update request accepted.");
            response.put("resumeId", resume.getResumeId());
            response.put("resumeCode", resume.getResumeCode());
            response.put("fileName", resume.getOriginalFileName());
            response.put("rawText", rawText.toString());
            response.put("updatedAt", resume.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to update resume content."));
        }
    }

    /**
     * Download a specific resume by ID
     * GET /api/user/resume/{resumeId}/download
     */
    @GetMapping(value = "/{resumeId:\\d+}/download")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> downloadResumeById(
            @PathVariable Long resumeId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getById(user.getUserId(), resumeId);

            String fileName = resume.getOriginalFileName() != null && !resume.getOriginalFileName().isBlank()
                    ? resume.getOriginalFileName()
                    : "resume.txt";

            String placeholderContent =
                    "Resume download placeholder for resume: " +
                            (resume.getResumeCode() != null ? resume.getResumeCode() : resumeId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(placeholderContent.getBytes(StandardCharsets.UTF_8));
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to download resume."));
        }
    }

    /**
     * Get current/latest resume of current user
     * GET /api/user/resume/current
     * GET /api/user/resume/default
     * 
     * Returns 200 OK with null data if no resume exists (new user)
     */
    @GetMapping(value = {"/current", "/default"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getCurrentResume(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            try {
                ResumeResponse resume = resumeService.getLatestResumeForUser(user.getUserId());
                return ResponseEntity.ok(ApiResponse.success("Resume retrieved successfully", resume));
            } catch (ResumeNotFoundException ex) {
                log.warn("ResumeNotFoundException in getCurrentResume: {}", ex.getMessage());
                // New user with no resume - return null data with 200 OK
                return ResponseEntity.ok(ApiResponse.success("No resume found", null));
            }
        } catch (ApiException ex) {
            log.error("ApiException in getCurrentResume: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Exception in getCurrentResume: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to fetch current resume: " + ex.getMessage()));
        }
    }

    /**
     * Get current resume content
     * GET /api/user/resume/content
     */
    @GetMapping(value = "/content", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getCurrentResumeContent(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getLatestResumeForUser(user.getUserId());
            return ResponseEntity.ok(resume);
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to fetch current resume content."));
        }
    }

    /**
     * Update current resume content
     * PUT /api/user/resume/current/content
     */
    @PutMapping(
            value = "/current/content",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> updateCurrentResumeContent(
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);

            Object rawText = request.get("rawText");
            if (rawText == null || rawText.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail("Resume content is required."));
            }

            ResumeResponse resume = resumeService.getLatestResumeForUser(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Resume content update request accepted.");
            response.put("resumeId", resume.getResumeId());
            response.put("resumeCode", resume.getResumeCode());
            response.put("fileName", resume.getOriginalFileName());
            response.put("rawText", rawText.toString());
            response.put("updatedAt", resume.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to update current resume content."));
        }
    }

    /**
     * Calculate ATS score for a specific resume
     * POST /api/user/resume/{resumeId}/ats-score
     */
    @PostMapping(
            value = "/{resumeId}/ats-score",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> calculateAtsScore(
            @PathVariable Long resumeId,
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getById(user.getUserId(), resumeId);

            String rawText = (request != null && request.get("rawText") != null)
                    ? request.get("rawText").toString()
                    : "";
            String jobDescription = (request != null && request.get("jobDescription") != null)
                    ? request.get("jobDescription").toString()
                    : null;

            // Use the real ATS service instead of a mock calculation
            AtsClient.AtsScoreResult atsResult = resumeAtsService.processResumeAts(user, rawText, jobDescription, resume.getTitle());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ATS score calculated successfully.");
            response.put("resumeId", resume.getResumeId());
            response.put("atsScore", atsResult.getScore());
            response.put("tips", atsResult.getSuggestions());
            response.put("matchedKeywords", atsResult.getMatchedKeywords());
            response.put("missingKeywords", atsResult.getMissingKeywords());

            return ResponseEntity.ok(response);
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to calculate ATS score."));
        }
    }

    /**
     * Calculate ATS score for the current/latest resume
     * POST /api/user/resume/current/ats-score
     */
    @PostMapping(
            value = "/current/ats-score",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> calculateCurrentAtsScore(
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getLatestResumeForUser(user.getUserId());

            String rawText = (request != null && request.get("rawText") != null)
                    ? request.get("rawText").toString()
                    : "";
            String jobDescription = (request != null && request.get("jobDescription") != null)
                    ? request.get("jobDescription").toString()
                    : null;

            // Use the real ATS service instead of a mock calculation
            AtsClient.AtsScoreResult atsResult = resumeAtsService.processResumeAts(user, rawText, jobDescription, resume.getTitle());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ATS score calculated successfully.");
            response.put("resumeId", resume.getResumeId());
            response.put("atsScore", atsResult.getScore());
            response.put("tips", atsResult.getSuggestions());
            response.put("matchedKeywords", atsResult.getMatchedKeywords());
            response.put("missingKeywords", atsResult.getMissingKeywords());

            return ResponseEntity.ok(response);
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to calculate ATS score for current resume."));
        }
    }

    /**
     * Download current/latest resume
     * GET /api/user/resume/current/download
     */
    @GetMapping(value = "/current/download")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> downloadCurrentResume(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            ResumeResponse resume = resumeService.getLatestResumeForUser(user.getUserId());

            String fileName = resume.getOriginalFileName() != null && !resume.getOriginalFileName().isBlank()
                    ? resume.getOriginalFileName()
                    : "resume.txt";

            String placeholderContent =
                    "Resume download placeholder for resume: " +
                            (resume.getResumeCode() != null ? resume.getResumeCode() : "current");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(placeholderContent.getBytes(StandardCharsets.UTF_8));
        } catch (ResumeNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to download current resume."));
        }
    }

    /**
     * Health check for resume module
     * GET /api/user/resume/ping
     */
    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Resume module is working", "OK")
        );
    }
}