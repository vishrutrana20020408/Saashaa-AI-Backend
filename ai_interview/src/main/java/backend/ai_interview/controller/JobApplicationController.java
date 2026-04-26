package backend.ai_interview.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.request.AdminJobApplyRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.InternalJobApplication;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.InternalJobApplicationRepository;
import backend.ai_interview.service.InternalJobApplicationService;
import jakarta.validation.Valid;

@RestController
@SuppressWarnings("all")
@RequestMapping("/api/job-applications")
public class JobApplicationController {

    private final AdminRepository adminRepository;
    private final InternalJobApplicationRepository applicationRepository;
    private final InternalJobApplicationService applicationService;

    public JobApplicationController(
            AdminRepository adminRepository,
            InternalJobApplicationRepository applicationRepository,
            InternalJobApplicationService applicationService
    ) {
        this.adminRepository = adminRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
    }

    /**
     * Admin applies for a job
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InternalJobApplication>> applyForJob(
            @Valid @RequestBody AdminJobApplyRequest request,
            Authentication authentication
    ) {
        String adminPrincipal = authentication.getName();

        Admin admin = adminRepository.findByAdminId(adminPrincipal)
                .or(() -> adminRepository.findByEmailAddress(adminPrincipal))
                .orElseThrow(() -> new ApiException("Admin not found"));

        InternalJobApplication application = applicationService.apply(request, admin);

        return ResponseEntity.ok(ApiResponse.success("Applied successfully", application));
    }

    /**
     * Company views applications for its jobs (Approval Messages)
     */
    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompanyApplications(Authentication authentication) {
        String companyPrincipal = authentication.getName();
        
        List<InternalJobApplication> applications = applicationRepository.findByJob_Company_CompanyIdOrderByAppliedAtDesc(companyPrincipal);
        if (applications.isEmpty()) {
            // Try searching by email as well
            // (Assumes companyId is used as the principal name in the token)
        }

        List<Map<String, Object>> response = applications.stream().map(app -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("applicationId", app.getId());
            map.put("jobTitle", app.getJob().getPost());
            map.put("adminName", app.getAdmin().getName() + " " + app.getAdmin().getSurname());
            map.put("adminEmail", app.getAdmin().getEmailAddress());
            map.put("adminId", app.getAdmin().getAdminId());
            map.put("status", app.getStatus());
            map.put("appliedAt", app.getAppliedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Applications fetched", response));
    }
}
