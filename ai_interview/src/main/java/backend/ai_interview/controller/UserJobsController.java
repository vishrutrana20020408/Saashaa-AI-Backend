package backend.ai_interview.controller;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Job;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.JobService;
import backend.ai_interview.service.MockJobStore;
import backend.ai_interview.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Jobs Controller
 *
 * Handles job listing and job application shortcuts for the frontend.
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/user/jobs")
public class UserJobsController {

    private final MockJobStore mockJobStore;
    private final JobService jobService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public UserJobsController(MockJobStore mockJobStore, JobService jobService, UserRepository userRepository, NotificationService notificationService) {
        this.mockJobStore = mockJobStore;
        this.jobService = jobService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get all available jobs
     * GET /api/user/jobs
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAvailableJobs(Authentication authentication) {
        String principal = authentication.getName();
        AppUser user = userRepository.findByUserId(principal)
                .or(() -> userRepository.findByEmailAddress(principal))
                .orElse(null);

        String userDomain = (user != null) ? user.getOnboardingDomain() : "TECH";
        
        // Normalize user domain for filtering
        String filterDomain = "TECH";
        if (userDomain != null) {
            if (userDomain.toUpperCase().contains("NON-TECHNICAL") || userDomain.toUpperCase().contains("NON_TECH")) {
                filterDomain = "NON_TECH";
            }
        }

        Map<String, Object> data = new HashMap<>();
        
        // Filter MockJobStore results as well for backward compatibility
        final String finalFilterDomain = filterDomain;
        List<Map<String, Object>> allJobs = mockJobStore.getAllJobs().stream()
                .filter(job -> {
                    String jobDomain = (String) job.getOrDefault("domain", "TECH");
                    if ("NON_TECH".equals(finalFilterDomain)) {
                        return "NON_TECH".equals(jobDomain);
                    }
                    return true; // TECH sees both
                })
                .toList();

        List<Map<String, Object>> recommended = allJobs.stream()
                .filter(job -> Boolean.TRUE.equals(job.get("isRecommended")))
                .toList();

        List<Map<String, Object>> regular = allJobs.stream()
                .filter(job -> !Boolean.TRUE.equals(job.get("isRecommended")))
                .toList();

        if (recommended.isEmpty() && !regular.isEmpty()) {
            int limit = Math.min(2, regular.size());
            recommended = regular.subList(0, limit);
            regular = regular.subList(limit, regular.size());
        }

        data.put("recommendedJobs", recommended);
        data.put("regularJobs", regular);
        
        // Also include real jobs from DB if any
        List<Job> realJobs = jobService.getActiveJobsForUser(filterDomain);
        data.put("dbJobs", realJobs);

        return ResponseEntity.ok(ApiResponse.success("Jobs fetched successfully", data));
    }

    /**
     * Apply for a job
     * POST /api/user/jobs/{jobId}/apply
     */
    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> applyForJob(@PathVariable Long jobId, Authentication authentication) {
        AppUser user = null;
        if (authentication != null) {
            String principal = authentication.getName();
            user = userRepository.findByUserId(principal)
                    .or(() -> userRepository.findByEmailAddress(principal))
                    .orElse(null);
        }

        String applicantName = "Applicant";
        if (user != null) {
            String firstName = user.getName() != null ? user.getName().trim() : "";
            String lastName = user.getSurname() != null ? user.getSurname().trim() : "";
            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isEmpty()) {
                applicantName = fullName;
            }
        }

        String jobTitle = "the selected job";
        Map<String, Object> job = mockJobStore.findJobById(jobId);
        if (job != null && job.get("title") != null) {
            jobTitle = String.valueOf(job.get("title"));
        }

        notificationService.createNotification(
                "New application for " + jobTitle,
                applicantName + " has applied for " + jobTitle + ".",
                "JOB_APPLICATION"
        );

        Map<String, Object> data = new HashMap<>();
        data.put("applicationId", System.currentTimeMillis());
        data.put("jobId", jobId);
        data.put("interviewAvailable", true);
        data.put("status", "SUBMITTED");

        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", data));
    }
}
