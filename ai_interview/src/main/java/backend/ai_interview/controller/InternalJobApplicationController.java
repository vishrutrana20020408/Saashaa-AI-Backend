package backend.ai_interview.controller;

import backend.ai_interview.dto.request.AdminJobApplyRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.InternalJobApplication;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.service.InternalJobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@SuppressWarnings("all")
@RequestMapping("/api/internal-job-applications")
public class InternalJobApplicationController {

    private final InternalJobApplicationService service;
    private final AdminRepository adminRepository;

    public InternalJobApplicationController(InternalJobApplicationService service, AdminRepository adminRepository) {
        this.service = service;
        this.adminRepository = adminRepository;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@Valid @RequestBody AdminJobApplyRequest request, Authentication authentication) {
        Admin admin = getCurrentAdmin(authentication);
        InternalJobApplication application = service.apply(request, admin);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", application));
    }

    private Admin getCurrentAdmin(Authentication authentication) {
        if (authentication == null) throw new ApiException("Not authenticated");
        String principal = authentication.getName();
        return adminRepository.findByAdminId(principal)
                .or(() -> adminRepository.findByEmailAddress(principal))
                .orElseThrow(() -> new ApiException("Admin not found"));
    }
}
