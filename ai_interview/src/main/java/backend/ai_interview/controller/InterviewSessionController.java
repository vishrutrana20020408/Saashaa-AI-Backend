package backend.ai_interview.controller;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.request.InterviewAnswerRequest;
import backend.ai_interview.dto.request.InterviewEvaluateRequest;
import backend.ai_interview.dto.request.InterviewHintRequest;
import backend.ai_interview.dto.request.InterviewStartRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.InterviewFeedbackResponse;
import backend.ai_interview.dto.response.InterviewQuestionResponse;
import backend.ai_interview.dto.response.InterviewScoreResponse;
import backend.ai_interview.dto.response.InterviewSessionResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.exception.AiIntegrationException;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.InterviewSessionException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.service.interview.InterviewEvaluationService;
import backend.ai_interview.service.interview.InterviewSessionService;
import jakarta.validation.Valid;
@RestController
@SuppressWarnings("all")
@RequestMapping({"/api/user/interview/session", "/api/interview/session"})
public class InterviewSessionController {

    private static final Logger log = LoggerFactory.getLogger(InterviewSessionController.class);

    private final InterviewSessionService interviewSessionService;
    private final InterviewEvaluationService interviewEvaluationService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public InterviewSessionController(
            InterviewSessionService interviewSessionService,
            InterviewEvaluationService interviewEvaluationService,
            UserRepository userRepository,
            AdminRepository adminRepository
    ) {
        this.interviewSessionService = interviewSessionService;
        this.interviewEvaluationService = interviewEvaluationService;
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
        log.debug("Resolving user for principal: {}", principal);

        // Try resolving as AppUser first
        AppUser user = userRepository.findByUserId(principal).orElse(null);
        if (user == null) {
            user = userRepository.findByEmailAddress(principal).orElse(null);
        }

        if (user == null) {
            // Try resolving as Admin and create a proxy AppUser if found
            Admin admin = adminRepository.findByAdminId(principal).orElse(null);
            if (admin == null) {
                admin = adminRepository.findByEmailAddress(principal).orElse(null);
            }

            if (admin != null) {
                log.info("Found admin record for principal: {}. Checking for mirror AppUser.", principal);
                // Check if an AppUser already exists for this admin email
                user = userRepository.findByEmailAddress(admin.getEmailAddress()).orElse(null);

                if (user == null) {
                    log.info("Mirror AppUser not found for admin: {}. Creating one.", admin.getEmailAddress());
                    // Create a mirror AppUser record for the admin so they can have a resume/interview
                    user = new AppUser();
                    user.setUserId(admin.getAdminId());
                    user.setName(admin.getName());
                    user.setSurname(admin.getSurname());
                    user.setEmailAddress(admin.getEmailAddress());
                    user.setMobileNumber(admin.getMobileNumber());
                    user.setPassword(admin.getPassword()); // Mirrors same hash
                    user.setRole("ADMIN");
                    user.setShareId(admin.getShareId());
                    user.setUserCreatedDate(java.time.LocalDate.now());
                    user.setUserCreatedTime(java.time.LocalTime.now());
                    user = userRepository.save(user);
                }
            }
        }

        if (user == null) {
            log.error("Failed to resolve user for principal: {}", principal);
            throw new ApiException("User not found");
        }

        return user;
    }

    /**
     * Create a new interview session.
     * POST /api/user/interview/session
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> createSession(
            @Valid @RequestBody InterviewStartRequest request,
            Authentication authentication
    ) {
        try {
            log.debug("Received interview start request: {}", request);
            
            AppUser user = resolveAuthenticatedUser(authentication);
            
            Long userId = null;
            Long adminId = null;
            
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                // Find admin by email or id
                Admin admin = adminRepository.findByEmailAddress(user.getEmailAddress()).orElse(null);
                if (admin != null) {
                    adminId = admin.getSNo();
                } else {
                    // If mirror user is ADMIN but no admin record exists, use user sNo
                    userId = user.getSNo();
                }
            } else {
                userId = user.getSNo();
            }

            log.info("Starting session for user: {} (userId: {}, adminId: {})", user.getEmailAddress(), userId, adminId);
            InterviewSessionResponse response = interviewSessionService.startSession(request, userId, adminId);
            return ResponseEntity.ok(ApiResponse.success("Interview session created", response));
        } catch (ApiException | InterviewSessionException ex) {
            log.warn("Interview API error in createSession: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (AiIntegrationException ex) {
            log.error("AI integration failure in createSession", ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail("AI engine unavailable: " + ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error in createSession", ex);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to create interview session: " + ex.getMessage()));
        }
    }

    /**
     * Get all active interview sessions for current user.
     * GET /api/user/interview/session
     * GET /api/user/interview/sessions
     */
    @GetMapping({"", "/sessions"})
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<List<InterviewSessionResponse>>> getAllSessions(
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            List<InterviewSessionResponse> sessions;

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                Admin admin = adminRepository.findByEmailAddress(user.getEmailAddress()).orElse(null);
                if (admin != null) {
                    sessions = interviewSessionService.getSessionsByAdmin(admin.getSNo());
                } else {
                    sessions = List.of();
                }
            } else {
                sessions = interviewSessionService.getSessionsByUser(user.getSNo());
            }

            return ResponseEntity.ok(ApiResponse.success("Sessions fetched", sessions));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to fetch sessions: " + ex.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<List<InterviewSessionResponse>>> getPendingSessions(
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            List<InterviewSessionResponse> sessions;

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                Admin admin = adminRepository.findByEmailAddress(user.getEmailAddress()).orElse(null);
                if (admin != null) {
                    sessions = interviewSessionService.getPendingSessionsByAdmin(admin.getSNo());
                } else {
                    sessions = List.of();
                }
            } else {
                sessions = interviewSessionService.getPendingSessionsByUser(user.getSNo());
            }

            return ResponseEntity.ok(ApiResponse.success("Pending interview sessions fetched", sessions));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to fetch pending sessions: " + ex.getMessage()));
        }
    }

    /**
     * Get interview session details by session id.
     * GET /api/user/interview/session/{sessionId}
     */
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> getInterviewSession(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        try {
            AppUser user = resolveAuthenticatedUser(authentication);
            InterviewSessionResponse response = interviewSessionService.getSessionById(sessionId);
            
            // Verify ownership
            verifySessionOwnership(response, user);
            
            return ResponseEntity.ok(ApiResponse.success("Interview session fetched", response));
        } catch (ApiException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to fetch session: " + ex.getMessage()));
        }
    }

    private void verifySessionOwnership(InterviewSessionResponse session, AppUser user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            Admin admin = adminRepository.findByEmailAddress(user.getEmailAddress()).orElse(null);
            if (admin == null || !admin.getSNo().equals(session.getAdminId())) {
                throw new ApiException("Access denied: You do not own this session.");
            }
        } else {
            if (!user.getSNo().equals(session.getUserId())) {
                throw new ApiException("Access denied: You do not own this session.");
            }
        }
    }

    private String normalizeToken(String token) {
        return token != null && !token.isBlank() ? token.trim() : null;
    }

    private String resolveRequestToken(String token, String sessionToken) {
        String normalized = normalizeToken(token);
        if (normalized != null) {
            return normalized;
        }
        return normalizeToken(sessionToken);
    }

    private void verifyInterviewToken(InterviewSessionResponse session, String token) {
        String resolvedToken = normalizeToken(token);
        if (resolvedToken == null) {
            throw new ApiException("Interview token is required for this action.");
        }

        if (!resolvedToken.equals(session.getInterviewToken())) {
            throw new ApiException("Interview token does not match the requested session.");
        }
    }

    /**
     * Start session
     * POST /api/user/interview/session/{sessionId}/start
     */
    @PostMapping("/{sessionId}/start")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> startSession(
            @PathVariable Long sessionId
    ) {
        try {
            InterviewSessionResponse response = interviewSessionService.getSessionById(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Interview session started", response));
        } catch (ApiException ex) {
            log.warn("Interview API error in startSession: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error in startSession", ex);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("Failed to start interview session: " + ex.getMessage()));
        }
    }

    /**
     * Update transcript
     * POST /api/user/interview/session/{sessionId}/transcript
     */
    @PostMapping("/{sessionId}/transcript")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> updateTranscript(
            @PathVariable Long sessionId,
            @Valid @RequestBody InterviewAnswerRequest request
    ) {
        InterviewSessionResponse session = interviewSessionService.getSessionById(sessionId);
        String requestToken = resolveRequestToken(request.getToken(), request.getSessionToken());
        verifyInterviewToken(session, requestToken);

        InterviewFeedbackResponse response = interviewSessionService.submitAnswer(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Transcript updated", response));
    }

    /**
     * End session
     * POST /api/user/interview/session/{sessionId}/end
     */
    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> endSession(
            @PathVariable Long sessionId
    ) {
        InterviewSessionResponse response = interviewSessionService.finishSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Interview session ended", response));
    }

    /**
     * Save typing result
     * POST /api/user/interview/session/{sessionId}/typing-result
     */
    @PostMapping("/{sessionId}/typing-result")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<String>> saveTypingResult(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> typingData
    ) {
        // Mock implementation for now
        return ResponseEntity.ok(ApiResponse.success("Typing result saved", "OK"));
    }

    /**
     * Submit an answer for the current interview turn.
     * POST /api/user/interview/session/{sessionId}/answer
     */
    @PostMapping("/{sessionId}/answer")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody InterviewAnswerRequest request
    ) {
        InterviewSessionResponse session = interviewSessionService.getSessionById(sessionId);
        String requestToken = resolveRequestToken(request.getToken(), request.getSessionToken());
        verifyInterviewToken(session, requestToken);

        InterviewFeedbackResponse response = interviewSessionService.submitAnswer(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Answer submitted", response));
    }

    /**
     * Ask backend/AI-engine for a hint or mock-help response.
     * POST /api/user/interview/session/{sessionId}/hint
     */
    @PostMapping("/{sessionId}/hint")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> requestHint(
            @PathVariable Long sessionId,
            @Valid @RequestBody InterviewHintRequest request
    ) {
        InterviewSessionResponse session = interviewSessionService.getSessionById(sessionId);
        String requestToken = resolveRequestToken(request.getToken(), request.getSessionToken());
        verifyInterviewToken(session, requestToken);

        InterviewFeedbackResponse response = interviewSessionService.requestHint(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Hint generated", response));
    }

    /**
     * Evaluate the latest answer or a supplied answer payload.
     * POST /api/user/interview/session/{sessionId}/evaluate
     */
    @PostMapping("/{sessionId}/evaluate")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> evaluateAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody InterviewEvaluateRequest request
    ) {
        InterviewSessionResponse session = interviewSessionService.getSessionById(sessionId);
        String requestToken = resolveRequestToken(request.getToken(), request.getSessionToken());
        verifyInterviewToken(session, requestToken);

        InterviewFeedbackResponse response = interviewEvaluationService.evaluateAnswer(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Answer evaluated", response));
    }

    /**
     * Generate/fetch the next interview question.
     * POST /api/user/interview/session/{sessionId}/next-question
     */
    @PostMapping("/{sessionId}/next-question")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewQuestionResponse>> nextQuestion(
            @PathVariable Long sessionId
    ) {
        InterviewQuestionResponse response = interviewSessionService.generateNextQuestion(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Next question generated", response));
    }

    /**
     * Final score retrieval.
     * GET /api/user/interview/session/{sessionId}/score
     */
    @GetMapping("/{sessionId}/score")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<InterviewScoreResponse>> getScore(
            @PathVariable Long sessionId
    ) {
        InterviewScoreResponse response = interviewEvaluationService.getScore(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Final score fetched", response));
    }
}