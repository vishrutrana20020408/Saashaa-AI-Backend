package backend.ai_interview.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.request.JobPostRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.Company;
import backend.ai_interview.entity.Job;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.CompanyRepository;
import backend.ai_interview.service.JobService;
import jakarta.validation.Valid;

@RestController
@SuppressWarnings("all")
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final CompanyRepository companyRepository;
    private final AdminRepository adminRepository;

    public JobController(JobService jobService, CompanyRepository companyRepository, AdminRepository adminRepository) {
        this.jobService = jobService;
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
    }

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody JobPostRequest request, Authentication authentication) {
        Company company = getCurrentCompany(authentication);
        Job job = jobService.createJob(request, company);
        return ResponseEntity.ok(ApiResponse.success("Job posted successfully", job));
    }

    @GetMapping("/company")
    public ResponseEntity<?> getCompanyJobs(Authentication authentication) {
        Company company = getCurrentCompany(authentication);
        List<Job> jobs = jobService.getJobsByCompany(company);
        return ResponseEntity.ok(ApiResponse.success("Company jobs fetched", jobs));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @Valid @RequestBody JobPostRequest request, Authentication authentication) {
        Company company = getCurrentCompany(authentication);
        Job updatedJob = jobService.updateJob(id, request, company);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updatedJob));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, Authentication authentication) {
        Company company = getCurrentCompany(authentication);
        jobService.deleteJob(id, company);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", Map.of("jobId", id)));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActiveJobs(Authentication authentication) {
        String adminDomain = "TECH"; // default
        if (authentication != null && authentication.isAuthenticated()) {
            String adminId = authentication.getName();
            Admin admin = adminRepository.findByAdminId(adminId)
                    .or(() -> adminRepository.findByEmailAddress(adminId))
                    .orElse(null);
            if (admin != null && admin.getOnboardingDomain() != null) {
                adminDomain = admin.getOnboardingDomain();
            }
        }
        
        // Normalize domain
        String filterDomain = "TECH";
        if (adminDomain != null) {
            if (adminDomain.toUpperCase().contains("NON-TECHNICAL") || adminDomain.toUpperCase().contains("NON_TECH")) {
                filterDomain = "NON_TECH";
            }
        }

        List<Job> filteredJobs = jobService.getActiveJobsForUser(filterDomain);
        return ResponseEntity.ok(ApiResponse.success("Active jobs fetched for admin domain: " + filterDomain, filteredJobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJob(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success("Job details fetched", job));
    }

    private Company getCurrentCompany(Authentication authentication) {
        if (authentication == null) throw new ApiException("Not authenticated");
        String principal = authentication.getName();
        return companyRepository.findByCompanyId(principal)
                .or(() -> companyRepository.findByEmailAddress(principal))
                .orElseThrow(() -> new ApiException("Company not found"));
    }
}
