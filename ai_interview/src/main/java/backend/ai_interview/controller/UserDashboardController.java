package backend.ai_interview.controller;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * User Dashboard Controller
 *
 * ✅ Only USER role can access (/api/user/**)
 * ❌ Admins cannot access these endpoints
 *
 * Frontend usage:
 * - GET /api/user/home
 * - GET /api/user/me
 *
 * Fixed version:
 * - resolves authenticated principal by userId OR emailAddress
 * - prevents null date/time formatting crashes
 * - returns frontend-friendly fields consistently
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/user")
@Transactional
public class UserDashboardController {

    private final UserRepository userRepository;
    private final backend.ai_interview.repository.AdminRepository adminRepository;
    private final ResumeRepository resumeRepository;

    public UserDashboardController(
            UserRepository userRepository,
            backend.ai_interview.repository.AdminRepository adminRepository,
            ResumeRepository resumeRepository
    ) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.resumeRepository = resumeRepository;
    }

    private AppUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ApiException("Unauthorized user");
        }

        String principal = authentication.getName().trim();

        // Try resolving as AppUser first
        AppUser user = userRepository.findByUserId(principal).orElse(null);
        if (user == null) {
            user = userRepository.findByEmailAddress(principal).orElse(null);
        }

        if (user == null) {
            // Try resolving as Admin and create a proxy AppUser if found
            backend.ai_interview.entity.Admin admin = adminRepository.findByAdminId(principal).orElse(null);
            if (admin == null) {
                admin = adminRepository.findByEmailAddress(principal).orElse(null);
            }

            if (admin != null) {
                // Check if an AppUser already exists for this admin email
                user = userRepository.findByEmailAddress(admin.getEmailAddress()).orElse(null);

                if (user == null) {
                    // Create a mirror AppUser record for the admin so they can have a dashboard
                    user = AppUser.builder()
                            .userId(admin.getAdminId())
                            .name(admin.getName())
                            .surname(admin.getSurname())
                            .emailAddress(admin.getEmailAddress())
                            .mobileNumber(admin.getMobileNumber())
                            .password(admin.getPassword()) // Mirrors same hash
                            .shareId(admin.getShareId()) // Important for DB constraint
                            .role("ADMIN")
                            .onboardingDone(true)
                            .profileCreated(true)
                            .userCreatedDate(java.time.LocalDate.now())
                            .userCreatedTime(java.time.LocalTime.now())
                            .build();
                    userRepository.save(user);
                    userRepository.flush();
                }
            }
        }

        if (user == null) {
            throw new ApiException("User not found");
        }

        return user;
    }

    private Map<String, Object> buildUserData(AppUser user) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH-mm-ss");

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);

        String firstName = user.getName();
        String lastName = user.getSurname();
        String fullName = ((firstName == null ? "" : firstName.trim()) + " " +
                (lastName == null ? "" : lastName.trim())).trim();

        data.put("id", user.getSNo());
        data.put("userId", user.getUserId());
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("fullName", fullName.isBlank() ? null : fullName);
        data.put("name", fullName.isBlank() ? firstName : fullName);
        data.put("email", user.getEmailAddress());
        data.put("mobileNumber", user.getMobileNumber());
        data.put("role", user.getRole());

        // Compatibility fields for existing frontend/backend usage
        data.put("Name", firstName);
        data.put("S_No", user.getSNo());
        data.put("User_ID", user.getUserId());
        data.put("Surname", lastName);
        data.put("Email_Address", user.getEmailAddress());
        data.put("Mobile_Number", user.getMobileNumber());
        data.put("Share_ID", user.getShareId());

        data.put(
                "User_Created_Date",
                user.getUserCreatedDate() != null ? user.getUserCreatedDate().format(dateFmt) : null
        );
        data.put(
                "User_Created_Time",
                user.getUserCreatedTime() != null ? user.getUserCreatedTime().format(timeFmt) : null
        );

        return data;
    }

    /**
     * USER HOME / DASHBOARD (Protected)
     * GET /api/user/home
     * GET /api/user/dashboard
     */
    @GetMapping({"/home", "/dashboard"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> home(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);

            Map<String, Object> userData = buildUserData(user);
            
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.putAll(userData);
            dashboardData.put("userName", userData.get("firstName"));
            dashboardData.put("continueSession", null); // Could be populated from InterviewSessionRepository
            dashboardData.put("pastActivities", new java.util.ArrayList<>()); // Could be populated from InterviewSessionRepository
            
            // Default domains for the frontend
            java.util.List<Map<String, Object>> domains = new java.util.ArrayList<>();
            domains.add(createDomain("Technical", "Coding, System Design, etc.", "Code", 
                List.of(Map.of("label", "Java", "value", "java"), Map.of("label", "Python", "value", "python"))));
            domains.add(createDomain("Non-Technical", "HR, Behavioral, etc.", "Users", 
                List.of(Map.of("label", "HR", "value", "hr"), Map.of("label", "Management", "value", "management"))));
            
            dashboardData.put("domains", domains);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User dashboard loaded successfully");
            response.put("data", dashboardData);

            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load user dashboard"));
        }
    }

    private double calculatePeerRankScore(AppUser user) {
        double score = 0;
        if (user.getResumeScore() != null) {
            score += user.getResumeScore();
        }
        if (user.getExperienceYears() != null) {
            score += Math.min(10, user.getExperienceYears()) * 3;
        }
        if (user.isResumeScanned()) {
            score += 5;
        }
        return Math.min(100, score);
    }

    private String getPeerRankLabel(double score) {
        if (score >= 90) return "Elite";
        if (score >= 75) return "Pro";
        if (score >= 55) return "Advanced";
        if (score >= 35) return "Rising";
        return "Beginner";
    }

    private AppUser resolveTargetUser(String userId) {
        AppUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            user = userRepository.findByEmailAddress(userId).orElse(null);
        }
        if (user == null) {
            throw new ApiException("Target user not found: " + userId);
        }
        return user;
    }

    private Map<String, Object> buildUserResumeSummary(AppUser target) {
        Map<String, Object> map = new HashMap<>();
        String fullName = ((target.getName() == null ? "" : target.getName().trim()) + " " + (target.getSurname() == null ? "" : target.getSurname().trim())).trim();
        map.put("userId", target.getUserId());
        map.put("name", fullName.isBlank() ? target.getUserId() : fullName);
        map.put("currentCompany", target.getCurrentCompany());
        map.put("currentRole", target.getCurrentRole());
        map.put("headline", target.getProfileHeadline());
        map.put("profileSummary", target.getProfileSummary());
        map.put("experienceSummaryJson", target.getExperienceSummaryJson());
        map.put("topSkillsJson", target.getTopSkillsJson());

        resumeRepository.findByUser_UserIdOrderByCreatedAtDesc(target.getUserId())
                .stream()
                .findFirst()
                .ifPresent(resume -> {
                    map.put("resumeTitle", resume.getTitle());
                    map.put("resumeCode", resume.getResumeCode());
                    map.put("resumeUrl", resume.getOriginalFileUrl());
                    map.put("resumeFileName", resume.getOriginalFileName());
                });

        return map;
    }

    @GetMapping("/dashboard/ranks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPeerRanks(Authentication authentication) {
        try {
            resolveAuthenticatedUser(authentication);
            List<Map<String, Object>> ranked = userRepository.findAll().stream()
                    .map(user -> {
                        Map<String, Object> peer = new HashMap<>();
                        double rankScore = calculatePeerRankScore(user);
                        String fullName = ((user.getName() == null ? "" : user.getName().trim()) + " " + (user.getSurname() == null ? "" : user.getSurname().trim())).trim();
                        peer.put("userId", user.getUserId());
                        peer.put("name", fullName.isBlank() ? user.getUserId() : fullName);
                        peer.put("currentRole", user.getCurrentRole());
                        peer.put("currentCompany", user.getCurrentCompany());
                        peer.put("resumeScore", user.getResumeScore());
                        peer.put("experienceYears", user.getExperienceYears());
                        peer.put("rankScore", rankScore);
                        peer.put("rankLabel", getPeerRankLabel(rankScore));
                        return peer;
                    })
                    .sorted((a, b) -> Double.compare(
                            ((Number) b.get("rankScore")).doubleValue(),
                            ((Number) a.get("rankScore")).doubleValue()
                    ))
                    .toList();

            for (int i = 0; i < ranked.size(); i++) {
                ranked.get(i).put("position", i + 1);
            }

            return ResponseEntity.ok(ApiResponse.success("Peer rankings fetched successfully", ranked));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to fetch peer rankings"));
        }
    }

    @GetMapping("/dashboard/user-resume/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserResumeSummary(@PathVariable String userId, Authentication authentication) {
        try {
            resolveAuthenticatedUser(authentication);
            AppUser target = resolveTargetUser(userId);
            Map<String, Object> payload = buildUserResumeSummary(target);
            return ResponseEntity.ok(ApiResponse.success("User resume summary fetched", payload));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to load user resume summary"));
        }
    }

    private Map<String, Object> createDomain(String title, String subtitle, String icon, List<Map<String, String>> categories) {
        Map<String, Object> domain = new HashMap<>();
        domain.put("title", title);
        domain.put("subtitle", subtitle);
        domain.put("icon", icon);
        domain.put("categories", categories);
        return domain;
    }

    /**
     * USER PROFILE (Protected)
     * GET /api/user/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> me(Authentication authentication) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Authenticated user fetched successfully");
            response.put("data", buildUserData(user));

            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load user profile"));
        }
    }
}