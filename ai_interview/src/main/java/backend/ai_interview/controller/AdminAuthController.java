package backend.ai_interview.controller;

import backend.ai_interview.dto.request.AdminLoginRequest;
import backend.ai_interview.dto.request.AdminRegisterRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.service.auth.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Authentication Controller
 *
 * - Allows Admin Login
 * - Does NOT allow public Admin Registration
 * - Protects admin endpoints from user access
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/auth/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * ADMIN LOGIN
     *
     * Endpoint:
     * POST /api/auth/admin/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AdminLoginRequest request
    ) {
        AuthResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody AdminRegisterRequest request
    ) {
        AuthResponse response = adminAuthService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Optional: Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Admin Auth Service is running", "OK")
        );
    }
}