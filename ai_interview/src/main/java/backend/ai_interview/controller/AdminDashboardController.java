package backend.ai_interview.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.InterviewSessionResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.InterviewSessionRepository;
import backend.ai_interview.repository.JobRepository;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.MockJobStore;
import backend.ai_interview.service.interview.InterviewSessionService;

/**
 * Admin Dashboard Controller
 *
 * Only ADMIN role can access (/api/admin/**)
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewSessionService interviewSessionService;
    private final MockJobStore mockJobStore;

    public AdminDashboardController(
            AdminRepository adminRepository,
            UserRepository userRepository,
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            InterviewSessionRepository interviewSessionRepository,
            InterviewSessionService interviewSessionService,
            MockJobStore mockJobStore
    ) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewSessionService = interviewSessionService;
        this.mockJobStore = mockJobStore;
    }

    /**
     * ADMIN HOME / DASHBOARD (Protected)
     * GET /api/admin/home
     * GET /api/admin/dashboard
     *
     * Returns both a welcome message and essential admin details for the frontend navbar.
     */
    @GetMapping({"/home", "/dashboard"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(ApiResponse.fail("Not authenticated"));
            }

            String principal = authentication.getName();
            
            // Try resolving as Admin first
            Admin admin = adminRepository.findByAdminId(principal).orElse(null);
            if (admin == null) {
                admin = adminRepository.findByEmailAddress(principal).orElse(null);
            }

            if (admin == null) {
                return ResponseEntity.status(404).body(ApiResponse.fail("Admin record not found for principal: " + principal));
            }

            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", admin.getSNo());
            adminData.put("adminId", admin.getAdminId());
            adminData.put("firstName", admin.getName() != null ? admin.getName() : "");
            adminData.put("lastName", admin.getSurname() != null ? admin.getSurname() : "");
            adminData.put("fullName", (admin.getName() != null ? admin.getName() : "") + " " + (admin.getSurname() != null ? admin.getSurname() : ""));
            adminData.put("email", admin.getEmailAddress() != null ? admin.getEmailAddress() : "");
            adminData.put("role", admin.getRole() != null ? admin.getRole() : "ADMIN");

            return ResponseEntity.ok(
                    ApiResponse.success("Welcome Admin - Dashboard loaded successfully", adminData)
            );
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Critical Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
        }
    }

    /**
     * ADMIN DASHBOARD SUMMARY
     * GET /api/admin/dashboard/summary
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userRepository.count());
        data.put("totalAdmins", adminRepository.count());
        data.put("totalJobs", jobRepository.count());
        data.put("totalResumes", resumeRepository.count());
        data.put("totalInterviews", interviewSessionRepository.count());
        data.put("aiEngineStatus", "HEALTHY");
        data.put("lastUpdated", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(ApiResponse.success("Dashboard summary fetched", data));
    }

    /**
     * GET ALL USERS
     * GET /api/admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getSNo());
            map.put("userId", u.getUserId());
            map.put("name", u.getName());
            map.put("surname", u.getSurname());
            map.put("email", u.getEmailAddress());
            map.put("role", u.getRole());
            return map;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    /**
     * GET ALL JOBS
     * GET /api/admin/jobs
     */
    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllJobs() {
        List<Map<String, Object>> jobs = mockJobStore.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", jobs));
    }

    /**
     * CREATE JOB (POST /api/admin/jobs)
     */
    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createJob(@RequestBody Map<String, Object> jobData) {
        Map<String, Object> data = mockJobStore.addJob(jobData);
        return ResponseEntity.ok(ApiResponse.success("Job created successfully", data));
    }

    /**
     * UPDATE JOB (PUT /api/admin/jobs/{jobId})
     */
    @PutMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateJob(
            @PathVariable("jobId") Long jobId,
            @RequestBody Map<String, Object> jobData
    ) {
        Map<String, Object> data = mockJobStore.updateJob(jobId, jobData);
        if (data == null) {
            return ResponseEntity.status(404).body(ApiResponse.fail("Job not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", data));
    }

    /**
     * DELETE JOB (DELETE /api/admin/jobs/{jobId})
     */
    @DeleteMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteJob(@PathVariable("jobId") Long jobId) {
        boolean deleted = mockJobStore.deleteJob(jobId);
        if (!deleted) {
            return ResponseEntity.status(404).body(ApiResponse.fail("Job not found"));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", data));
    }

    /**
     * ADMIN INTERVIEW MONITOR
     * GET /api/admin/interview/monitor
     */
    @GetMapping("/interview/monitor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInterviewMonitor() {
        Map<String, Object> data = new HashMap<>();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCandidates", interviewSessionRepository.count());
        summary.put("activeSessions", 0);
        summary.put("completedSessions", interviewSessionRepository.count());
        summary.put("aiEngineStatus", "HEALTHY");
        summary.put("liveQuestion", "Waiting for live session...");
        
        data.put("summary", summary);
        data.put("candidates", List.of()); // Empty for now or fetch from repository
        
        return ResponseEntity.ok(ApiResponse.success("Interview monitor data fetched", data));
    }

    /**
     * ADMIN INTERVIEW SESSIONS
     * GET /api/admin/interview/sessions
     */
    @GetMapping({"/interview/sessions", "/interview/session"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InterviewSessionResponse>>> getAdminInterviewSessions(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(ApiResponse.fail("Not authenticated"));
            }

            String principal = authentication.getName();
            Admin admin = adminRepository.findByAdminId(principal).orElse(null);
            if (admin == null) {
                admin = adminRepository.findByEmailAddress(principal).orElse(null);
            }

            if (admin == null) {
                return ResponseEntity.status(404).body(ApiResponse.fail("Admin record not found for principal: " + principal));
            }

            List<InterviewSessionResponse> sessions = interviewSessionService.getSessionsByAdmin(admin.getSNo());
            return ResponseEntity.ok(ApiResponse.success("Admin interview sessions fetched", sessions));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Critical Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
        }
    }

    /**
     * ADMIN VALIDATE (Protected)
     * GET /api/admin/validate
     *
     * Used by the frontend admin layout to confirm the token is valid.
     */
    @GetMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(ApiResponse.fail("Not authenticated"));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("authenticated", true);
            data.put("role", "ADMIN");
            data.put("adminId", authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Admin validation succeeded", data));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Critical Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
        }
    }

    /**
     * ADMIN PROFILE (Protected)
     * GET /api/admin/me
     *
     * JWT subject contains adminId (UUID),
     * so query by adminId, not by primary key.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(ApiResponse.fail("Not authenticated"));
            }

            String principal = authentication.getName();

            Admin admin = adminRepository.findByAdminId(principal).orElse(null);
            if (admin == null) {
                admin = adminRepository.findByEmailAddress(principal).orElse(null);
            }

            if (admin == null) {
                return ResponseEntity.status(404).body(ApiResponse.fail("Admin record not found for principal: " + principal));
            }

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH-mm-ss");

            Map<String, Object> data = new HashMap<>();
            data.put("id", admin.getSNo());
            data.put("adminId", admin.getAdminId());
            data.put("firstName", admin.getName() != null ? admin.getName() : "");
            data.put("lastName", admin.getSurname() != null ? admin.getSurname() : "");
            data.put("fullName", (admin.getName() != null ? admin.getName() : "") + " " + (admin.getSurname() != null ? admin.getSurname() : ""));
            data.put("email", admin.getEmailAddress() != null ? admin.getEmailAddress() : "");
            data.put("mobileNumber", admin.getMobileNumber());
            data.put("role", admin.getRole() != null ? admin.getRole() : "ADMIN");

            // Compatibility fields for legacy frontend usage
            data.put("S_No", admin.getSNo());
            data.put("Admin_ID", admin.getAdminId());
            data.put("Name", admin.getName());
            data.put("Surname", admin.getSurname());
            data.put("Email_Address", admin.getEmailAddress());
            data.put("Mobile_Number", admin.getMobileNumber());
            
            if (admin.getAdminCreatedDate() != null) {
                data.put("Admin_Created_Date", admin.getAdminCreatedDate().format(dateFmt));
            } else {
                data.put("Admin_Created_Date", "");
            }
            
            if (admin.getAdminCreatedTime() != null) {
                data.put("Admin_Created_Time", admin.getAdminCreatedTime().format(timeFmt));
            } else {
                data.put("Admin_Created_Time", "");
            }
            
            data.put("Share_ID", admin.getShareId());

            return ResponseEntity.ok(ApiResponse.success("Admin profile fetched", data));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Critical Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
        }
    }
}