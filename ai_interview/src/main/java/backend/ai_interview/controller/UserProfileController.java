package backend.ai_interview.controller;

import backend.ai_interview.dto.request.UserProfileUpdateRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.ProfileSummaryResponse;
import backend.ai_interview.dto.response.UserProfileResponse;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.profile.ProfileSyncService;
import backend.ai_interview.service.profile.UserProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User Profile Controller
 *
 * Handles authenticated user-profile operations in the
 * backend-integrated project structure.
 *
 * Endpoints:
 * - GET    /api/user/profile
 * - PUT    /api/user/profile
 * - GET    /api/user/profile/navbar-summary
 * - POST   /api/user/profile/sync-from-resume/{resumeId}/versions/{versionId}
 * - GET    /api/user/profile/ping
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ProfileSyncService profileSyncService;
    private final backend.ai_interview.service.profile.ProfileDocumentService profileDocumentService;

    public UserProfileController(
            UserProfileService userProfileService,
            ProfileSyncService profileSyncService,
            backend.ai_interview.service.profile.ProfileDocumentService profileDocumentService
    ) {
        this.userProfileService = userProfileService;
        this.profileSyncService = profileSyncService;
        this.profileDocumentService = profileDocumentService;
    }

    /**
     * Upload profile document (marksheet or resume)
     * POST /api/user/profile/upload-document
     */
    @PostMapping(value = "/upload-document", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("docType") String docType,
            Authentication authentication
    ) {
        try {
            String userId = extractAuthenticatedUsername(authentication);
            String fileUrl = profileDocumentService.storeDocument("user", userId, file, docType);
            
            // Update profile with the new file URL
            UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            switch (cleanDocType) {
                case "class10": updateRequest.setClass10MarksheetUrl(fileUrl); break;
                case "class12": updateRequest.setClass12MarksheetUrl(fileUrl); break;
                case "graduation": updateRequest.setGraduationMarksheetUrl(fileUrl); break;
                case "postgraduation": updateRequest.setPostGraduationMarksheetUrl(fileUrl); break;
                case "resume": updateRequest.setResumeUrl(fileUrl); break;
                case "profilepicture": updateRequest.setProfilePictureUrl(fileUrl); break;
                default: throw new ApiException("Invalid document type: " + docType);
            }
            
            userProfileService.updateMyProfile(userId, updateRequest);
            
            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", fileUrl));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to upload document: " + ex.getMessage()));
        }
    }

    /**
     * Delete profile document
     * DELETE /api/user/profile/document/{docType}
     */
    @DeleteMapping("/document/{docType}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable String docType,
            Authentication authentication
    ) {
        try {
            String userId = extractAuthenticatedUsername(authentication);
            profileDocumentService.deleteDocument("user", userId, docType);
            
            // Update profile with null URL
            UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            switch (cleanDocType) {
                case "class10": updateRequest.setClass10MarksheetUrl(null); break;
                case "class12": updateRequest.setClass12MarksheetUrl(null); break;
                case "graduation": updateRequest.setGraduationMarksheetUrl(null); break;
                case "postgraduation": updateRequest.setPostGraduationMarksheetUrl(null); break;
                case "resume": updateRequest.setResumeUrl(null); break;
                case "profilepicture": updateRequest.setProfilePictureUrl(null); break;
                default: throw new ApiException("Invalid document type: " + docType);
            }
            
            userProfileService.updateMyProfile(userId, updateRequest);
            
            return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to delete document."));
        }
    }

    /**
     * Fetch logged-in user's full profile
     * GET /api/user/profile
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        try {
            String username = extractAuthenticatedUsername(authentication);
            UserProfileResponse response = userProfileService.getMyProfile(username);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(UserProfileResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(UserProfileResponse.fail("Failed to load user profile."));
        }
    }

    /**
     * Update logged-in user's profile
     * PUT /api/user/profile
     */
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @RequestBody UserProfileUpdateRequest request,
            Authentication authentication
    ) {
        try {
            String username = extractAuthenticatedUsername(authentication);
            UserProfileResponse response = userProfileService.updateMyProfile(username, request);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(UserProfileResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(UserProfileResponse.fail("Failed to update user profile."));
        }
    }

    /**
     * Fetch compact navbar summary of logged-in user's profile
     * GET /api/user/profile/navbar-summary
     */
    @GetMapping("/navbar-summary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileSummaryResponse> getNavbarSummary(Authentication authentication) {
        try {
            String username = extractAuthenticatedUsername(authentication);
            ProfileSummaryResponse response = userProfileService.getNavbarSummary(username);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ProfileSummaryResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ProfileSummaryResponse.fail("Failed to load profile summary."));
        }
    }

    /**
     * Sync logged-in user's official profile from a specific resume version
     * POST /api/user/profile/sync-from-resume/{resumeId}/versions/{versionId}
     */
    @PostMapping("/sync-from-resume/{resumeId}/versions/{versionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> syncProfileFromResume(
            @PathVariable Long resumeId,
            @PathVariable Long versionId,
            Authentication authentication
    ) {
        try {
            String username = extractAuthenticatedUsername(authentication);
            UserProfileResponse response =
                    profileSyncService.syncUserProfileFromResume(username, resumeId, versionId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(UserProfileResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(UserProfileResponse.fail("Failed to sync profile from resume."));
        }
    }

    /**
     * Health check
     * GET /api/user/profile/ping
     */
    @GetMapping("/ping")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("User profile module is working", "OK")
        );
    }

    /**
     * Safely extract authenticated username
     */
    private String extractAuthenticatedUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("User is not authenticated.");
        }

        String username = authentication.getName();
        if (username == null || username.isBlank()) {
            throw new ApiException("User not found");
        }

        return username;
    }
}