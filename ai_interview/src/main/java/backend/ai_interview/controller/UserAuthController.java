package backend.ai_interview.controller;

import backend.ai_interview.dto.request.UserLoginRequest;
import backend.ai_interview.dto.request.UserRegisterRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.service.auth.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Authentication Controller
 *
 * ✅ Users can Register
 * ✅ Users can Login
 * ❌ Admin registration is NOT available here
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/auth/user")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    /**
     * USER REGISTER
     *
     * Endpoint:
     * POST /api/auth/user/register
     *
     * Body:
     * {
     *   "name": "John",
     *   "surname": "Doe",
     *   "emailAddress": "john@example.com",
     *   "mobileNumber": "9876543210",
     *   "password": "secret123"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UserRegisterRequest request) {

        AuthResponse response = userAuthService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * USER LOGIN
     *
     * Endpoint:
     * POST /api/auth/user/login
     *
     * Body:
     * {
     *   "emailAddress": "john@example.com",
     *   "password": "secret123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginRequest request) {

        AuthResponse response = userAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * USER LOGOUT
     * Endpoint: POST /api/auth/user/logout
     * Alias for general logout if needed by frontend
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    /**
     * Optional: Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User Auth Service is running", "OK")
        );
    }
}