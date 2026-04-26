package backend.ai_interview.controller;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.JobApplicationResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.JobApplicationException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.JobApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@SuppressWarnings("all")
@RequestMapping("/api/user/job-application")
@Slf4j
public class UserJobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public UserJobApplicationController(
            JobApplicationService jobApplicationService,
            UserRepository userRepository,
            AdminRepository adminRepository
    ) {
        this.jobApplicationService = jobApplicationService;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
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

        Optional<AppUser> userOptional = userRepository.findByUserId(principal);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        userOptional = userRepository.findByEmailAddress(principal);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        Optional<Admin> adminOptional = adminRepository.findByAdminId(principal);
        if (adminOptional.isEmpty()) {
            adminOptional = adminRepository.findByEmailAddress(principal);
        }

        if (adminOptional.isPresent()) {
            AppUser mirrorUser = userRepository.findByEmailAddress(adminOptional.get().getEmailAddress()).orElse(null);
            if (mirrorUser != null) {
                return mirrorUser;
            }
        }

        throw new ApiException("User not found for principal: " + principal);
    }

    @GetMapping(produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listUserJobApplications(
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            List<JobApplicationResponse> applications = jobApplicationService.getAll(user.getUserId());

            if (size != null && size > 0 && applications.size() > size) {
                applications = applications.subList(0, Math.min(size, applications.size()));
            }

            return ResponseEntity.ok(ApiResponse.success("Job applications loaded successfully", applications));
        } catch (ApiException | JobApplicationException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to load user job applications", ex);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load job applications."));
        }
    }

    @GetMapping(value = "/{applicationId}", produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserJobApplicationById(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            JobApplicationResponse application = jobApplicationService.getById(user.getUserId(), applicationId);
            return ResponseEntity.ok(ApiResponse.success("Job application loaded successfully", application));
        } catch (JobApplicationException | ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to load job application", ex);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load job application."));
        }
    }
}
