package backend.ai_interview.controller;

import backend.ai_interview.dto.request.AdminProfileUpdateRequest;
import backend.ai_interview.dto.response.AdminProfileResponse;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.ProfileSummaryResponse;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.profile.AdminProfileService;
import backend.ai_interview.service.profile.ProfileSyncService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Profile Controller
 *
 * Handles admin-side profile operations:
 * - fetch logged-in admin's full profile
 * - update logged-in admin's profile
 * - fetch navbar profile summary
 * - sync profile from a specific resume version
 * - optional health check
 *
 * Endpoints:
 * - GET    /api/admin/profile
 * - PUT    /api/admin/profile
 * - GET    /api/admin/profile/navbar-summary
 * - POST   /api/admin/profile/sync-from-resume/{userId}/versions/{versionId}
 * - GET    /api/admin/profile/ping
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/admin/profile")
public class AdminProfileController {

    private static final Logger log = LoggerFactory.getLogger(AdminProfileController.class);

    private final AdminProfileService adminProfileService;
    private final ProfileSyncService profileSyncService;
    private final backend.ai_interview.service.profile.ProfileDocumentService profileDocumentService;

    public AdminProfileController(
            AdminProfileService adminProfileService,
            ProfileSyncService profileSyncService,
            backend.ai_interview.service.profile.ProfileDocumentService profileDocumentService
    ) {
        this.adminProfileService = adminProfileService;
        this.profileSyncService = profileSyncService;
        this.profileDocumentService = profileDocumentService;
    }

    /**
     * Upload profile document (marksheet or resume)
     * POST /api/admin/profile/upload-document
     */
    @PostMapping(value = "/upload-document", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("docType") String docType,
            Authentication authentication
    ) {
        try {
            String adminId = extractAuthenticatedUsername(authentication);
            String fileUrl = profileDocumentService.storeDocument("admin", adminId, file, docType);
            
            // Update profile with the new file URL
            AdminProfileUpdateRequest updateRequest = new AdminProfileUpdateRequest();
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            switch (cleanDocType) {
                case "class10": updateRequest.setClass10MarksheetUrl(fileUrl); break;
                case "class12": updateRequest.setClass12MarksheetUrl(fileUrl); break;
                case "graduation": updateRequest.setGraduationMarksheetUrl(fileUrl); break;
                case "postgraduation": updateRequest.setPostGraduationMarksheetUrl(fileUrl); break;
                case "resume": updateRequest.setResumeUrl(fileUrl); break;
                default: throw new ApiException("Invalid document type: " + docType);
            }
            
            adminProfileService.updateMyProfile(adminId, updateRequest);
            
            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", fileUrl));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to upload document: " + ex.getMessage()));
        }
    }

    /**
     * Delete profile document
     * DELETE /api/admin/profile/document/{docType}
     */
    @DeleteMapping("/document/{docType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable String docType,
            Authentication authentication
    ) {
        try {
            String adminId = extractAuthenticatedUsername(authentication);
            profileDocumentService.deleteDocument("admin", adminId, docType);
            
            // Update profile with null URL
            AdminProfileUpdateRequest updateRequest = new AdminProfileUpdateRequest();
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            switch (cleanDocType) {
                case "class10": updateRequest.setClass10MarksheetUrl(null); break;
                case "class12": updateRequest.setClass12MarksheetUrl(null); break;
                case "graduation": updateRequest.setGraduationMarksheetUrl(null); break;
                case "postgraduation": updateRequest.setPostGraduationMarksheetUrl(null); break;
                case "resume": updateRequest.setResumeUrl(null); break;
                default: throw new ApiException("Invalid document type: " + docType);
            }
            
            adminProfileService.updateMyProfile(adminId, updateRequest);
            
            return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to delete document."));
        }
    }

    /**
     * GET /api/admin/profile
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfileResponse> getMyProfile(Authentication authentication) {
        try {
            String username = extractAuthenticatedUsername(authentication);

            log.debug("Fetching admin profile for: {}", username);

            AdminProfileResponse response = adminProfileService.getMyProfile(username);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Admin profile fetch failed: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(AdminProfileResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error fetching admin profile", ex);
            return ResponseEntity.internalServerError()
                    .body(AdminProfileResponse.fail("Failed to load admin profile."));
        }
    }

    /**
     * PUT /api/admin/profile
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfileResponse> updateMyProfile(
            @RequestBody AdminProfileUpdateRequest request,
            Authentication authentication
    ) {
        try {
            String username = extractAuthenticatedUsername(authentication);

            log.debug("Updating admin profile for: {}", username);

            AdminProfileResponse response = adminProfileService.updateMyProfile(username, request);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Admin profile update failed: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(AdminProfileResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error updating admin profile", ex);
            return ResponseEntity.internalServerError()
                    .body(AdminProfileResponse.fail("Failed to update admin profile."));
        }
    }

    /**
     * GET /api/admin/profile/navbar-summary
     */
    @GetMapping("/navbar-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileSummaryResponse> getNavbarSummary(Authentication authentication) {
        try {
            String username = extractAuthenticatedUsername(authentication);

            ProfileSummaryResponse response = adminProfileService.getNavbarSummary(username);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Navbar summary failed: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ProfileSummaryResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error fetching navbar summary", ex);
            return ResponseEntity.internalServerError()
                    .body(ProfileSummaryResponse.fail("Failed to load profile summary."));
        }
    }

    /**
     * POST /api/admin/profile/sync-from-resume/{userId}/versions/{versionId}
     */
    @PostMapping("/sync-from-resume/{userId}/versions/{versionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfileResponse> syncProfileFromResume(
            @PathVariable String userId,
            @PathVariable Long versionId,
            Authentication authentication
    ) {
        try {
            String adminUsername = extractAuthenticatedUsername(authentication);

            log.info("Admin {} syncing profile from user {} resume version {}",
                    adminUsername, userId, versionId);

            AdminProfileResponse response = profileSyncService.syncAdminProfileFromResume(
                    adminUsername,
                    userId,
                    versionId
            );

            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            log.warn("Profile sync failed: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(AdminProfileResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Unexpected error during profile sync", ex);
            return ResponseEntity.internalServerError()
                    .body(AdminProfileResponse.fail("Failed to sync admin profile from resume."));
        }
    }

    /**
     * Health check
     */
    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Admin profile module is working", "OK")
        );
    }

    /**
     * Extract authenticated username safely
     */
    private String extractAuthenticatedUsername(Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new ApiException("Unauthorized admin.");
        }
        return authentication.getName();
    }
}