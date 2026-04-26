package backend.ai_interview.controller;

import backend.ai_interview.dto.request.AdminOnboardingRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.AdminOnboardingResponse;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.AdminOnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Onboarding Controller
 *
 * Handles onboarding flows for authenticated admins.
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/admin/onboarding")
public class AdminOnboardingController {

    private final AdminOnboardingService adminOnboardingService;

    public AdminOnboardingController(AdminOnboardingService adminOnboardingService) {
        this.adminOnboardingService = adminOnboardingService;
    }

    private String requireAuthenticatedAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("Admin is not authenticated.");
        }

        String adminId = authentication.getName();
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new ApiException("Admin not found");
        }

        return adminId;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOnboardingResponse> save(
            @RequestBody AdminOnboardingRequest request,
            Authentication authentication
    ) {
        try {
            String adminId = requireAuthenticatedAdmin(authentication);
            AdminOnboardingResponse saved = adminOnboardingService.save(adminId, request);
            return ResponseEntity.ok(saved);
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AdminOnboardingResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminOnboardingResponse.fail("Failed to save onboarding. Please try again."));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOnboardingResponse> get(Authentication authentication) {
        try {
            String adminId = requireAuthenticatedAdmin(authentication);
            AdminOnboardingResponse response = adminOnboardingService.get(adminId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AdminOnboardingResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminOnboardingResponse.fail("Failed to load onboarding."));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOnboardingResponse> getStatus(Authentication authentication) {
        try {
            String adminId = requireAuthenticatedAdmin(authentication);
            AdminOnboardingResponse response = adminOnboardingService.get(adminId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AdminOnboardingResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminOnboardingResponse.fail("Failed to load onboarding status."));
        }
    }

    @DeleteMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reset(Authentication authentication) {
        try {
            String adminId = requireAuthenticatedAdmin(authentication);
            adminOnboardingService.reset(adminId);
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

    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Admin Onboarding module is working", "OK")
        );
    }
}
