package backend.ai_interview.controller;

import backend.ai_interview.dto.request.UserOnboardingRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.UserOnboardingResponse;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.UserOnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User Onboarding Controller
 *
 * Handles backend-integrated onboarding flows for authenticated users
 * and stays aligned with the latest project structure.
 *
 * Supported flows:
 * - save onboarding selections
 * - fetch onboarding selections
 * - fetch onboarding status
 * - reset onboarding selections
 *
 * Endpoints:
 * - POST   /api/user/onboarding
 * - GET    /api/user/onboarding
 * - GET    /api/user/onboarding/status
 * - DELETE /api/user/onboarding/reset
 * - GET    /api/user/onboarding/ping
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/user/onboarding")
public class UserOnboardingController {

    private final UserOnboardingService userOnboardingService;

    public UserOnboardingController(UserOnboardingService userOnboardingService) {
        this.userOnboardingService = userOnboardingService;
    }

    private String requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("User is not authenticated.");
        }

        String userId = authentication.getName();
        if (userId == null || userId.trim().isEmpty()) {
            throw new ApiException("User not found");
        }

        return userId;
    }

    /**
     * Save onboarding selections
     * POST /api/user/onboarding
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserOnboardingResponse> save(
            @RequestBody UserOnboardingRequest request,
            Authentication authentication
    ) {
        try {
            String userId = requireAuthenticatedUser(authentication);
            UserOnboardingResponse saved = userOnboardingService.save(userId, request);
            return ResponseEntity.ok(saved);
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UserOnboardingResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserOnboardingResponse.fail("Failed to save onboarding. Please try again."));
        }
    }

    /**
     * Fetch onboarding selections
     * GET /api/user/onboarding
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserOnboardingResponse> get(Authentication authentication) {
        try {
            String userId = requireAuthenticatedUser(authentication);
            UserOnboardingResponse response = userOnboardingService.get(userId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UserOnboardingResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserOnboardingResponse.fail("Failed to load onboarding."));
        }
    }

    /**
     * Fetch onboarding status
     * GET /api/user/onboarding/status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserOnboardingResponse> getStatus(Authentication authentication) {
        try {
            String userId = requireAuthenticatedUser(authentication);
            UserOnboardingResponse response = userOnboardingService.get(userId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UserOnboardingResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserOnboardingResponse.fail("Failed to load onboarding status."));
        }
    }

    /**
     * Reset onboarding selections
     * DELETE /api/user/onboarding/reset
     */
    @DeleteMapping("/reset")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> reset(Authentication authentication) {
        try {
            String userId = requireAuthenticatedUser(authentication);
            userOnboardingService.reset(userId);
            return ResponseEntity.ok(
                    ApiResponse.success("Onboarding reset successfully", "OK")
            );
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Failed to reset onboarding."));
        }
    }

    /**
     * Optional health check
     * GET /api/user/onboarding/ping
     */
    @GetMapping("/ping")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Onboarding module is working", "OK")
        );
    }
}